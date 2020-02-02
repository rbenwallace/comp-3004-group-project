package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;

import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.SettingsFragment;

public class CreateHouseCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public CreateHouseCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(SettingsFragment.TAG, "Add Fee Clicked");


    }

    @Override
    public void updateInfo() {

    }
}
