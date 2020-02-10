package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddFeeFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;

public class AddFeeCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public AddFeeCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(AddFeeFragment.TAG, "Add Fee Clicked");

        Button manageBackButton = view.findViewById(R.id.manage_back);
        manageBackButton.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Back", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(3);
            activity.popFragment(FragmentId.GET(AddFeeFragment.TAG));
        });
    }

    @Override
    public void updateInfo() {

    }
}
