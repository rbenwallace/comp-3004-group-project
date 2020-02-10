package com.uniques.ourhouse.controller;

import android.view.View;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.util.RecyclerCtrl;
import com.uniques.ourhouse.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        observableCards.clear();
        List<FeedCard> homeCardList = new ArrayList<>();
        Util.initFeedCardList(homeCardList, Arrays.asList(Event.testEvents()), activity);
        Util.sortList(homeCardList);
        observableCards.addAll(homeCardList);

//        observableCards.add(0, new HomeHeaderCard());
        cardsAdapter.notifyDataSetChanged();
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
                    return R.layout.content_card_feed_event;
                }
                return -1;
            }
        });
    }
}
