package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.PerformanceFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;

import org.bson.types.ObjectId;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class AmountPaidCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int month, doneGettingUsers;
    private int year;
    private String strMonth;
    private TextView calculateTitle, amountview, gatheringInfo;
    private HashMap<ObjectId, Float> userAmountPaid;
    private HashMap<ObjectId, Float> userPerformance;
    private HashMap<ObjectId, Integer> userTasksCompleted;
    private ArrayList<String> userFees;
    private ArrayList<User> userArray;
    private Consumer<User> filler;
    private ArrayList<Float> floatArray;
    private boolean recalculate;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private String amount = "";
    private float count = (float)0.5;
    private int counter = 0;

    public AmountPaidCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public void init(View view) {
        userArray = new ArrayList<>();
        floatArray = new ArrayList<>();
        gatheringInfo = view.findViewById(R.id.gatheringUsers);
        if(recalculate){
            gatheringInfo.setVisibility(View.VISIBLE);
            DatabaseLink myDatabase = Session.getSession().getDatabase();
            ObjectId houseId = Settings.OPEN_HOUSE.get();
            ObjectId userId = Session.getSession().getLoggedInUserId();
            myDatabase.getHouse(houseId, house -> {
                house.populateStats(year, month, userId);
                userAmountPaid = house.getUserAmountPaid();
                userPerformance = house.getUserPoints();
                userTasksCompleted = house.getTasksCompleted();
                userFees = house.getUserFees();
                gatheringUsers(view);
            });
        }
        else {
            gatheringUsers(view);
        }
    }

    private void doneCalculatingScreen(View view) {
        strMonth = months[month];
        BarChart barChart;
        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);
        amountview = (TextView) view.findViewById(R.id.amount);
        barChart = (BarChart) view.findViewById(R.id.idBarChart);
        leftButton.setVisibility(View.GONE);
        calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(strMonth + " : " + year);
        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(5);
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG), month, year, userAmountPaid, userPerformance, userTasksCompleted, userFees);
            }
        });
        float count = (float)0.5;
        Iterator<Map.Entry<ObjectId, Float>> it = userAmountPaid.entrySet().iterator();
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> list_x_axis_name = new ArrayList<>();
        int curUser = 0;
        for(int i = 0; i < userArray.size(); i++){
            Log.d("test", userArray.get(curUser).getFirstName());
            list_x_axis_name.add(userArray.get(curUser).getFirstName());
            amount += userArray.get(curUser).getFirstName() + ": " + floatArray.get(curUser) + "\n";
            entries.add(new BarEntry(count, floatArray.get(curUser), userArray.get(curUser)));
            count += 1;
            curUser++;
        }
        amountview.setText(amount);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setAvoidFirstLastClipping(true);
        barChart.getXAxis().setCenterAxisLabels(true);
        xAxis.setGranularity(1f);
        barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(list_x_axis_name));

        BarDataSet bardataset = new BarDataSet(entries, "label");
        bardataset.setDrawValues(false);

        BarData data = new BarData(bardataset);
        barChart.setData(data);

        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setAvoidFirstLastClipping(true);
        barChart.getXAxis().setCenterAxisLabels(true);
        xAxis.setGranularity(1f);
        barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(list_x_axis_name));

        data.setBarWidth(0.6f); //how thick
        barChart.setData(data);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true); //make x-axis fit exactly all bars
        barChart.setHighlightFullBarEnabled(true);
        barChart.invalidate(); //refresh
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);
    }

    @Override
    public void acceptArguments(Object... args) {
        month = Integer.parseInt(String.valueOf(args[0]));
        year = Integer.parseInt(String.valueOf(args[1]));;
        userAmountPaid = (HashMap<ObjectId, Float>) args[2];
        userPerformance = (HashMap<ObjectId, Float>) args[3];
        userTasksCompleted = (HashMap<ObjectId, Integer>) args[4];
        userFees = (ArrayList<String>) args[5];
        recalculate = (Boolean) args[6];
    }

    @Override
    public void updateInfo() {

    }

    public void gatheringUsers(View view){
        if (!userAmountPaid.isEmpty()) doneCalculatingScreen(view);
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
                floatArray.add(pair.getValue());
                myDatabase.getUser(pair.getKey(), filler);
            }
        };
        Map.Entry<ObjectId, Float> pair = (Map.Entry<ObjectId, Float>) it.next();
        floatArray.add(pair.getValue());
        myDatabase.getUser(pair.getKey(), filler);
    }
}
