package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;

import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;

public class AddTaskCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public AddTaskCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(AddTaskFragment.TAG, "Add Fee Clicked");
    }

    @Override
    public void updateInfo() {

    }
}