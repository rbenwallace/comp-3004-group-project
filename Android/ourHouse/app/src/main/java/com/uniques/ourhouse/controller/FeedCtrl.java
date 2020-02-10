package com.uniques.ourhouse.controller;

import android.view.View;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
<<<<<<< HEAD
import com.uniques.ourhouse.util.RecyclerCtrl;
=======
>>>>>>> master
import com.uniques.ourhouse.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

<<<<<<< HEAD
public class FeedCtrl implements FragmentCtrl, RecyclerCtrl<FeedCard> {
    private FragmentActivity activity;

    public List<FeedCard> observableCards;
    private RecyclerAdapter<FeedCard> cardsAdapter;

    public FeedCtrl(FragmentActivity activity) {
        this.activity = activity;
        observableCards = new ArrayList<>();
=======
public class FeedCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public List<RecyclerCard> observableCards;
    private RecyclerAdapter cardsAdapter;

    public FeedCtrl(FragmentActivity activity) {
        this.activity = activity;
>>>>>>> master
    }

    @Override
    public void init(View view) {
<<<<<<< HEAD
=======
        observableCards = new ArrayList<>();
>>>>>>> master
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

<<<<<<< HEAD
    @Override
    public void setRecyclerAdapter(RecyclerAdapter<FeedCard> recyclerAdapter) {
        this.cardsAdapter = recyclerAdapter;
        recyclerAdapter.setViewSelector(new RecyclerAdapter.ViewSelector() {
=======
    public void setCardsAdapter(RecyclerAdapter cardsAdapter) {
        cardsAdapter.setCardViewSelector(new RecyclerAdapter.CardViewSelector() {
>>>>>>> master
            @Override
            public int getItemViewType(RecyclerCard card) {
                return 0;
            }

            @Override
            public int getViewLayoutId(int itemViewType) {
<<<<<<< HEAD
                if (itemViewType == 0) {
                    return R.layout.content_card_feed_event;
                }
                return -1;
            }
        });
=======
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
>>>>>>> master
    }
}
