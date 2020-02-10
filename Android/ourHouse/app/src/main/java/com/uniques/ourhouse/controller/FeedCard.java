package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.Hue;
import com.uniques.ourhouse.util.Observable;

import androidx.cardview.widget.CardView;

public final class FeedCard implements RecyclerCard, Comparable {

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public Comparable getCompareObject() {
        return object;
    }

    public enum CardType {
        NORMAL, TEST, DELIVERABLE
    }

    private CardType type;
    private Observable object;
    private Hue hue;
    private FragmentActivity activity;
    private CardView cv;
    private TextView txtTitle;
    private TextView txtSubtitle;
    private TextView txtDate;

    public FeedCard(CardType type, Observable object, Hue hue, FragmentActivity activity) {
        this.type = type;
        this.object = object;
        this.hue = hue;
        this.activity = activity;
    }

    @Override
    public void attachLayoutViews(View layout, CardView cv) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateInfo() {
    }

    private void handleClick() {
    }
}
