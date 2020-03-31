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
import com.github.mikephil.charting.utils.ColorTemplate;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.FeeListFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.graphics.Color.BLACK;

public class PerformanceCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int month;
    private int year;
    private String strMonth;
    private TextView calculateTitle;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    //for jon
    HashMap<User, Float> points;
    HashMap<User, Float> amounts;

    public PerformanceCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public void init(View view) {
        points = new HashMap<>();
        amounts = new HashMap<>();
        strMonth = months[month];
        calculateTitle = (TextView) view.findViewById(R.id.calculate_date);
        calculateTitle.setText(strMonth + " : " + year);

        PieChart pieChart;
        BarChart barChart;

        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);

        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(AmountPaidFragment.TAG), month, year);
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(5);
                activity.pushFragment(FragmentId.GET(CalculateAmountToPayFragment.TAG), month, year);
            }
        });

        //testing
        User ben = new User("ben", "a", "1");
        User seb = new User("seb", "b", "2");
        User jon = new User("jon", "c", "3");
        User victor = new User("victor", "d", "4");
        points.put(jon, (float)32);
        points.put(victor, (float)23);
        points.put(ben, (float)11);
        points.put(seb, (float)34);



        pieChart = (PieChart) view.findViewById(R.id.idPieChart);
        pieChart.setUsePercentValues(true);

        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleRadius(0f);

        int total = 0;
        if (!points.isEmpty()) {
            Iterator<Map.Entry<User, Float>> it = points.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry<User, Float> pair = (Map.Entry<User, Float>) it.next();
                total += pair.getValue();
            }
        }

        List<PieEntry> value = new ArrayList<>();
        if (!points.isEmpty()) {
            int count = 0;
            Iterator<Map.Entry<User, Float>> it = points.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry<User, Float> pair = (Map.Entry<User, Float>) it.next();
                value.add(new PieEntry((pair.getValue()/total), pair.getKey().getFirstName()));
                count += 1;
            }
        }

        PieDataSet pieDataSet = new PieDataSet(value, "");
        pieDataSet.setDrawValues(false);
        pieChart.setEntryLabelColor(BLACK);
        pieChart.setDrawEntryLabels(true);
        pieChart.getDescription().setEnabled(false);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);

        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        pieChart.animateXY(1500, 1500);

        //pieChart.setTransparentCircleAlpha(0);

        //addDataSet(pieChart);

        barChart = (BarChart) view.findViewById(R.id.idBarChart);

        ArrayList<BarEntry> entries = new ArrayList<>();


        ArrayList<String> list_x_axis_name = new ArrayList<>();

        if (!points.isEmpty()) {
            int count = 0;
            Iterator<Map.Entry<User, Float>> it = points.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry<User, Float> pair = (Map.Entry<User, Float>) it.next();
                entries.add(new BarEntry(count, pair.getValue(), pair.getKey().getFirstName()));
                list_x_axis_name.add(pair.getKey().getFirstName());
                Log.d("test", pair.getKey().getFirstName());
                count += 1;
            }
        }

        for(int i=0; i<3; i++) {
            Log.d(AmountPaidFragment.TAG, list_x_axis_name.get(i));
        }

        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setAvoidFirstLastClipping(true);
        barChart.getXAxis().setCenterAxisLabels(true);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(list_x_axis_name));

        BarDataSet bardataset = new BarDataSet(entries, "");
        bardataset.setDrawValues(true);

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
        barChart.getXAxis().setDrawLabels(true);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true); //make x-axis fit exactly all bars
        barChart.setHighlightFullBarEnabled(false);
        barChart.invalidate(); //refresh
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);
    }

    @Override
    public void acceptArguments(Object... args) {
        month = Integer.parseInt(String.valueOf(args[0]));
        year = Integer.parseInt(String.valueOf(args[1]));;
    }

    @Override
    public void updateInfo() {

    }
}
