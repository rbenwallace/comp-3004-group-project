package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.ScreenMonthFragment;


public class ScreenMonthCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private String month;
    private String year;

    public ScreenMonthCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    public ScreenMonthCtrl(FragmentActivity activity, String month, String year) {
        this.activity = activity;
        this.month = month;
        this.year = year;
        //Log.d("wallace: ", month);
        //Log.d("wallace: ", year);
    }

    @Override
    public void init(View view) {
        TextView calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(month + " : " + year);
        Log.d(ScreenMonthFragment.TAG, "Screen Month Clicked");
    }

    @Override
    public void updateInfo() {

    }
}
