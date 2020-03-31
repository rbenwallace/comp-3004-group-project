package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.Comparable;

public final class FeeListItemCard implements RecyclerCard, Comparable {

    private String object;
    private CardView cv;
    private TextView txtTitle;

    public FeeListItemCard(String object) {
        this.object = object;
    }

    @Override
    public void attachLayoutViews(View layout, CardView cv) {
        this.cv = layout.findViewById(R.id.fee_list_item_card);
        txtTitle = layout.findViewById(R.id.fee_list_item_txtTitle);

        layout.setOnClickListener(v -> handleClick());
    }

    public String getObject(){ return object; }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateInfo() {
        txtTitle.setText(object);
    }

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public Comparable getCompareObject() {
        return new Comparable() {
            @Override
            public int getCompareType() {
                return STRING;
            }

            @Override
            public java.lang.Comparable getCompareObject() {
                return object;
            }
        };
    }

    private void handleClick() {
    }
}
