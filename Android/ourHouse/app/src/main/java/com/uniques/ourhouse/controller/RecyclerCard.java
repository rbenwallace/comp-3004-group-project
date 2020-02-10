package com.uniques.ourhouse.controller;

import android.view.View;

import androidx.cardview.widget.CardView;

public interface RecyclerCard {

    void attachLayoutViews(View layout, CardView cv);

    void updateInfo();
}
