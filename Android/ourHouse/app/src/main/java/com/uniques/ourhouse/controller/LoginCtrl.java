package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;
import com.uniques.ourhouse.session.Session;

public class LoginCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public LoginCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {

        EditText editEmail = view.findViewById(R.id.login_emailid);
        EditText editPassword = view.findViewById(R.id.login_password);

        Button backToSignUp = view.findViewById(R.id.createAccount);
        Button loginBtn = view.findViewById(R.id.loginBtn);
        Button forgetPassword = view.findViewById(R.id.forgot_password);

        backToSignUp.setOnClickListener(view1 -> {
            activity.pushFragment(FragmentId.GET(SignUpFragment.TAG));
        });
        forgetPassword.setOnClickListener(view12 -> {
            activity.pushFragment(FragmentId.GET(ForgotPasswordFragment.TAG));
        });
        loginBtn.setOnClickListener(view14 -> {
            if (Session.getSession().getSecureAuthenticator()
                    .authenticate(editEmail.getText().toString(), editPassword.getText().toString())) {
                activity.startActivity(new Intent(activity, MainActivity.class));
            } else {
                Log.d(LoginFragment.TAG, "Login failed");
                Snackbar.make(view, "Invalid email or password", BaseTransientBottomBar.LENGTH_LONG);
            }
        });
    }

    @Override
    public void updateInfo() {

    }
}
