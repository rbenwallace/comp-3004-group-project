package com.uniques.ourhouse.controller;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EventService extends JobService {
    private Logic runningLogic;

    @Override
    public boolean onStartJob(JobParameters params) {
        if (Session.getSession() == null) {
            Session.newSession(this);
        }
        runningLogic = new Logic(failedEvents -> {
            runningLogic = null;
            System.out.println("EVENT-SERVICE failedEvents= " + failedEvents);
        });
        runningLogic.doInBackground(Session.getSession());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (runningLogic == null) return false;
        runningLogic.cancel(true);
        runningLogic = null;
        return true;
    }

    private static class Logic extends AsyncTask<Session, Void, Void> {
        private Consumer<List<Event>> onCompleteCallback;
        private BiConsumer<Event, Event> getterConsumer;

        private Logic(Consumer<List<Event>> onCompleteCallback) {
            this.onCompleteCallback = onCompleteCallback;
        }

        @Override
        protected Void doInBackground(Session... sessions) {
            Session session = sessions[0];

            HashMap<Date, List<Task>> occurrences = new HashMap<>();

            // get tasks from database
            ArrayList<Task> tasks = new ArrayList<>(Arrays.asList(Task.testTasks()));
            if (isCancelled()) return null;

            Calendar cal = initializeMonth();
            Date upperBound = upperBoundOfMonth(cal.getTime());

            for (Task t : tasks) {
                Schedule schedule = t.getSchedule();

                for (Date occurrence : schedule.finiteIterable(upperBound)) {
                    Date dayOfOccurrence = initializeDay(occurrence);

                    if (occurrences.containsKey(dayOfOccurrence)) {
                        occurrences.get(dayOfOccurrence).add(t);
                    } else {
                        ArrayList<Task> l = new ArrayList<>();
                        l.add(t);
                        occurrences.put(dayOfOccurrence, l);
                    }
                }
            }

            if (isCancelled()) return null;

            House.Rotation rotation = House.testHouse().getRotation();

            PriorityQueue<UsersTasks> results = new PriorityQueue<>();
            for (User user : rotation) {
                results.add(new UsersTasks(user));
            }

            Iterator<Date> days = occurrences.keySet().iterator();
            Date curDay;
            while (days.hasNext()) {
                curDay = days.next();

                PriorityQueue<Task> daysTasks = new PriorityQueue<>((o1, o2) -> o2.getDifficulty() - o1.getDifficulty());
                daysTasks.addAll(occurrences.get(curDay));

                while (!daysTasks.isEmpty()) {
                    results.peek().assignTask(curDay, daysTasks.poll());
                    results.offer(results.poll());
                }
            }

            List<Event> events = new ArrayList<>();
            for (UsersTasks result : results) {
                events.addAll(result.createEvents());
            }

            if (events.size() == 0) return null;
            if (isCancelled()) return null;

            List<Event> failedEvents = new ArrayList<>();

            BiConsumer<Boolean, Event> putterConsumer = (success, failedEvent) -> {
                if (isCancelled()) return;
                if (!success) {
                    failedEvents.add(failedEvent);
                }
                if (events.isEmpty()) {
                    // COMPLETE
                    onCompleteCallback.accept(failedEvents);
                } else {
                    Event nextEvent = events.remove(0);
                    session.getDatabase().getEvent(nextEvent.getId(), e -> getterConsumer.accept(nextEvent, e));
                }
            };
            getterConsumer = (orgEvent, fetchedEvent) -> {
                if (isCancelled()) return;
                if (fetchedEvent == null) {
                    session.getDatabase().postEvent(orgEvent, success -> putterConsumer.accept(success, orgEvent));
                } else {
                    putterConsumer.accept(true, null);
                }
            };

            Event firstEvent = events.remove(0);
            session.getDatabase().getEvent(firstEvent.getId(), event -> getterConsumer.accept(firstEvent, event));
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
                    tasks.get(day).add(task);
                } else {
                    List<Task> l = new ArrayList<>();
                    l.add(task);
                    tasks.put(day, l);
                }
            }

            private List<Event> createEvents() {
                List<Event> events = new ArrayList<>();
                tasks.forEach(((day, tasks) ->
                        tasks.forEach(task ->
                                events.add(new Event(task.getName(), user, day, null)))));
                return events;
            }

            @Override
            public int compareTo(Object o) {
                if (o instanceof UsersTasks) {
                    return totalDifficulty - ((UsersTasks) o).totalDifficulty;
                } else return 0;
            }
        }
    }
}
