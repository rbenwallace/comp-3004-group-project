package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.PerformanceFragment;
import com.uniques.ourhouse.fragment.ScreenMonthFragment;


public class ScreenMonthCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private String month;
    private String year;
    private TextView calculateTitle;

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
        calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(month + " : " + year);
        Log.d(ScreenMonthFragment.TAG, "Screen Month Clicked");

        Button viewAmountPaid = (Button) view.findViewById(R.id.viewAmountPaid);
        Button viewPerformance = (Button) view.findViewById(R.id.viewPerformance);

        viewAmountPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(AmountPaidFragment.TAG), month, year);
            }
        });
        viewPerformance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(5);
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG), month, year);
            }
        });
    }

    @Override
    public void acceptArguments(Object... args) {
        month = (String) args[0];
        year = (String) args[1];
    }

    @Override
    public void updateInfo() {

    }
}
