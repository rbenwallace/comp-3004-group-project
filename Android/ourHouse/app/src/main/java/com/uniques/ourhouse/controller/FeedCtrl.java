package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uniques.ourhouse.EventService;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.RecyclerCtrl;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FeedCtrl implements FragmentCtrl, RecyclerCtrl<FeedCard> {
    private static final String TAG = "FeedCtrl";

    FragmentActivity activity;
    private TextView txtPastEvents;
    private TextView txtUpcomingEvents;
    private TextView loadingText;
    private LinearLayout loadingPanel;
    private RecyclerView recyclerView;
    private boolean showingPastEvents;
    private boolean pendingReopenFilter;

    public List<FeedCard> observableCards;
    private RecyclerAdapter<FeedCard> cardsAdapter;
    private EventService.Logic eventServiceLogic;

    public FeedCtrl(FragmentActivity activity) {
        this.activity = activity;
        observableCards = new ArrayList<>();
    }

    @Override
    public void init(View view) {
        txtPastEvents = view.findViewById(R.id.feed_txtPastEvents);
        txtUpcomingEvents = view.findViewById(R.id.feed_txtUpcomingEvents);
        loadingText = view.findViewById(R.id.feed_loading_txtMsg);
        loadingPanel = view.findViewById(R.id.feed_loadingPanel);
        recyclerView = view.findViewById(R.id.feed_recycler);

        showingPastEvents = Settings.FEED_SHOWING_PAST_EVENTS.get();
        ObjectId houseId = Settings.OPEN_HOUSE.get();
        Session.getSession().getDatabase().getHouse(houseId, house -> {
            Settings.FEED_PENALIZE_TASKS.set(house.getPenalizeLateTasks());
        });

        txtPastEvents.setOnClickListener(v -> {
            if (!showingPastEvents) {
                showingPastEvents = true;
                Settings.FEED_SHOWING_PAST_EVENTS.set(true);
                updateInfo();
            }
        });
        txtUpcomingEvents.setOnClickListener(v -> {
            if (showingPastEvents) {
                showingPastEvents = false;
                Settings.FEED_SHOWING_PAST_EVENTS.set(false);
                updateInfo();
            }
        });

        Long eventServiceLastUpdate = Settings.EVENT_SERVICE_LAST_UPDATE.get();

        if (eventServiceLastUpdate == null
                || (System.currentTimeMillis() - eventServiceLastUpdate) > EventService.JOB_INTERVAL_MILLIS) {

            Log.d(TAG, "Last run of EventService.Logic may be out of date");
            invokeEventServiceLogic();
        }
    }

    @Override
    public void acceptArguments(Object... args) {
    }

    void updateInfoAndReopenFilter() {
        pendingReopenFilter = true;
        updateInfo();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateInfo() {
        loadingPanel.setVisibility(View.VISIBLE);

        if (eventServiceLogic != null)
            return;

        if (!Settings.EVENT_SERVICE_DUTIES_ARE_PRISTINE.<Boolean>get()) {
            Log.d(TAG, "Last run of EventService.Logic may be out of date");
            invokeEventServiceLogic();
            return;
        }

        loadingText.setText("Fetching events...");

        txtPastEvents.setTextColor(activity.getColor(showingPastEvents ? R.color.colorTextPrimary : R.color.colorTextSecondary));
        txtUpcomingEvents.setTextColor(activity.getColor(!showingPastEvents ? R.color.colorTextPrimary : R.color.colorTextSecondary));
        if (showingPastEvents) {
            txtPastEvents.setTypeface(txtPastEvents.getTypeface(), Typeface.BOLD);
            txtUpcomingEvents.setTypeface(txtUpcomingEvents.getTypeface(), Typeface.NORMAL);
        } else {
            txtUpcomingEvents.setTypeface(txtPastEvents.getTypeface(), Typeface.BOLD);
            txtPastEvents.setTypeface(txtUpcomingEvents.getTypeface(), Typeface.NORMAL);
        }

        List<FeedCard> feedCardList = new ArrayList<>();

        if (RefreshTask.refreshing) {
            return;
        }

        RefreshTask refreshTask = new RefreshTask(this, v -> {
//               Util.sortList(feedCardList, false);

            Log.d(TAG, "observableCardsSize " + observableCards.size() + " | feedCardsSize " + feedCardList.size());

            for (int i = observableCards.size() - 1; i >= 0; --i) {
                if (!feedCardList.contains(observableCards.get(i))) {
                    observableCards.remove(i);
                    cardsAdapter.notifyItemRemoved(i);
                }
            }

//               outer:
            for (int i = 0; i < feedCardList.size(); ++i) {
                FeedCard feedCard = feedCardList.get(i);
                if (!observableCards.contains(feedCard)) {
//                       for (int j = 0; j < observableCards.size(); ++j) {
//                            if (observableCards.get(j).compareTo(feedCard) > 0) {
//                                observableCards.add(j, feedCard);
//                              cardsAdapter.notifyItemInserted(j);
//                              continue outer;
//                         }
//                        }
                    observableCards.add(i, feedCard);
                    cardsAdapter.notifyItemInserted(observableCards.size() - 1);
                } else {
                    int previousIndex;
                    for (previousIndex = i; previousIndex < observableCards.size(); ++previousIndex) {
                        if (observableCards.get(previousIndex).equals(feedCard)) {
                            if (previousIndex != i) {
                                observableCards.remove(previousIndex);
                                observableCards.add(i, feedCard);
                                cardsAdapter.notifyItemMoved(previousIndex, i);
                            } else {
                                observableCards.get(i).updateInfo();
                            }
                        }
                    }
                }
            }

            loadingPanel.setVisibility(View.GONE);

//             cardsAdapter.notifyDataSetChanged();
        });
        refreshTask.doInBackground(feedCardList);
    }

    @Override
    public void setRecyclerAdapter(RecyclerAdapter<FeedCard> recyclerAdapter) {
        this.cardsAdapter = recyclerAdapter;
        recyclerAdapter.setViewSelector(new RecyclerAdapter.ViewSelector<FeedCard>() {
            @Override
            public int getItemViewType(FeedCard card) {
                FeedCard.FeedCardType cardType = card.getCardType();
                if (cardType == FeedCard.FeedCardType.FILTER) {
                    return 2;
                } else if (cardType == FeedCard.FeedCardType.DATE) {
                    return 1;
                } else {
                    return 0;
                }
            }

            @Override
            public int getViewLayoutId(int itemViewType) {
                if (itemViewType == 0) {
                    return R.layout.content_card_feed_bubble;
                } else if (itemViewType == 1) {
                    return R.layout.content_card_feed_date;
                } else if (itemViewType == 2) {
                    return R.layout.content_card_feed_filter;
                }
                return -1;
            }
        });
    }

    private static class RefreshTask extends AsyncTask<List<FeedCard>, Void, Void> {
        private static boolean refreshing;

        private final FeedCtrl controller;
        private final Consumer<Void> consumer;

        private RefreshTask(FeedCtrl controller, Consumer<Void> consumer) {
            this.controller = controller;
            this.consumer = consumer;
        }

        @Override
        protected void onPreExecute() {
            if (refreshing)
                throw new RuntimeException("Attempt to perform multiple RefreshTasks at a time");
            refreshing = true;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<FeedCard>... lists) {
            List<FeedCard> feedCardList = lists[0];

            FeedCard filterCard = new FeedCard(FeedCard.FeedCardType.FILTER, new FeedCard.FeedCardObject() {
                @Override
                public Date getDueDate() {
                    return null;
                }

                @Override
                public Date getDateCompleted() {
                    return null;
                }

                @Override
                public void getPerson(Consumer<User> consumer) {
                    consumer.accept(null);
                }

                @Override
                public int getCompareType() {
                    return -1;
                }

                @Override
                public java.lang.Comparable getCompareObject() {
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public void setName(String name) {
                }

                @Override
                public int compareTo(@NonNull Object o) {
                    return -1;
                }

                @Override
                public Event getEvent() {
                    return null;
                }

                @Override
                public boolean equals(@Nullable Object obj) {
                    return obj instanceof FeedCard.FeedCardObject && ((FeedCard.FeedCardObject) obj).getCompareType() == getCompareType();
                }
            }, controller);
            filterCard.setExpanded(controller.pendingReopenFilter);
            controller.pendingReopenFilter = false;
            feedCardList.add(filterCard);

            ObjectId filterUser = Settings.FEED_FILTER_USER.get();

            retrieveEvents(retrievedEvents -> {

                boolean doneToday = false;
                boolean doneDayDiff = false;
                boolean doneWeek = false;
                boolean doneWeekDiff = false;
                List<String> doneMonths = new ArrayList<>();
                Calendar cal = Calendar.getInstance();
                Date now = new Date();

                for (Event event : retrievedEvents) {
                    if (filterUser != null && !filterUser.equals(event.getAssignedTo())) {
                        continue;
                    }
                    if (event.isLate()) {
                        if (!Settings.FEED_SHOW_LATE.<Boolean>get())
                            continue;
                    } else {
                        if (!Settings.FEED_SHOW_ON_TIME.<Boolean>get())
                            continue;
                    }
                    if ((event.getDateCompleted() != null ? event.getDateCompleted() : event.getDueDate()) != null) {
                        cal.setTime(event.getDateCompleted() != null ? event.getDateCompleted() : event.getDueDate());
                        if (controller.showingPastEvents) {
                            cal.set(Calendar.HOUR_OF_DAY, 23);
                            cal.set(Calendar.MINUTE, 59);
                            cal.set(Calendar.SECOND, 59);
                            cal.set(Calendar.MILLISECOND, 999);
                        } else {
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                        }
                        Date date = cal.getTime();
                        // if date occurs today
                        if (occursInSameCalendarField(date, now, Calendar.YEAR, 0)) {
                            if (occursInSameCalendarField(date, now, Calendar.DAY_OF_YEAR, 0)) {
                                if (!doneToday) {
                                    feedCardList.add(
                                            new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, controller));
                                    doneToday = true;
                                }
                            } else if (occursInSameCalendarField(date, now, Calendar.WEEK_OF_YEAR, 0)) {
                                // if date occurs yesterday/tomorrow (within this week)
                                if (occursInSameCalendarField(date, now, Calendar.DAY_OF_YEAR, 1)) {
                                    if (!doneDayDiff) {
                                        feedCardList.add(
                                                new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, controller));
                                        doneDayDiff = true;
                                    }
                                    // if date just occurs this week
                                } else {
                                    if (!doneWeek) {
                                        feedCardList.add(
                                                new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, controller));
                                        doneWeek = true;
                                    }
                                }
                            } else if (occursInSameCalendarField(date, now, Calendar.WEEK_OF_YEAR, 1)) {
                                if (!doneWeekDiff) {
                                    feedCardList.add(
                                            new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, controller));
                                    doneWeekDiff = true;
                                }
                            }
                        } else {
                            cal.setTime(date);
                            int month = cal.get(Calendar.MONTH);
                            int year = cal.get(Calendar.YEAR);
                            if (!doneMonths.contains(month + ":" + year)) {
                                feedCardList.add(
                                        new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, controller));
                                doneMonths.add(month + ":" + year);
                            }
                        }
                    }
                    feedCardList.add(new FeedCard(FeedCard.FeedCardType.BUBBLE, new FeedCard.FeedCardObject() {
                        @Override
                        public Date getDueDate() {
                            return event.getDueDate();
                        }

                        @Override
                        public Date getDateCompleted() {
                            return event.getDateCompleted();
                        }

                        @Override
                        public void getPerson(Consumer<User> consumer) {
                            Session.getSession().getDatabase().getUser(event.getAssignedTo(), consumer);
                        }

                        @Override
                        public Event getEvent() {
                            return event;
                        }

                        @Override
                        public int getCompareType() {
                            return DATE;
                        }

                        @Override
                        public java.lang.Comparable getCompareObject() {
                            return event.getDateCompleted() != null ? event.getDateCompleted() : event.getDueDate();
                        }

                        @Override
                        public String getName() {
                            return event.getName();
                        }

                        @Override
                        public void setName(String name) {
                            event.setName(name);
                        }

                        @Override
                        public boolean equals(@Nullable Object obj) {
                            return obj instanceof Event && obj.equals(event);
                        }
                    }, controller));
                }
                if (controller.showingPastEvents) {
                    feedCardList.sort((o1, o2) -> -o1.compareTo(o2));
                } else {
                    feedCardList.sort(Comparable::compareTo);
                }

                refreshing = false;
                consumer.accept(null);
            });
            return null;
        }

        private void retrieveEvents(Consumer<List<Event>> consumer) {
            Session.getSession().getDatabase().getAllEventsFromHouse(Settings.OPEN_HOUSE.get(), events -> {
                Date now = new Date();
                consumer.accept(events.stream()
                        .filter(event -> controller.showingPastEvents
                                ? event.getDueDate().before(now) : event.getDueDate().after(now))
                        .collect(Collectors.toList()));
            });
        }

        private boolean occursInSameCalendarField(Date a, Date b, int field, int fieldTolerance) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(a);
            int fieldVal = cal.get(field);
            int year = cal.get(Calendar.YEAR);
            cal.setTime(b);
            int otherFieldVal = cal.get(field);
            return year == cal.get(Calendar.YEAR)
                    && fieldVal >= otherFieldVal - fieldTolerance && fieldVal <= otherFieldVal + fieldTolerance;
        }
    }

    private static String getDateName(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        cal.setTime(date);
        boolean sameYear = year == cal.get(Calendar.YEAR);
        String yearString = sameYear ? "" : " " + cal.get(Calendar.YEAR);
        int suppliedDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        if (sameYear && suppliedDayOfYear == dayOfYear) {
            return "today";
        }
        if (suppliedDayOfYear == dayOfYear - 1) {
            return "yesterday";
        }
        if (suppliedDayOfYear == dayOfYear + 1) {
            return "tomorrow";
        }
        int suppliedWeekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
        if (sameYear && suppliedWeekOfYear == weekOfYear) {
            return "this week";
        }
        if (suppliedWeekOfYear == weekOfYear - 1) {
            return "last week";
        }
        if (suppliedWeekOfYear == weekOfYear + 1) {
            return "next week";
        }
        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + yearString;
    }

    private void invokeEventServiceLogic() {
        Log.d(TAG, "Manually invoking EventService's Logic");
        eventServiceLogic = EventService.manualInvoke(producedException -> {
            recyclerView.setVisibility(View.VISIBLE);
            Settings.EVENT_SERVICE_DUTIES_ARE_PRISTINE.set(true);
            eventServiceLogic = null;
            updateInfo();
        });
        loadingPanel.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        loadingText.setText("Searching for events to generate...\nThis might take a minute");
        eventServiceLogic.doInBackground(Session.getSession());
    }

    private interface FeedCardDateObject extends FeedCard.FeedCardObject {

        Date getDate();

        @Override
        default Date getDueDate() {
            return getDate();
        }

        @Override
        default Date getDateCompleted() {
            return getDate();
        }

        @Override
        default void getPerson(Consumer<User> consumer) {
            consumer.accept(null);
        }

        @Override
        default int getCompareType() {
            return DATE;
        }

        @Override
        default java.lang.Comparable getCompareObject() {
            return getDate();
        }

        @Override
        default String getName() {
            return getDateName(getDate());
        }

        @Override
        default void setName(String name) {
        }

        @Override
        default Event getEvent() {
            return null;
        }
    }
}
