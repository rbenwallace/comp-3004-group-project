package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.SignUpFragment;
import com.uniques.ourhouse.session.Session;

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

        EditText editFullName = view.findViewById(R.id.fullName);
        EditText editEmail = view.findViewById(R.id.userEmailId);
        EditText editPhoneNumber = view.findViewById(R.id.mobileNumber);
        EditText editPassword = view.findViewById(R.id.password);
        EditText editConfirmPassword = view.findViewById(R.id.confirmPassword);

        Button backTOLogin = view.findViewById(R.id.already_user);
        Button signUpBtn = view.findViewById(R.id.signUpBtn);
        Log.d(SignUpFragment.TAG, "onCreatedView: Started");
        backTOLogin.setOnClickListener(view1 -> {
            activity.popFragment(FragmentId.GET(SignUpFragment.TAG));
        });
        signUpBtn.setOnClickListener(view12 -> {
            if (!editPassword.getText().toString().equals(editConfirmPassword.getText().toString())) {
                Log.d(SignUpFragment.TAG, "Password match failed");
                Snackbar.make(view, "Passwords don't match", BaseTransientBottomBar.LENGTH_LONG);
                return;
            }
            if (Session.getSession().getSecureAuthenticator()
                    .newUser(editFullName.getText().toString(), editEmail.getText().toString(),
                            editPhoneNumber.getText().toString(), editPassword.getText().toString())) {
                Log.d(SignUpFragment.TAG, "Signup success");
                activity.startActivity(new Intent(activity, MainActivity.class));
            } else {
                Log.d(SignUpFragment.TAG, "Signup failed");
            }
        });
    }

    @Override
    public void updateInfo() {

    }
}
