package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;

import com.uniques.ourhouse.fragment.AddFeeFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;

public class AddFeeCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public AddFeeCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(AddFeeFragment.TAG, "Add Fee Clicked");
    }

    @Override
    public void updateInfo() {

    }
}
