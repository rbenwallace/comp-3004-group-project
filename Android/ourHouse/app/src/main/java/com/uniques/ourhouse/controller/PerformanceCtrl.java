package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.FeeListFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.PerformanceFragment;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static android.graphics.Color.BLACK;

public class PerformanceCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int month;
    private int year;
    private String strMonth;
    private TextView calculateTitle, gatheringInfo;
    private HashMap<ObjectId, Float> userAmountPaid;
    private HashMap<ObjectId, Float> userPerformance;
    private HashMap<ObjectId, Integer> userTasksCompleted;
    private ArrayList<String> userFees;
    private ArrayList<User> userArray, userArray2;
    private Consumer<User> filler, filler2;
    private ArrayList<Float> floatPerformanceArray;
    private ArrayList<Integer> intTaskCompletedArray;
    private boolean recalculate;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private Float total = 0f;

    public PerformanceCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public void init(View view) {
        userArray = new ArrayList<>();
        userArray2 = new ArrayList<>();
        floatPerformanceArray = new ArrayList<>();
        intTaskCompletedArray = new ArrayList<>();
        gatheringInfo = view.findViewById(R.id.gatheringUsers2);
        if (recalculate) {
            gatheringInfo.setVisibility(View.VISIBLE);
            DatabaseLink myDatabase = Session.getSession().getDatabase();
            ObjectId houseId = Settings.OPEN_HOUSE.get();
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
        } else {
            gatheringUsers(view);
        }
    }

    private void doneCalculatingScreen(View view) {
        gatheringInfo.setVisibility(View.GONE);
        strMonth = months[month];
        BarChart barChart;
        PieChart pieChart;
        barChart = (BarChart) view.findViewById(R.id.idBarChart);
        calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(strMonth + " : " + year);
        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);

        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.popFragment(FragmentId.GET(PerformanceFragment.TAG));
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(5);
                activity.pushFragment(FragmentId.GET(CalculateAmountToPayFragment.TAG), month, year, userAmountPaid, userPerformance, userTasksCompleted, userFees);
            }
        });

        float count = (float) 0.5;
        Iterator<Map.Entry<ObjectId, Integer>> it = userTasksCompleted.entrySet().iterator();
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> list_x_axis_name = new ArrayList<>();
        int curUser = 0;
        for (int i = 0; i < userArray.size(); i++) {
            Log.d("array size", Integer.toString(i));
            list_x_axis_name.add(userArray.get(curUser).getFirstName());
            Log.d("names", userArray.get(curUser).toString());
            entries.add(new BarEntry(count, intTaskCompletedArray.get(curUser), userArray.get(curUser)));
            count += 1;
            curUser++;
        }

        curUser = 0;
        count = 0.5f;

        for (int j = 0; j < floatPerformanceArray.size(); j++) {
            total += floatPerformanceArray.get(j);
        }

        List<PieEntry> value = new ArrayList<>();
        Iterator<Map.Entry<ObjectId, Float>> it2 = userPerformance.entrySet().iterator();
        ArrayList<PieEntry> entries2 = new ArrayList<>();
        int curUser2 = 0;
        for (int i = 0; i < userArray2.size(); i++) {
            if (floatPerformanceArray.get(curUser2) != 0.0) {
                value.add(new PieEntry((float) (Math.round((floatPerformanceArray.get(curUser2) / total) * 10000) / 100), userArray2.get(curUser2).getFirstName()));
            }
            curUser2++;
        }

        curUser2 = 0;

//piechart
        pieChart = (PieChart) view.findViewById(R.id.idPieChart);
        PieDataSet pieDataSet = new PieDataSet(value, "");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.animateXY(1500, 1500);

        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleRadius(0f);

        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);

        pieChart.invalidate();


//barchart
        barChart = (BarChart) view.findViewById(R.id.idBarChart);

        BarDataSet bardataset = new BarDataSet(entries, "");
        bardataset.setDrawValues(true);

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

        //BarData data = new BarData(bars);
        data.setBarWidth(0.6f); //how thick
        barChart.setData(data);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setScaleEnabled(true);
        barChart.setFitBars(true); //make x-axis fit exactly all bars
        barChart.setHighlightFullBarEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);

        barChart.invalidate(); //refresh

    }

    @Override
    public void acceptArguments(Object... args) {
        month = Integer.parseInt(String.valueOf(args[0]));
        year = Integer.parseInt(String.valueOf(args[1]));;
        userAmountPaid = (HashMap<ObjectId, Float>) args[2];
        userPerformance = (HashMap<ObjectId, Float>) args[3];
        userTasksCompleted = (HashMap<ObjectId, Integer>) args[4];
        userFees = (ArrayList<String>) args[5];
    }

    @Override
    public void updateInfo() {

    }

    public void gatheringUsers(View view) {
        gatheringInfo.setVisibility(View.VISIBLE);
        if (userTasksCompleted.isEmpty()) doneCalculatingScreen(view);
        Iterator<Map.Entry<ObjectId, Integer>> it = userTasksCompleted.entrySet().iterator();
        if (filler != null) {
            return;
        }
        if (!it.hasNext()) {
            gatheringInfo.setVisibility(View.GONE);
            gatheringUsers2(view);
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
                gatheringUsers2(view);
            } else {
                Map.Entry<ObjectId, Integer> pair = (Map.Entry<ObjectId, Integer>) it.next();
                intTaskCompletedArray.add(pair.getValue());
                Log.d("gathering1", pair.getValue().toString() + " " + pair.getKey().toString());
                myDatabase.getUser(pair.getKey(), filler);
            }
        };
        Map.Entry<ObjectId, Integer> pair = (Map.Entry<ObjectId, Integer>) it.next();
        intTaskCompletedArray.add(pair.getValue());
        Log.d("gathering1", pair.getValue().toString() + " " + pair.getKey().toString());
        myDatabase.getUser(pair.getKey(), filler);
    }

    public void gatheringUsers2(View view) {
        if (userPerformance.isEmpty()) doneCalculatingScreen(view);
        Iterator<Map.Entry<ObjectId, Float>> it2 = userPerformance.entrySet().iterator();
        if (filler2 != null) {
            return;
        }
        if (!it2.hasNext()) {
            gatheringInfo.setVisibility(View.GONE);
            doneCalculatingScreen(view);
            return;
        }
        filler2 = user -> {
            if (user != null) {
                userArray2.add(user);
            }
            if (!it2.hasNext()) {
                filler2 = null;
                Log.d("MyHousesCtrl", "All users are a go");
                gatheringInfo.setVisibility(View.GONE);
                doneCalculatingScreen(view);
            } else {
                Map.Entry<ObjectId, Float> pair2 = (Map.Entry<ObjectId, Float>) it2.next();
                floatPerformanceArray.add(pair2.getValue());
                Log.d("gathering2", pair2.getValue().toString());
                myDatabase.getUser(pair2.getKey(), filler2);
            }
        };
        Map.Entry<ObjectId, Float> pair2 = (Map.Entry<ObjectId, Float>) it2.next();
        floatPerformanceArray.add(pair2.getValue());
        Log.d("gathering2", pair2.getValue().toString());
        myDatabase.getUser(pair2.getKey(), filler2);
    }


}
