package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
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
import com.uniques.ourhouse.model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class ScreenMonthCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int thisMonth;
    private int thisYear;
    private int month;
    private int year;
    private String strMonth;
    private String strYear;
    private TextView calculateTitle;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    //for jon
    HashMap<User, Float> points;
    HashMap<User, Float> amounts;

    public ScreenMonthCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void init(View view) {
        points = new HashMap<>();
        amounts = new HashMap<>();
        strMonth = months[month];
        calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(strMonth + " : " + year);
        Log.d(ScreenMonthFragment.TAG, "Screen Month Clicked");

        Button viewAmountPaid = (Button) view.findViewById(R.id.viewAmountPaid);
        Button viewPerformance = (Button) view.findViewById(R.id.viewPerformance);
        Button statsBack = (Button) view.findViewById(R.id.statsBack);

        viewAmountPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                activity.pushFragment(FragmentId.GET(AmountPaidFragment.TAG), month, year);
            }
        });
        viewPerformance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG), month, year);
            }
        });
        statsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                activity.pushFragment(FragmentId.GET(CalculateAmountToPayFragment.TAG), thisMonth, thisYear);
            }
        });
    }

    @Override
    public void acceptArguments(Object... args) {
        thisMonth = Integer.parseInt(String.valueOf(args[0]));
        thisYear = Integer.parseInt(String.valueOf(args[1]));
        month = Integer.parseInt(String.valueOf(args[2]));
        year = Integer.parseInt(String.valueOf(args[3]));;
    }

    @Override
    public void updateInfo() {

    }
}
