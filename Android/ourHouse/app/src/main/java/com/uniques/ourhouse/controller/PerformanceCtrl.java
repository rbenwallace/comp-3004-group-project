package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.PerformanceFragment;
import com.uniques.ourhouse.model.User;

import java.util.ArrayList;
import java.util.List;

public class PerformanceCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public PerformanceCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    public void init(View view) {
        PieChart pieChart;
        BarChart barChart;

        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);

        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "Left", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(AmountPaidFragment.TAG));
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "Right", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(5);
                activity.pushFragment(FragmentId.GET(CalculateAmountToPayFragment.TAG));
            }
        });

        //testing
        int test = 0;

        User ben = new User("ben", "a", "1", 5);
        User seb = new User("seb", "b", "2", 6);
        User jon = new User("jon", "c", "3", 2);


        pieChart = (PieChart) view.findViewById(R.id.idPieChart);
        pieChart.setUsePercentValues(true);

        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleRadius(0f);

        List<PieEntry> value = new ArrayList<>();
        //would need to loop through get data, make it percentage
        value.add(new PieEntry(40f, "Ben"));
        value.add(new PieEntry(60f, "Seb"));

        PieDataSet pieDataSet = new PieDataSet(value, "performance");
        pieChart.getDescription().setEnabled(false);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);

        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        pieChart.animateXY(1500, 1500);

        //pieChart.setTransparentCircleAlpha(0);

        //addDataSet(pieChart);

        barChart = (BarChart) view.findViewById(R.id.idBarChart);

        ArrayList<BarEntry> entries = new ArrayList<>();
        //for (int i=0; i<database size; i++)
        //don't forget to floor/ceiling number in database#
        //entries.add(new BarEntry(i, database#[i], databasename[i]));
        test += seb.getPerformance();
        test += seb.getPerformance();


        entries.add(new BarEntry(0, 50, "Name 1"));
        entries.add(new BarEntry(1, 20, "Name 2"));
        entries.add(new BarEntry(2, 100, "Name 3"));
        entries.add(new BarEntry(3, 44, "Name 4"));
        entries.add(new BarEntry(4, 56, "Name 5"));
        entries.add(new BarEntry(5, 20, ben.getFirstName()));

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
    }

    @Override
    public void updateInfo() {

    }
}
