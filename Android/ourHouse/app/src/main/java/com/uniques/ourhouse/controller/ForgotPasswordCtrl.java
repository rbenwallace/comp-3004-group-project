package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;

public class ForgotPasswordCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public ForgotPasswordCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Button backTOLogin = (Button) view.findViewById(R.id.backToLoginBtn);
        Log.d(ForgotPasswordFragment.TAG, "onCreatedView: Started");
        backTOLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "Going to LoginFragment", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(0);
                activity.popFragment(FragmentId.GET(ForgotPasswordFragment.TAG));
            }
        });
    }

    @Override
    public void updateInfo() {

    }
}
