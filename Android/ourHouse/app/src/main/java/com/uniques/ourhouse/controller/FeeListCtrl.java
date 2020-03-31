package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.PerformanceFragment;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.ReadOnlyNameable;
import com.uniques.ourhouse.util.RecyclerCtrl;

import java.util.ArrayList;
import java.util.List;


public class FeeListCtrl implements RecyclerCtrl<FeeListItemCard>, FragmentCtrl {
    private FragmentActivity activity;
    private int month;
    private int year;
    private String string;

    public List<FeeListItemCard> observableCards;
    private RecyclerAdapter<FeeListItemCard> recyclerAdapter;

    public FeeListCtrl(FragmentActivity activity) {
        this.activity = activity;
        observableCards = new ArrayList<>();
    }


    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public void init(View view) {

        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);

        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG), month, year);
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
    }


    @Override
    public void acceptArguments(Object... args) {
        month = Integer.parseInt(String.valueOf(args[0]));
        year = Integer.parseInt(String.valueOf(args[1]));;
    }

    @Override
    public void updateInfo() {

        ArrayList<String> feeList = new ArrayList<>();
        
        observableCards.clear();

        for(String text: feeList)
        {
            observableCards.add(new FeeListItemCard(text));
        }
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void setRecyclerAdapter(RecyclerAdapter<FeeListItemCard> recyclerAdapter) {
        this.recyclerAdapter = recyclerAdapter;
    }

    private class ObservableString implements Observable, ReadOnlyNameable {
        private String string;

        private ObservableString(String string) {
            this.string = string;
        }

        @Override
        public String getName() {
            return string;
        }

        @Override
        public int getCompareType() {
            return Observable.STRING;
        }

        @Override
        public Comparable getCompareObject() {
            return string;
        }
    }

}
