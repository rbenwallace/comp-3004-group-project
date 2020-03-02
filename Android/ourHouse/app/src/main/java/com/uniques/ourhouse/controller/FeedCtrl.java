package com.uniques.ourhouse.controller;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.RecyclerCtrl;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FeedCtrl implements FragmentCtrl, RecyclerCtrl<FeedCard> {
    FragmentActivity activity;
    private TextView txtPastEvents;
    private TextView txtUpcomingEvents;
    private boolean showingPastEvents;
    private boolean pendingReopenFilter;

    public List<FeedCard> observableCards;
    private RecyclerAdapter<FeedCard> cardsAdapter;

    public FeedCtrl(FragmentActivity activity) {
        this.activity = activity;
        showingPastEvents = true;
        observableCards = new ArrayList<>();
    }

    @Override
    public void init(View view) {
        txtPastEvents = view.findViewById(R.id.feed_txtPastEvents);
        txtUpcomingEvents = view.findViewById(R.id.feed_txtUpcomingEvents);

        txtPastEvents.setOnClickListener(v -> {
            if (!showingPastEvents) {
                showingPastEvents = true;
                updateInfo();
            }
        });
        txtUpcomingEvents.setOnClickListener(v -> {
            if (showingPastEvents) {
                showingPastEvents = false;
                updateInfo();
            }
        });
    }

    void updateInfoAndReopenFilter() {
        pendingReopenFilter = true;
        updateInfo();
    }

    @Override
    public void updateInfo() {
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
            public User getPerson() {
                return null;
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
        }, this);
        filterCard.setExpanded(pendingReopenFilter);
        pendingReopenFilter = false;
        feedCardList.add(filterCard);

        boolean doneToday = false;
        boolean doneDayDiff = false;
        boolean doneWeek = false;
        boolean doneWeekDiff = false;
        List<String> doneMonths = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Date now = new Date();

        ObjectId filterUser = Settings.FEED_FILTER_USER.get();

        Event[] eventArr = showingPastEvents ? Event.testEvents() : new Event[0];
        for (Event event : eventArr) {
            if (filterUser != null && !filterUser.equals(event.getAssignedTo().getId())) {
                continue;
            }
            if (event.isLate()) {
                if (!Settings.FEED_SHOW_LATE.<Boolean>get())
                    continue;
            } else {
                if (!Settings.FEED_SHOW_ON_TIME.<Boolean>get())
                    continue;
            }
            if (event.getCompareObject() instanceof Date) {
                cal.setTime((Date) event.getCompareObject());
                if (showingPastEvents) {
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
                if (occursSameField(date, now, Calendar.DAY_OF_YEAR, 0)) {
                    if (!doneToday) {
                        feedCardList.add(
                                new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, this));
                        doneToday = true;
                    }
                } else if (occursSameField(date, now, Calendar.WEEK_OF_YEAR, 0)) {
                    // if date occurs yesterday/tomorrow (within this week)
                    if (occursSameField(date, now, Calendar.DAY_OF_YEAR, 1)) {
                        if (!doneDayDiff) {
                            feedCardList.add(
                                    new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, this));
                            doneDayDiff = true;
                        }
                        // if date just occurs this week
                    } else {
                        if (!doneWeek) {
                            feedCardList.add(
                                    new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, this));
                            doneWeek = true;
                        }
                    }
                } else if (occursSameField(date, now, Calendar.WEEK_OF_YEAR, 1)) {
                    if (!doneWeekDiff) {
                        feedCardList.add(
                                new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, this));
                        doneWeekDiff = true;
                    }
                } else {
                    cal.setTime(date);
                    int month = cal.get(Calendar.MONTH);
                    int year = cal.get(Calendar.YEAR);
                    if (!doneMonths.contains(month + ":" + year)) {
                        feedCardList.add(
                                new FeedCard(FeedCard.FeedCardType.DATE, (FeedCardDateObject) () -> date, this));
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
                public User getPerson() {
                    return event.getAssignedTo();
                }

                @Override
                public Event getEvent() {
                    return event;
                }

                @Override
                public int getCompareType() {
                    return event.getCompareType();
                }

                @Override
                public java.lang.Comparable getCompareObject() {
                    return event.getCompareObject();
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
            }, this));
        }
        if (showingPastEvents) {
            feedCardList.sort((o1, o2) -> -o1.compareTo(o2));
        } else {
            feedCardList.sort(Comparable::compareTo);
        }
//        Util.sortList(feedCardList, false);

        System.out.println("FEED CTRL " + observableCards.size() + " " + feedCardList.size());

        for (int i = observableCards.size() - 1; i >= 0; --i) {
            if (!feedCardList.contains(observableCards.get(i))) {
                observableCards.remove(i);
                cardsAdapter.notifyItemRemoved(i);
            }
        }

//        outer:
        for (int i = 0; i < feedCardList.size(); ++i) {
            FeedCard feedCard = feedCardList.get(i);
            if (!observableCards.contains(feedCard)) {
//                for (int j = 0; j < observableCards.size(); ++j) {
//                    if (observableCards.get(j).compareTo(feedCard) > 0) {
//                        observableCards.add(j, feedCard);
//                        cardsAdapter.notifyItemInserted(j);
//                        continue outer;
//                    }
//                }
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

//        cardsAdapter.notifyDataSetChanged();

//        System.out.println("FEED CTRL " + observableCards.size() + " " + feedCardList.size());
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

    private boolean occursSameField(Date a, Date b, int field, int fieldTolerance) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(a);
        int fieldVal = cal.get(field);
        int year = cal.get(Calendar.YEAR);
        cal.setTime(b);
        int otherFieldVal = cal.get(field);
        return year == cal.get(Calendar.YEAR)
                && fieldVal >= otherFieldVal - fieldTolerance && fieldVal <= otherFieldVal + fieldTolerance;
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
        default User getPerson() {
            return null;
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
