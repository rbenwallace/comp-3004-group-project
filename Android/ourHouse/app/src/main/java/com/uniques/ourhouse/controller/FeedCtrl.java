package com.uniques.ourhouse.controller;

import android.view.View;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.RecyclerCtrl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;

public class FeedCtrl implements FragmentCtrl, RecyclerCtrl<FeedCard> {
    private FragmentActivity activity;

    public List<FeedCard> observableCards;
    private RecyclerAdapter<FeedCard> cardsAdapter;

    public FeedCtrl(FragmentActivity activity) {
        this.activity = activity;
        observableCards = new ArrayList<>();
    }

    @Override
    public void init(View view) {
    }

    @Override
    public void updateInfo() {
        List<FeedCard> homeCardList = new ArrayList<>();
        for (Event event : Event.testEvents()) {
            homeCardList.add(new FeedCard(FeedCard.FeedCardType.EVENT, new FeedCard.FeedCardObject() {
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
            }, activity));
        }
        homeCardList.sort(Comparable::compareTo);
//        Util.sortList(homeCardList, false);

        System.out.println("FEED CTRL " + observableCards.size() + " " + homeCardList.size());

        for (int i = observableCards.size() - 1; i >= 0; --i) {
            if (!homeCardList.contains(observableCards.get(i))) {
                observableCards.remove(i);
                cardsAdapter.notifyItemRemoved(i);
                i--;
            }
        }

        outer: for (int i = 0; i < homeCardList.size(); ++i) {
            FeedCard feedCard = homeCardList.get(i);
            if (!observableCards.contains(feedCard)) {
                for (int j = 0; j < observableCards.size(); ++j) {
                    if (observableCards.get(j).compareTo(feedCard) > 0) {
                        observableCards.add(j, feedCard);
                        cardsAdapter.notifyItemInserted(j);
                        continue outer;
                    }
                }
                observableCards.add(feedCard);
                cardsAdapter.notifyItemInserted(observableCards.size() - 1);
            }
        }

//        cardsAdapter.notifyDataSetChanged();

        System.out.println("FEED CTRL " + observableCards.size() + " " + homeCardList.size());
    }


    @Override
    public void setRecyclerAdapter(RecyclerAdapter<FeedCard> recyclerAdapter) {
        this.cardsAdapter = recyclerAdapter;
        recyclerAdapter.setViewSelector(new RecyclerAdapter.ViewSelector() {
            @Override
            public int getItemViewType(RecyclerCard card) {
                return 0;
            }

            @Override
            public int getViewLayoutId(int itemViewType) {
                if (itemViewType == 0) {
                    return R.layout.content_card_feed;
                }
                return -1;
            }
        });
    }
}
