package com.uniques.ourhouse.controller;

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

import java.util.ArrayList;

public class AmountPaidCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public AmountPaidCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    public void init(View view) {
        BarChart barChart;

        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);

        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(AmountPaidFragment.TAG));
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(5);
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG));
            }
        });

        //Number under graph
        //replace with database money
        //loop through database and grab all database#
        String amount = "";
        TextView amountview = (TextView) view.findViewById(R.id.amount);
        amountview.setText(amount);


        barChart = (BarChart) view.findViewById(R.id.idBarChart);

        ArrayList<BarEntry> entries = new ArrayList<>();
        //for (int i=0; i<database size; i++)
        //don't forget to floor/ceiling number in database#
        //entries.add(new BarEntry(i, database#[i], databasename[i]));
        entries.add(new BarEntry(0, 2352, "Name 1"));
        entries.add(new BarEntry(1, 3341, "Name 2"));
        entries.add(new BarEntry(2, 2734, "Name 3"));

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
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {

    }
}
