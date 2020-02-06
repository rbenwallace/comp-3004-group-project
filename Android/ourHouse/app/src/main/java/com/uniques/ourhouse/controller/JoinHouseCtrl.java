package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.SettingsFragment;

public class JoinHouseCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public JoinHouseCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(SettingsFragment.TAG, "Add Fee Clicked");
        Button joinHouse = view.findViewById(R.id.joinHouseBtn);

        joinHouse.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Going to Feed", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(3);
            activity.startActivity(new Intent(activity, MainActivity.class));
        });
    }

    @Override
    public void updateInfo() {

    }
}
