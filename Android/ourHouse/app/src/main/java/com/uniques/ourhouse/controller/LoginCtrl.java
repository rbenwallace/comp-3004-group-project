package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.ManageFragment;

public class LoginCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public LoginCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d("titsMagee", view.toString());

        Button backToSignUp = view.findViewById(R.id.createAccount);
        Button loginBtn = view.findViewById(R.id.loginBtn);
        Button forgetPassword = view.findViewById(R.id.forgot_password);
        Button manageButton = view.findViewById(R.id.manageButton);

        Log.d("titsMagee", "onCreatedView: Started");

        backToSignUp.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Going to Sign Up", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
            //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
//            ((LS_Main) activity).setViewPager(1);
            activity.popFragment(FragmentId.GET(LoginFragment.TAG));
        });
//        backToSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getActivity(), "Going to Sign Up", Toast.LENGTH_LONG).show();
//                //TODO NAVIGATE TO NEXT FRAGMENT
//                //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
//                ((LS_Main) getActivity()).setViewPager(1);
//            }
//        });
        forgetPassword.setOnClickListener(view12 -> {
            Toast.makeText(activity, "Going to find my pw", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
            //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
//            ((LS_Main) activity).setViewPager(2);
            activity.pushFragment(FragmentId.GET(ForgotPasswordFragment.TAG));
        });
        manageButton.setOnClickListener(view13 -> {
            Toast.makeText(activity, "Going to manage", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
            //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
//            ((LS_Main) activity).setViewPager(3);
            activity.pushFragment(FragmentId.GET(ManageFragment.TAG));
        });
        loginBtn.setOnClickListener(view14 -> {
            //TODO CHECK FOR VERIFICATION
            Toast.makeText(activity, "Logging in", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT Main Activity
        });
    }

    @Override
    public void updateInfo() {

    }
}
