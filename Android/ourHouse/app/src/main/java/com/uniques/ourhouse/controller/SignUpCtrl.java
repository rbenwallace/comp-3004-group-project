package com.uniques.ourhouse.controller;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;
import com.uniques.ourhouse.session.MongoDB;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;
import static com.uniques.ourhouse.controller.LoginCtrl.regEx;

public class SignUpCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private MongoDB myDatabase;
    private static Button login;
    private static CheckBox terms_conditions;
    public static StitchAppClient client;
    public static final String phone_regEx = "^\\+?[0-9\\/.()-]{9,}$";

    public SignUpCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        client = Stitch.getAppClient("ourhouse-notdj");
        myDatabase = new MongoDB();
        EditText editFirstName = view.findViewById(R.id.firstName);
        EditText editLastName = view.findViewById(R.id.lastName);
        EditText editEmail = view.findViewById(R.id.userEmailId);
        EditText editPassword = view.findViewById(R.id.password);
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
            if (!checkValidation(view, editEmail.getText().toString().trim(), editPassword.getText().toString().trim())){
                Log.d(SignUpFragment.TAG, "Invalid Credentials");
                return;
            }
            if(!agreement.isChecked()){
                Log.d(SignUpFragment.TAG, "Terms and Conditions unchecked");
                Toast.makeText(activity, "unchecked", Toast.LENGTH_SHORT).show();
                return;
            }
            UserPasswordAuthProviderClient emailPassClient = client.getAuth().getProviderClient(UserPasswordAuthProviderClient.factory);

            emailPassClient.registerWithEmail(editEmail.getText().toString().trim(), editPassword.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull final Task<Void> task) {
                    if (task.isSuccessful()) {
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
                        Log.e("stitch", "Error registering new user:", task.getException());
                        Toast.makeText(activity, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
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
        Pattern ph = Pattern.compile(phone_regEx);

        Matcher m = p.matcher(email);

        // Check for both field is empty or not
        if (email.equals("") || email.length() == 0
                || password.equals("") || password.length() == 0) {
            Toast.makeText(activity, "Error with Credentials!", Toast.LENGTH_LONG);
            return false;

        }
        // Check if email id is valid or not
        else if (!m.find()) {
            Toast.makeText(activity, "Emails Trash", Toast.LENGTH_LONG);
            return false;
        }

        // Else do login and do your stuff
        else
            return true;


    }
}