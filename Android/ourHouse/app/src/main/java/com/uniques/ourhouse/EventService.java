package com.uniques.ourhouse;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.ManageItem;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.session.DatabaseLink;
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
            DatabaseLink database = session.getDatabase();
            if (database == null) {
                onCompleteCallback.accept(new NullPointerException("session database is null"), null);
                return null;
            }
            ObjectId loggedInUser = session.getLoggedInUserId();
            // a user must be logged in
            if (loggedInUser == null) {
                onCompleteCallback.accept(new Exception("User is not logged in"), null);
                return null;
            }

            ObjectId openHouse = Settings.OPEN_HOUSE.get();
            // the user must have opened a house
            if (openHouse == null) {
                onCompleteCallback.accept(new NullPointerException("OPEN_HOUSE setting is unset"), null);
                return null;
            }

            // get tasks from database
            database.getHouse(openHouse, house -> {
                if (isCancelled()) return;
                if (house == null) {
                    onCompleteCallback.accept(new NullPointerException("Failed to get open house object"), null);
                    return;
                }
                Log.d(TAG, "Got open house object");
                Log.d(TAG, "Fetching house tasks.. (might take a while)");
                database.getAllTasksFromHouse(openHouse, tasks -> {
                    if (isCancelled()) return;
                    if (tasks == null) {
                        onCompleteCallback.accept(new NullPointerException("getAllTasksFromHouse() failed"), null);
                        return;
                    }
                    Log.d(TAG, "Got house tasks");

                    List<ManageItem> items = new ArrayList<>(tasks);
                    //noinspection UnusedAssignment
                    tasks = null;

                    database.getAllFeesFromHouse(openHouse, fees -> {
                        if (isCancelled()) return;
                        if (fees == null) {
                            onCompleteCallback.accept(new NullPointerException("getAllFeesFromHouse() failed"), null);
                            return;
                        }

                        while (!fees.isEmpty()) {
                            items.add(fees.remove(0));
                        }
                        //noinspection UnusedAssignment
                        fees = null;

                        if (items.isEmpty()) {

                            onCompleteCallback.accept(new Exception("No Tasks or Fees created in house"), null);
                            return;
                        }
                        Log.d(TAG, "Got house fees");

                        HashMap<Date, List<ManageItem>> occurrences = new HashMap<>();

                        Calendar cal = initializeMonth();
                        Date lowerBound = cal.getTime();
                        Date upperBound = upperBoundOfMonth(cal.getTime());

                        for (ManageItem item : items) {
                            Schedule schedule = item.getSchedule();

                            Log.d(TAG, "Iterating over occurrences in {" + item + "}..");

                            for (Date occurrence : schedule.finiteIterable(lowerBound, upperBound)) {
                                Date dayOfOccurrence = initializeDueDay(occurrence);

                                if (occurrences.containsKey(dayOfOccurrence)) {
                                    //noinspection ConstantConditions
                                    occurrences.get(dayOfOccurrence).add(item);
                                } else {
                                    ArrayList<ManageItem> l = new ArrayList<>();
                                    l.add(item);
                                    occurrences.put(dayOfOccurrence, l);
                                }
                            }
                        }

                        if (isCancelled()) return;

                        Log.d(TAG, "Done iterating over items");

                        House.Rotation rotation = house.getRotation();
                        if (rotation.getRotation().isEmpty()) {
                            onCompleteCallback.accept(new Exception("OPEN_HOUSE rotation list is empty"), null);
                            return;
                        }

                        PriorityQueue<UsersDuties> results = new PriorityQueue<>();
                        for (ObjectId userId : rotation) {
                            results.add(new UsersDuties(userId, openHouse));
                        }

                        Log.d(TAG, "Assigning each gathered occurrence..");

                        Iterator<Date> days = occurrences.keySet().iterator();
                        Date curDay;
                        while (days.hasNext()) {
                            curDay = days.next();

                            Log.d(TAG, "Assigning for day " + curDay + "...");

                            PriorityQueue<ManageItem> daysDuties = new PriorityQueue<>(
                                    (o1, o2) -> o2.getDifficulty() - o1.getDifficulty());
                            daysDuties.addAll(occurrences.get(curDay));

                            while (!daysDuties.isEmpty()) {
                                //noinspection ConstantConditions
                                results.peek().assignDuty(curDay, daysDuties.poll());
                                results.offer(results.poll());
                            }
                        }

                        Log.d(TAG, "Done assigning");
                        Log.d(TAG, "Generating event objects..");

                        List<Event> events = new ArrayList<>();
                        for (UsersDuties result : results) {
                            events.addAll(result.createEvents());
                        }

                        Log.d(TAG, "Done generating event objects");

                        if (isCancelled()) return;
                        if (events.isEmpty()) {
                            onCompleteCallback.accept(new Exception("generated event list was empty"), null);
                            return;
                        }

                        long now = System.currentTimeMillis();
                        List<Event> failedEvents = new ArrayList<>();

                        // eventPair = { fetchedEvent, orgEvent }
                        BiConsumer<Pair<Event, Event>, BiConsumer> batchConsumer = (eventPair, next) -> {
                            if (isCancelled()) return;

                            Event fetchedEvent = eventPair.first;
                            Event orgEvent = eventPair.second;

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
                                    //noinspection unchecked
                                    database.getHouseEventOnDay(
                                            openHouse,
                                            nextEvent.getAssociatedTask(),
                                            nextEvent.getDueDate(),
                                            e -> next.accept(new Pair<>(e, nextEvent), next)
                                    );
                                }
                            };

                            // event doesn't exist yet
                            if (fetchedEvent == null) {
                                // post a new one
                                database.postEvent(orgEvent, success -> putterConsumer.accept(success, orgEvent));
                                return;
                            }

                            // event is in the past (we don't update past events)
                            if (fetchedEvent.getDueDate().getTime() < now) {
                                // mark event as successful
                                putterConsumer.accept(true, null);
                                return;
                            }

                            // event is in the future, delete old event and post a new one
                            database.deleteEvent(fetchedEvent, deleted -> {
                                if (!deleted) {
                                    // old event must be deleted to count as a success
                                    putterConsumer.accept(false, orgEvent);
                                    return;
                                }
                                // post the new event
                                database.postEvent(orgEvent, success -> putterConsumer.accept(success, orgEvent));
                            });
                        };

                        Log.d(TAG, "Posting (non-existent) events to database.. (might take a while)");

                        Event firstEvent = events.remove(0);
                        database.getHouseEventOnDay(
                                openHouse,
                                firstEvent.getAssociatedTask(),
                                firstEvent.getDueDate(),
                                event -> batchConsumer.accept(new Pair<>(event, firstEvent), batchConsumer)
                        );
                    });
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

        private Date initializeDueDay(Date day) {
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

        private static class UsersDuties implements Comparable {
            private final ObjectId userId;
            private final ObjectId houseId;
            private int totalDifficulty;
            private HashMap<Date, List<ManageItem>> duties;

            private UsersDuties(ObjectId userId, ObjectId houseId) {
                this.userId = userId;
                this.houseId = houseId;
                duties = new HashMap<>();
            }

            private void assignDuty(Date day, ManageItem duty) {
                if (duties.containsKey(day)) {
                    //noinspection ConstantConditions
                    duties.get(day).add(duty);
                } else {
                    List<ManageItem> l = new ArrayList<>();
                    l.add(duty);
                    duties.put(day, l);
                }
                totalDifficulty += duty.getDifficulty();
            }

            private List<Event> createEvents() {
                List<Event> events = new ArrayList<>();
                duties.forEach((day, duty) ->
                        duty.forEach(dutyItem -> {
                            int type = dutyItem instanceof Task ? Event.TYPE_TASK : Event.TYPE_FEE;
                            events.add(new Event(type, dutyItem.getName(), day, userId, houseId, dutyItem.getId()));
                        }));
                return events;
            }

            @Override
            public int compareTo(@NonNull Object o) {
                if (o instanceof UsersDuties) {
                    return totalDifficulty - ((UsersDuties) o).totalDifficulty;
                } else return 0;
            }
        }
    }
}
