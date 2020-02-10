package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.SignUpFragment;

public class SignUpCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private static EditText fullName, emailId, mobileNumber,
            password, confirmPassword;
    private static Button login;
    private static Button signUpButton;
    private static CheckBox terms_conditions;

    public SignUpCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Button backTOLogin = view.findViewById(R.id.already_user);
        Button signUpBtn = view.findViewById(R.id.signUpBtn);
        Log.d(SignUpFragment.TAG, "onCreatedView: Started");
        backTOLogin.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Going to LoginFragment", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(0);
            activity.popFragment(FragmentId.GET(SignUpFragment.TAG));
        });
        signUpBtn.setOnClickListener(view12 -> {
            //TODO CHECK FOR VERIFICATION
            Toast.makeText(activity, "Signed Up", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT Main Activity
        });
    }

    @Override
    public void updateInfo() {

    }
}
