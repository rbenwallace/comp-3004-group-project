package com.uniques.ourhouse;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Schedule;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import androidx.annotation.NonNull;

public class EventService extends JobService {
    private static final String TAG = "EventService";
    // job should run once a day
    public static final long JOB_INTERVAL_MILLIS = 24 * 60 * 60 * 1000;
    // job should run within the last 6 hours of the day
    static final long JOB_INTERVAL_FLEX_MILLIS = (long) (0.4 * JOB_INTERVAL_MILLIS);

    private Logic runningLogic;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Starting Logic");
        if (Session.getSession() == null) {
            Session.newSession(this);
        }
        runningLogic = manualInvoke(wantsReschedule -> {
            runningLogic = null;
            jobFinished(params, wantsReschedule);
        });
        runningLogic.doInBackground(Session.getSession());
        return true;
    }

    public static Logic manualInvoke(Consumer<Boolean> onEndConsumer) {
        return new Logic((exception, failedEvents) -> {
            if (exception != null) {
                Log.e(TAG, "Failed Logic with error", exception);
            } else {
                if (failedEvents.isEmpty()) {
                    Log.i(TAG, "Finished Logic Successfully");
                } else {
                    Log.i(TAG, "Finished Logic with " + failedEvents.size()
                            + " failedEvents= " + failedEvents);
                }
                Settings.EVENT_SERVICE_LAST_UPDATE.set(System.currentTimeMillis());
            }
            onEndConsumer.accept(exception != null);
        });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (runningLogic == null) return false;
        Log.i(TAG, "Cancelling Logic");
        runningLogic.cancel(true);
        runningLogic = null;
        return true;
    }

    public static class Logic extends AsyncTask<Session, Void, Void> {
        private static final String TAG = EventService.TAG + ".Logic";
        private final BiConsumer<Exception, List<Event>> onCompleteCallback;
        private BiConsumer<Event, Event> getterConsumer;

        private Logic(BiConsumer<Exception, List<Event>> onCompleteCallback) {
            this.onCompleteCallback = onCompleteCallback;
        }

        @Override
        public Void doInBackground(Session... sessions) {
            Session session = sessions[0];
            if (session == null) {
                onCompleteCallback.accept(new NullPointerException("session is null"), null);
                return null;
            }

            ObjectId loggedInUser = session.getLoggedInUserId();
            // a user must be logged in
            if (loggedInUser == null) {
                onCompleteCallback.accept(new NullPointerException("User is not logged in"), null);
                return null;
            }

            ObjectId openHouse = Settings.OPEN_HOUSE.get();
            // the user must have opened a house
            if (openHouse == null) {
                onCompleteCallback.accept(new NullPointerException("No OPEN_HOUSE setting"), null);
                return null;
            }

            // get tasks from database
            session.getDatabase().getHouse(openHouse, house -> {
                if (isCancelled()) return;
                Objects.requireNonNull(house);
                Log.d(TAG, "Got open house object");
                Log.d(TAG, "Fetching house tasks.. (might take a while)");
                session.getDatabase().getAllTasksFromHouse(openHouse, tasks -> {
                    if (isCancelled()) return;
                    if (tasks.isEmpty()) {
                        onCompleteCallback.accept(new NullPointerException("No Tasks created in house"), null);
                        return;
                    }

                    Log.d(TAG, "Got house tasks");

                    HashMap<Date, List<Task>> occurrences = new HashMap<>();

                    Calendar cal = initializeMonth();
                    Date lowerBound = cal.getTime();
                    Date upperBound = upperBoundOfMonth(cal.getTime());

                    for (Task t : tasks) {
                        Schedule schedule = t.getSchedule();

                        Log.d(TAG, "Iterating over occurrences in {" + t + "}..");

                        for (Date occurrence : schedule.finiteIterable(lowerBound, upperBound)) {
                            Date dayOfOccurrence = initializeDay(occurrence);

                            if (occurrences.containsKey(dayOfOccurrence)) {
                                //noinspection ConstantConditions
                                occurrences.get(dayOfOccurrence).add(t);
                            } else {
                                ArrayList<Task> l = new ArrayList<>();
                                l.add(t);
                                occurrences.put(dayOfOccurrence, l);
                            }
                        }
                    }

                    if (isCancelled()) return;

                    Log.d(TAG, "Done iterating over tasks");

                    House.Rotation rotation = house.getRotation();
                    if (rotation.getRotation().isEmpty()) {
                        onCompleteCallback.accept(new Exception("OPEN_HOUSE rotation list is empty"), null);
                        return;
                    }

                    PriorityQueue<UsersTasks> results = new PriorityQueue<>();
                    for (User user : rotation) {
                        results.add(new UsersTasks(user));
                    }

                    Log.d(TAG, "Assigning each gathered occurrence..");

                    Iterator<Date> days = occurrences.keySet().iterator();
                    Date curDay;
                    while (days.hasNext()) {
                        curDay = days.next();

                        Log.d(TAG, "Assigning for day " + curDay + "...");

                        PriorityQueue<Task> daysTasks = new PriorityQueue<>(
                                (o1, o2) -> o2.getDifficulty() - o1.getDifficulty());
                        daysTasks.addAll(occurrences.get(curDay));

                        while (!daysTasks.isEmpty()) {
                            //noinspection ConstantConditions
                            results.peek().assignTask(curDay, daysTasks.poll());
                            results.offer(results.poll());
                        }
                    }

                    Log.d(TAG, "Done assigning");
                    Log.d(TAG, "Generating event objects..");

                    List<Event> events = new ArrayList<>();
                    for (UsersTasks result : results) {
                        events.addAll(result.createEvents());
                    }

                    Log.d(TAG, "Done generating event objects");

                    if (isCancelled()) return;
                    if (events.size() == 0) {
                        onCompleteCallback.accept(new Exception("generated event list was empty"), null);
                        return;
                    }

                    List<Event> failedEvents = new ArrayList<>();

                    BiConsumer<Boolean, Event> putterConsumer = (success, event) -> {
                        if (isCancelled()) return;
                        if (!success) {
                            failedEvents.add(event);
                        }
                        if (events.isEmpty()) {
                            // COMPLETE
                            onCompleteCallback.accept(null, failedEvents);
                        } else {
                            Event nextEvent = events.remove(0);
                            session.getDatabase().getEvent(nextEvent.getId(), e -> getterConsumer.accept(nextEvent, e));
                        }
                    };

                    if (getterConsumer != null) {
                        onCompleteCallback.accept(new Exception("Attempt to rerun logic using the same instance"), null);
                        return;
                    }

                    getterConsumer = (orgEvent, fetchedEvent) -> {
                        if (isCancelled()) return;
                        if (fetchedEvent == null) {
                            session.getDatabase().postEvent(orgEvent, success -> putterConsumer.accept(success, orgEvent));
                        } else {
                            putterConsumer.accept(true, null);
                        }
                    };

                    Log.d(TAG, "Posting (non-existent) events to database.. (might take a while)");

                    Event firstEvent = events.remove(0);
                    session.getDatabase().getEvent(firstEvent.getId(), event -> getterConsumer.accept(firstEvent, event));
                });
            });
            return null;
        }

        private Calendar initializeMonth() {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal;
        }

        private Date initializeDay(Date day) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(day);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            return cal.getTime();
        }

        private Date upperBoundOfMonth(Date dateInMonth) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateInMonth);
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            return cal.getTime();
        }

        private static class UsersTasks implements Comparable {
            private final User user;
            private int totalDifficulty;
            private HashMap<Date, List<Task>> tasks;

            private UsersTasks(User user) {
                this.user = user;
                tasks = new HashMap<>();
            }

            private void assignTask(Date day, Task task) {
                if (tasks.containsKey(day)) {
                    //noinspection ConstantConditions
                    tasks.get(day).add(task);
                } else {
                    List<Task> l = new ArrayList<>();
                    l.add(task);
                    tasks.put(day, l);
                }
                totalDifficulty += task.getDifficulty();
            }

            private List<Event> createEvents() {
                List<Event> events = new ArrayList<>();
                tasks.forEach(((day, tasks) ->
                        tasks.forEach(task ->
                                events.add(new Event(task.getName(), day, user, null)))));
                return events;
            }

            @Override
            public int compareTo(@NonNull Object o) {
                if (o instanceof UsersTasks) {
                    return totalDifficulty - ((UsersTasks) o).totalDifficulty;
                } else return 0;
            }
        }
    }
}
