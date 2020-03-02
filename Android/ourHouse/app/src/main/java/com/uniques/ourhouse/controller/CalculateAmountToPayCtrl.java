package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.PerformanceFragment;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class CalculateAmountToPayCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private Button selectedMonth;
    private String[] months = {"December", "November", "October", "September", "August", "July", "June", "May", "April", "March", "February", "January"};

    public CalculateAmountToPayCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    public void init(View view) {
        ImageButton hamburgerMenu;

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

        selectedMonth = currentMonth;

        Button currentYear = (Button) view.findViewById(R.id.year0);
        Button pastYearButton01 = (Button) view.findViewById(R.id.year01);
        Button pastYearButton02 = (Button) view.findViewById(R.id.year02);
        Button pastYearButton03 = (Button) view.findViewById(R.id.year03);

        Button[] monthButtons = {currentMonth, pastMonthButton01, pastMonthButton02, pastMonthButton03, pastMonthButton04, pastMonthButton05, pastMonthButton06, pastMonthButton07, pastMonthButton08, pastMonthButton09, pastMonthButton10, pastMonthButton11};
        Button[] yearButtons = {currentYear, pastYearButton01, pastYearButton02, pastYearButton03};

        Date date = Calendar.getInstance().getTime();
        int chosenYearInt = date.getYear();
        int currentMonthInt = date.getMonth();
        for(int i = 0; i < 12; i++){
            monthButtons[i].setText(months[currentMonthInt]);
            currentMonthInt = (currentMonthInt + 1) % 12;
        }
        /*for(int i = 0; i < 4; i++){
            yearButtons[i].setText(String.valueOf(chosenYearInt));
            chosenYearInt -= 1;
        }
        currentYear.setBackgroundResource(R.drawable.selected_item);*/
        currentMonth.setBackgroundResource(R.drawable.selected_item);

        currentMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                currentMonth.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = currentMonth;
            }
        });

        pastMonthButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton01.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton01;
            }
        });

        pastMonthButton02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton02.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton02;
            }
        });

        pastMonthButton03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton03.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton03;
            }
        });

        pastMonthButton04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton04.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton04;
            }
        });

        pastMonthButton05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton05.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton05;
            }
        });

        pastMonthButton06.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton06.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton06;
            }
        });

        pastMonthButton07.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton07.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton07;
            }
        });

        pastMonthButton08.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton08.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton08;
            }
        });

        pastMonthButton09.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton09.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton09;
            }
        });

        pastMonthButton10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMonth.setBackgroundResource(R.drawable.loginbutton_selector);
                pastMonthButton10.setBackgroundResource(R.drawable.selected_item);
                selectedMonth = pastMonthButton10;
            }
        });

        pastMonthButton11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG));
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(5);
                activity.pushFragment(FragmentId.GET(CalculateAmountToPayFragment.TAG));
            }
        });

        hamburgerMenu = (ImageButton) view.findViewById(R.id.HamburgerMenu);
        hamburgerMenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater)
                        activity.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.year, null);
                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.LEFT, 85, -10);
                // dismiss the popup window when touched
            }
        });



    }

    @Override
    public void updateInfo() {

    }
}
