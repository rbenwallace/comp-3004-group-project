package com.uniques.ourhouse.controller;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;
import com.uniques.ourhouse.session.Session;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static com.uniques.ourhouse.controller.LoginCtrl.regEx;

public class SignUpCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private static Button login;
    private static CheckBox terms_conditions;
    public static StitchAppClient client;
    TextView informError;
    public static final String phone_regEx = "^\\+?[0-9\\/.()-]{9,}$";

    public SignUpCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        client = Stitch.getAppClient("ourhouse-notdj");
        EditText editFirstName = view.findViewById(R.id.firstName);
        EditText editLastName = view.findViewById(R.id.lastName);
        EditText editEmail = view.findViewById(R.id.userEmailId);
        EditText editPassword = view.findViewById(R.id.password);
        informError = view.findViewById(R.id.info_user);
        EditText editConfirmPassword = view.findViewById(R.id.confirmPassword);
        CheckBox agreement = view.findViewById(R.id.terms_conditions);

        Button backTOLogin = view.findViewById(R.id.already_user);
        Button signUpBtn = view.findViewById(R.id.signUpBtn);
        Log.d(SignUpFragment.TAG, "onCreatedView: Started");
        backTOLogin.setOnClickListener(view1 -> {
            activity.popFragment(FragmentId.GET(SignUpFragment.TAG));
        });
        signUpBtn.setOnClickListener(view12 -> {
            if (!editPassword.getText().toString().equals(editConfirmPassword.getText().toString())) {
                Log.d(SignUpFragment.TAG, "Password match failed");
                Toast.makeText(activity, "Passwords don't match", Toast.LENGTH_LONG);
                return;
            }
            if (!checkValidation(view, editEmail.getText().toString().trim(), editPassword.getText().toString().trim())) {
                Log.d(SignUpFragment.TAG, "Invalid Credentials");
                return;
            }
            if (!agreement.isChecked()) {
                Log.d(SignUpFragment.TAG, "Terms and Conditions unchecked");
                Toast.makeText(activity, "unchecked", Toast.LENGTH_SHORT).show();
                return;
            }
            Session.getSession().getSecureAuthenticator().registerUser(editEmail.getText().toString().trim(), editPassword.getText().toString().trim(), exception -> {
                if (exception == null) {
                    Log.d("stitch", "Successfully sent account confirmation email");

                    Toast.makeText(activity, "Confirm your email " + editFirstName.getText().toString(), Toast.LENGTH_SHORT).show();
                    ArrayList<String> transferInfoArray = new ArrayList<>();
                    transferInfoArray.add(editFirstName.getText().toString().trim());
                    transferInfoArray.add(editLastName.getText().toString().trim());
                    transferInfoArray.add(editEmail.getText().toString().trim());
                    SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(transferInfoArray);
                    editor.putString("loginData", json);
                    editor.apply();
                    activity.pushFragment(FragmentId.GET(LoginFragment.TAG));
                } else {
                    Log.e("stitch", "Error registering new user:", exception);
                    Toast.makeText(activity, exception.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {

    }

    private boolean checkValidation(View view, String email, String password) {
        // Check pattern for email id
        Pattern p = Pattern.compile(regEx);

        Matcher m = p.matcher(email);

        // Check for both field is empty or not
        if (email.equals("") || email.length() == 0
                || password.equals("") || password.length() == 0) {
            informError.setText("Error with Credentials!");
            return false;

        }
        // Check if email id is valid or not
        else if (!m.find()) {
            informError.setText("Error with Credentials!");
            return false;
        }

        // Else do login and do your stuff
        else
            return true;


    }
}