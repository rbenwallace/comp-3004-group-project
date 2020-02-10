package com.uniques.ourhouse.controller;

import android.view.View;
import android.widget.TextView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.Observable;

import androidx.cardview.widget.CardView;

public final class TaskRotationCard implements RecyclerCard, Comparable {

    private Observable object;
    private CardView cv;
    private TextView txtTitle;

    public TaskRotationCard(Observable object) {
        this.object = object;
    }

    @Override
    public void attachLayoutViews(View layout, CardView cv) {
        this.cv = layout.findViewById(R.id.roommate_card);
        txtTitle = layout.findViewById(R.id.roommate_txtTitle);

        layout.setOnClickListener(v -> handleClick());
    }

    @Override
    public void updateInfo() {
        txtTitle.setText(object.getName());
    }

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public Comparable getCompareObject() {
        return object;
    }

    private void handleClick() {
    }
}
