package com.uniques.ourhouse.controller;

import android.view.View;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeedCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public List<RecyclerCard> observableCards;
    private RecyclerAdapter cardsAdapter;

    public FeedCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        observableCards = new ArrayList<>();
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

    public void setCardsAdapter(RecyclerAdapter cardsAdapter) {
        cardsAdapter.setCardViewSelector(new RecyclerAdapter.CardViewSelector() {
            @Override
            public int getItemViewType(RecyclerCard card) {
                return 0;
            }

            @Override
            public int getViewLayoutId(int itemViewType) {
                switch (itemViewType) {
                    case 0:
                        return R.layout.content_card_feed_event;
                    default:
                        return -1;
                }
            }

            @Override
            public int getCardViewId(int itemViewType) {
                return R.id.home_cardview_container;
            }
        });
        this.cardsAdapter = cardsAdapter;
    }
}
