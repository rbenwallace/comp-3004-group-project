package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class CalculateAmountToPayCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int month;
    private int chosenYearInt;
    private int year;
    private int newMonth;
    private int newYear;
    private Button selectedMonth;
    private Button selectedYear;
    private boolean menuOpen = false;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    //For jon
    HashMap<User, Float> points;
    HashMap<User, Float> amounts;

    public CalculateAmountToPayCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    private int findMonth(String str){
        for(int i = 0; i < 12; i++){
            if(months[i].equals(str)){
                return i;
            }
        }
        Log.d(CalculateAmountToPayFragment.TAG, "Failed to get Month");
        return 0;
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public void init(View view) {
        points = new HashMap<>();
        amounts = new HashMap<>();
        newMonth = month;
        newYear = year;
        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);
        Button currentMonth = (Button) view.findViewById(R.id.month0);
        Button pastMonthButton01 = (Button) view.findViewById(R.id.month01);
        Button pastMonthButton02 = (Button) view.findViewById(R.id.month02);
        Button pastMonthButton03 = (Button) view.findViewById(R.id.month03);
        Button pastMonthButton04 = (Button) view.findViewById(R.id.month04);
        Button pastMonthButton05 = (Button) view.findViewById(R.id.month05);
        Button pastMonthButton06 = (Button) view.findViewById(R.id.month06);
        Button pastMonthButton07 = (Button) view.findViewById(R.id.month07);
        Button pastMonthButton08 = (Button) view.findViewById(R.id.month08);
        Button pastMonthButton09 = (Button) view.findViewById(R.id.month09);
        Button pastMonthButton10 = (Button) view.findViewById(R.id.month10);
        Button pastMonthButton11 = (Button) view.findViewById(R.id.month11);

        Button currentYear = (Button) view.findViewById(R.id.year0);
        Button pastYearButton01 = (Button) view.findViewById(R.id.year01);
        Button pastYearButton02 = (Button) view.findViewById(R.id.year02);
        Button pastYearButton03 = (Button) view.findViewById(R.id.year03);

        Button calculateButton = (Button) view.findViewById(R.id.calculate);

        selectedYear = currentYear;

        Button[] monthButtons = {currentMonth, pastMonthButton01, pastMonthButton02, pastMonthButton03, pastMonthButton04, pastMonthButton05, pastMonthButton06, pastMonthButton07, pastMonthButton08, pastMonthButton09, pastMonthButton10, pastMonthButton11};
        Button[] yearButtons = {currentYear, pastYearButton01, pastYearButton02, pastYearButton03};

        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") String temp = new SimpleDateFormat("yyyy").format(date);
        chosenYearInt = Integer.parseInt(temp);
        int currentMonthInt = date.getMonth();
        selectedMonth = currentMonth;
        for(int i = 0; i < 12; i++){
            if(currentMonthInt == month){
                selectedMonth = monthButtons[i];
            }
            monthButtons[i].setText(months[currentMonthInt]);
            if(currentMonthInt == 0){
                currentMonthInt = 11;
            }
            else{
                currentMonthInt -= 1;
            }
        }
        for(int i = 0; i < 4; i++){
            if(chosenYearInt == year){
                selectedYear = yearButtons[i];
            }
            yearButtons[i].setText(String.valueOf(chosenYearInt));
            chosenYearInt -= 1;
        }
        selectedYear.setBackgroundResource(R.drawable.selected_item);
        selectedMonth.setBackgroundResource(R.drawable.selected_item);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String month = selectedMonth.getText().toString();
                //String year = selectedYear.getText().toString();
                activity.pushFragment(FragmentId.GET(ScreenMonthFragment.TAG), newMonth, newYear);
            }
        });

        currentMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(currentMonth.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                currentMonth.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = currentMonth;
            }
        });

        pastMonthButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton01.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton01.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton01;
            }
        });

        pastMonthButton02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton02.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton02.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton02;
            }
        });

        pastMonthButton03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton03.getText().toString());
                System.out.println("wallace : " + newMonth + " " + pastMonthButton03.getText().toString() + " " + months[11]);
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton03.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton03;
            }
        });

        pastMonthButton04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton04.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton04.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton04;
            }
        });

        pastMonthButton05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton05.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton05.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton05;
            }
        });

        pastMonthButton06.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton06.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton06.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton06;
            }
        });

        pastMonthButton07.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton07.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton07.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton07;
            }
        });

        pastMonthButton08.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton08.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton08.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton08;
            }
        });

        pastMonthButton09.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton09.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton09.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton09;
            }
        });

        pastMonthButton10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton10.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton10.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton10;
            }
        });

        pastMonthButton11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newMonth = findMonth(pastMonthButton11.getText().toString());
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton11.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton11;
            }
        });

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
                //activity.pushFragment(FragmentId.GET(CalculateAmountToPayFragment.TAG), month, year);
            }
        });

        currentYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newYear = Integer.parseInt(currentYear.getText().toString());
                selectedYear.setBackgroundResource(R.drawable.loginbutton_selector);
                currentYear.setBackgroundResource(R.drawable.selected_item);
                selectedYear = currentYear;
                
                currentYear.setVisibility(View.INVISIBLE);
                pastYearButton01.setVisibility(View.INVISIBLE);
                pastYearButton02.setVisibility(View.INVISIBLE);
                pastYearButton03.setVisibility(View.INVISIBLE);
                menuOpen = false;

            }
        });

        pastYearButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newYear = Integer.parseInt(pastYearButton01.getText().toString());
                selectedYear.setBackgroundResource(R.drawable.loginbutton_selector);
                pastYearButton01.setBackgroundResource(R.drawable.selected_item);
                selectedYear = pastYearButton01;

                currentYear.setVisibility(View.INVISIBLE);
                pastYearButton01.setVisibility(View.INVISIBLE);
                pastYearButton02.setVisibility(View.INVISIBLE);
                pastYearButton03.setVisibility(View.INVISIBLE);
                menuOpen = false;

            }
        });

        pastYearButton02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newYear = Integer.parseInt(pastYearButton02.getText().toString());
                selectedYear.setBackgroundResource(R.drawable.loginbutton_selector);
                pastYearButton02.setBackgroundResource(R.drawable.selected_item);
                selectedYear = pastYearButton02;

                currentYear.setVisibility(View.INVISIBLE);
                pastYearButton01.setVisibility(View.INVISIBLE);
                pastYearButton02.setVisibility(View.INVISIBLE);
                pastYearButton03.setVisibility(View.INVISIBLE);
                menuOpen = false;

            }
        });

        pastYearButton03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newYear = Integer.parseInt(pastYearButton03.getText().toString());
                selectedYear.setBackgroundResource(R.drawable.loginbutton_selector);
                pastYearButton03.setBackgroundResource(R.drawable.selected_item);
                selectedYear = pastYearButton03;

                currentYear.setVisibility(View.INVISIBLE);
                pastYearButton01.setVisibility(View.INVISIBLE);
                pastYearButton02.setVisibility(View.INVISIBLE);
                pastYearButton03.setVisibility(View.INVISIBLE);
                menuOpen = false;

            }
        });

        ImageButton hamburgerMenu = (ImageButton) activity.findViewById(R.id.HamburgerMenu);
        hamburgerMenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(menuOpen){
                    currentYear.setVisibility(View.INVISIBLE);
                    pastYearButton01.setVisibility(View.INVISIBLE);
                    pastYearButton02.setVisibility(View.INVISIBLE);
                    pastYearButton03.setVisibility(View.INVISIBLE);
                    menuOpen = false;
                }
                else{
                    currentYear.setVisibility(View.VISIBLE);
                    pastYearButton01.setVisibility(View.VISIBLE);
                    pastYearButton02.setVisibility(View.VISIBLE);
                    pastYearButton03.setVisibility(View.VISIBLE);
                    menuOpen = true;
                }
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

    }
}
