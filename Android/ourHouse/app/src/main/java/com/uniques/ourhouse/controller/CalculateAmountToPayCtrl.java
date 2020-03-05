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

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class CalculateAmountToPayCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public CalculateAmountToPayCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    public void init(View view) {
        ImageButton hamburgermenu;

        Button leftButton = (Button) view.findViewById(R.id.left_button);
        Button rightButton = (Button) view.findViewById(R.id.right_button);

        Log.d(AmountPaidFragment.TAG, "onCreatedView: Amount Paid");
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "Left", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(PerformanceFragment.TAG));
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

        hamburgermenu = (ImageButton) view.findViewById(R.id.HamburgerMenu);
        hamburgermenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Toast.makeText(activity, "Year", Toast.LENGTH_LONG).show();
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
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                // dismiss the popup window when touched
            }
        });



    }

    @Override
    public void acceptArguments(Object... args) {
    }

    @Override
    public void updateInfo() {

    }
}
