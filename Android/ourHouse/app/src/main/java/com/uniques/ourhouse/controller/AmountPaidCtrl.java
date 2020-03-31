package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.PerformanceFragment;
import com.uniques.ourhouse.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AmountPaidCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int month;
    private int year;
    private String strMonth;
    private TextView calculateTitle;
    private HashMap<User, Float> userAmountPaid;
    private HashMap<User, Float> userPerformance;
    private HashMap<User, Integer> userTasksCompleted;
    private ArrayList<String> userFees;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    //for jon
    HashMap<User, Float> points = new HashMap<>();
    HashMap<User, Float> amounts = new HashMap<>();

    public AmountPaidCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public void init(View view) {
        points = new HashMap<>();
        amounts = new HashMap<>();
        strMonth = months[month];
        calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(strMonth + " : " + year);

        BarChart barChart;

        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);

        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(5);
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG), month, year);
            }
        });

        String amount = "";
        TextView amountview = (TextView) view.findViewById(R.id.amount);


        barChart = (BarChart) view.findViewById(R.id.idBarChart);

        //this is for creating a testing hashmap
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


        ArrayList<BarEntry> entries = new ArrayList<>();

        if (!amounts.isEmpty()) {
            int count = 0;
            Iterator<Map.Entry<User, Float>> it = amounts.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry<User, Float> pair = (Map.Entry<User, Float>) it.next();
                amount += pair.getKey().getFirstName() + ": " + pair.getValue() + "\n";
                entries.add(new BarEntry(count, pair.getValue(), pair.getKey()));
                count += 1;
            }
            amountview.setText(amount);
        }

        BarDataSet bardataset = new BarDataSet(entries, "label");
        bardataset.setDrawValues(false);

        BarData data = new BarData(bardataset);
        barChart.setData(data);

        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);



        //BarData data = new BarData(bars);
        data.setBarWidth(0.6f); //how thick
        barChart.setData(data);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getXAxis().setDrawLabels(false);
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

        userAmountPaid = (HashMap<User, Float>) args[2];
        System.out.println("wallace Amount Paid: " + userAmountPaid.toString());
        userPerformance = (HashMap<User, Float>) args[3];
        userTasksCompleted = (HashMap<User, Integer>) args[4];
        userFees = (ArrayList<String>) args[5];
    }

    @Override
    public void updateInfo() {

    }
}
