package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.FeeListFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.PerformanceFragment;
import com.uniques.ourhouse.fragment.ScreenMonthFragment;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;

import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ScreenMonthCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int month;
    private int year;
    private ObjectId houseId;
    private ObjectId userId;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private String strMonth;
    private TextView calculateTitle;
    private boolean changed;
    private HashMap<ObjectId, Float> userAmountPaid;
    private HashMap<ObjectId, Float> userPerformance;
    private HashMap<ObjectId, Integer> userTasksCompleted;
    private ArrayList<String> userFees;
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
        houseId = Settings.OPEN_HOUSE.get();
        userId = Session.getSession().getLoggedInUserId();
        points = new HashMap<>();
        amounts = new HashMap<>();
        strMonth = months[month];
        calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(strMonth + " : " + year);
        Log.d(ScreenMonthFragment.TAG, "Screen Month Clicked");

        Button viewAmountPaid = (Button) view.findViewById(R.id.viewAmountPaid);
        Button viewPerformance = (Button) view.findViewById(R.id.viewPerformance);
        Button viewMonthlyFees = (Button) view.findViewById(R.id.monthly_fees);
        Button statsBack = (Button) view.findViewById(R.id.statsBack);
        TextView calculateBody = (TextView) view.findViewById(R.id.textView2);

        int total = 0;
        String amount = "";

        //testing
        User jon = new User();
        User seb = new User();
        User victor = new User();
        User ben = new User();
        jon.setFirstName("jon");
        seb.setFirstName("seb");
        victor.setFirstName("victor");
        ben.setFirstName("ben");
        amounts.put(jon, (float)2251.23);
        amounts.put(victor, (float)3315.23);
        amounts.put(ben, (float)200);
        amounts.put(seb, (float)1242.32);

        if (!amounts.isEmpty()) {
            Iterator<Map.Entry<User, Float>> it = amounts.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry<User, Float> pair = (Map.Entry<User, Float>) it.next();
                total += pair.getValue();
            }
        }

        if (!amounts.isEmpty()) {
            Iterator<Map.Entry<User, Float>> it = amounts.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry<User, Float> pair = (Map.Entry<User, Float>) it.next();
                if (total/amounts.size() - pair.getValue() > 0) {
                    amount += pair.getKey().getFirstName() + " owes: " + (total/amounts.size() - pair.getValue()) + "\n";
                }
                else if (total/amounts.size() - pair.getValue() == 0) {
                    amount += pair.getKey().getFirstName() + " owes: 0";
                }
                else {
                    amount += pair.getKey().getFirstName() + " is owed: " + (pair.getValue() - total/amounts.size()) + "\n";
                }
            }
        }

        calculateBody.setText(amount);

        if(changed) {
            myDatabase.getHouse(houseId, house -> {
                house.populateStats(year, month, userId);
                userAmountPaid = house.getUserAmountPaid();
                userPerformance = house.getUserPoints();
                userTasksCompleted = house.getTasksCompleted();
                userFees = house.getUserFees();
            });
        }

        viewAmountPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                activity.pushFragment(FragmentId.GET(AmountPaidFragment.TAG), month, year, userAmountPaid, userPerformance, userTasksCompleted, userFees, false);
            }
        });
        viewPerformance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                activity.pushFragment(FragmentId.GET(AmountPaidFragment.TAG), month, year, userAmountPaid, userPerformance, userTasksCompleted, userFees, false);
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG), month, year, userAmountPaid, userPerformance, userTasksCompleted, userFees);
            }
        });
        viewMonthlyFees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                activity.pushFragment(FragmentId.GET(FeeListFragment.TAG), month, year, userFees);
            }
        });
        statsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                activity.popFragment(FragmentId.GET(ScreenMonthFragment.TAG));            }
        });
    }

    @Override
    public void acceptArguments(Object... args) {
        month = Integer.parseInt(String.valueOf(args[0]));
        year = Integer.parseInt(String.valueOf(args[1]));;
        userAmountPaid = (HashMap<ObjectId, Float>) args[2];
        userPerformance = (HashMap<ObjectId, Float>) args[3];
        userTasksCompleted = (HashMap<ObjectId, Integer>) args[4];
        userFees = (ArrayList<String>) args[5];
        changed = (boolean) args[6];
    }

    @Override
    public void updateInfo() {

    }
}
