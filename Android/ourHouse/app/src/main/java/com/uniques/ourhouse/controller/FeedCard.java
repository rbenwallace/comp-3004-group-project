package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import com.uniques.ourhouse.R;
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
        this.cv = layout.findViewById(R.id.home_cardview_container);
        txtTitle = layout.findViewById(R.id.home_cardview_title);
        txtSubtitle = layout.findViewById(R.id.home_cardview_subtitle);
        txtDate = layout.findViewById(R.id.home_cardview_date);

        layout.setOnClickListener(v -> handleClick());

        if (type == CardType.NORMAL)
            return;

        int bgColour = 0;
        switch (type) {
            case TEST:
                //TODO bgcolour
//                bgColour = R.color.colorPriorityHigh;
                break;
            case DELIVERABLE:
//                bgColour = R.color.colorPriorityMedium;
                break;
        }
        cv.setBackgroundColor(cv.getContext().getColor(R.color.colorInverse));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateInfo() {
        txtTitle.setText(object.getFancyName());

        if (hue != null) cv.setCardBackgroundColor(hue.getLightColor());
    }

    private void handleClick() {
    }
}
