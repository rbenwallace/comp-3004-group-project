package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddFeeFragment;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;

public class ManageCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private Button addFeeButton;
    private Button addTaskButton;
    private Button settingsButton;

    public ManageCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        addFeeButton = view.findViewById(R.id.manage_btnAddFee);
        addTaskButton = view.findViewById(R.id.manage_btnAddTask);
        settingsButton = view.findViewById(R.id.manage_btnSettings);

        Log.d(ManageFragment.TAG, "Controller init()");
        addFeeButton.setOnClickListener(view1 -> {
            activity.pushFragment(FragmentId.GET(AddFeeFragment.TAG));
        });
        addTaskButton.setOnClickListener(view12 -> {
            activity.pushFragment(FragmentId.GET(AddTaskFragment.TAG));
        });
        settingsButton.setOnClickListener(view13 -> {
            activity.pushFragment(FragmentId.GET(SettingsFragment.TAG));
        });

    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {

    }
}
