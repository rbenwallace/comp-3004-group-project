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
import java.util.function.Consumer;


public class ScreenMonthCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int month;
    private int year;
    private ObjectId houseId;
    private ObjectId userId;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private String strMonth;
    private TextView calculateTitle, gatheringInfo;
    private boolean changed;
    private HashMap<ObjectId, Float> userAmountPaid;
    private HashMap<ObjectId, Float> userPerformance;
    private HashMap<ObjectId, Integer> userTasksCompleted;
    private ArrayList<String> userFees;
    private ArrayList<User> userArray;
    private Consumer<User> filler;
    private ArrayList<Float> floatAmountArray;
    private boolean recalculate;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    private int total = 0;
    private String amount = "";

    public ScreenMonthCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void init(View view) {
        userArray = new ArrayList<>();
        floatAmountArray = new ArrayList<>();
        gatheringInfo = view.findViewById(R.id.gatheringUsers3);
        gatheringInfo.setVisibility(View.VISIBLE);
        DatabaseLink myDatabase = Session.getSession().getDatabase();
        ObjectId houseId = Settings.OPEN_HOUSE.get();
        Log.d("TestingStuff", "year: " + year + "month" + month);
        Log.d("TestingStuff", "House ID Init : " + houseId.toString());
        ObjectId userId = Session.getSession().getLoggedInUserId();
        myDatabase.getHouse(houseId, house -> {
            house.populateStats(year, month, userId, eventsGrabbed ->{
                userAmountPaid = house.getUserAmountPaid();
                userPerformance = house.getUserPoints();
                userTasksCompleted = house.getTasksCompleted();
                userFees = house.getUserFees();
                gatheringUsers(view);
            });
        });
    }



    private void doneCalculatingScreen(View view) {
        gatheringInfo.setVisibility(View.GONE);
        strMonth = months[month];
        calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(strMonth + " : " + year);
        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");

        Button viewAmountPaid = (Button) view.findViewById(R.id.viewAmountPaid);
        Button viewPerformance = (Button) view.findViewById(R.id.viewPerformance);
        Button viewMonthlyFees = (Button) view.findViewById(R.id.monthly_fees);
        Button statsBack = (Button) view.findViewById(R.id.statsBack);
        TextView calculateBody = (TextView) view.findViewById(R.id.textView2);

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

        for (int j = 0; j < floatAmountArray.size(); j++) {
            total += floatAmountArray.get(j);
        }
        Log.d("TestingStuff", "Uploading stuff");
        Iterator<Map.Entry<ObjectId, Float>> it = userAmountPaid.entrySet().iterator();
        int curUser = 0;
        for(int i = 0; i < userArray.size(); i++){
            System.out.println(floatAmountArray.get(curUser));
            if (total/userArray.size() - floatAmountArray.get(curUser) > 0) {
                amount += userArray.get(curUser).getFirstName() + " owes: " + (total/floatAmountArray.size() - floatAmountArray.get(curUser)) + "\n";
            }
            else if (total/userArray.size() - floatAmountArray.get(curUser) == 0) {
                amount += userArray.get(curUser).getFirstName() + " owes: 0" + "\n";
            }
            else {
                amount += userArray.get(curUser).getFirstName() + " is owed: " + (floatAmountArray.get(curUser) - total/floatAmountArray.size()) + "\n";
            }
            curUser ++;
        }
        Log.d("TestingStuff", "Amount uploading : " + amount);
        calculateBody.setText(amount);
        amount = "";
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

    public void gatheringUsers(View view){
        gatheringInfo.setVisibility(View.VISIBLE);
        if (userAmountPaid.isEmpty()) doneCalculatingScreen(view);
        float count = (float)0.5;
        Iterator<Map.Entry<ObjectId, Float>> it = userAmountPaid.entrySet().iterator();
        if (filler != null) {
            return;
        }
        if (!it.hasNext()) {
            gatheringInfo.setVisibility(View.GONE);
            doneCalculatingScreen(view);
            return;
        }
        filler = user -> {
            if (user != null) {
                userArray.add(user);
            }
            if (!it.hasNext()) {
                filler = null;
                Log.d("MyHousesCtrl", "All users are a go");
                gatheringInfo.setVisibility(View.GONE);
                doneCalculatingScreen(view);
            } else {
                Map.Entry<ObjectId, Float> pair = (Map.Entry<ObjectId, Float>) it.next();
                floatAmountArray.add(pair.getValue());
                Log.d("CheckingNames", pair.getKey().toString() + " : " + pair.getValue().toString());
                myDatabase.getUser(pair.getKey(), filler);
            }
        };
        Map.Entry<ObjectId, Float> pair = (Map.Entry<ObjectId, Float>) it.next();
        floatAmountArray.add(pair.getValue());
        Log.d("CheckingNames", pair.getKey().toString() + " : " + pair.getValue().toString());
        myDatabase.getUser(pair.getKey(), filler);
    }

}
