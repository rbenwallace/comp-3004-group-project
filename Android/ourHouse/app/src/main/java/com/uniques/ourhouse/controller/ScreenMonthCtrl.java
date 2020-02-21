package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;

import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.ScreenMonthFragment;

public class ScreenMonthCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public ScreenMonthCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(ScreenMonthFragment.TAG, "Screen Month Clicked");
    }

    @Override
    public void updateInfo() {

    }
}
