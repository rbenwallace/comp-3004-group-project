package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.SettingsFragment;

public class SettingsCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public SettingsCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(SettingsFragment.TAG, "Add Fee Clicked");

        Button manageBackButton = view.findViewById(R.id.manage_back);
        manageBackButton.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Back", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(3);
            activity.popFragment(FragmentId.GET(SettingsFragment.TAG));
        });
    }

    @Override
    public void updateInfo() {

    }
}
