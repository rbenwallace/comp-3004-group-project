package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.CreateHouseFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.JoinHouseFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;

public class HousesCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public HousesCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(SettingsFragment.TAG, "Add Fee Clicked");

        Button createHouse = view.findViewById(R.id.createHouseBtn);
        Button joinHouse = view.findViewById(R.id.joinHouseBtn);

        createHouse.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Going to CreateHouseFragment", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(3);
            activity.pushFragment(FragmentId.GET(CreateHouseFragment.TAG));
        });
        joinHouse.setOnClickListener(view2 -> {
            Toast.makeText(activity, "Going to JoinHouseFragment", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(3);
            activity.pushFragment(FragmentId.GET(JoinHouseFragment.TAG));
        });
    }

    @Override
    public void updateInfo() {

    }
}
