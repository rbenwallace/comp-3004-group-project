package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public class LoginCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int showPassword = 1;
    private EditText email;
    private EditText password;
    private StitchUser currentUser;
    public static StitchAppClient client;


    public static final String regEx = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public LoginCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        client = Stitch.getAppClient("ourhouse-notdj");
        currentUser = client.getAuth().getUser();
        if (currentUser != null) {
            Toast.makeText(activity, currentUser.getId().toString(), Toast.LENGTH_LONG).show();
            activity.startActivity(new Intent(activity, MainActivity.class));
        }
        email = view.findViewById(R.id.login_emailid);
        password = view.findViewById(R.id.login_password);

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
            if (checkValidation()) {
                new getLoginTask(view).execute(email.getText().toString().trim(), password.getText().toString().trim());
            } else {
                Log.d(LoginFragment.TAG, "Login failed");
                Snackbar.make(view, "Invalid email or password", BaseTransientBottomBar.LENGTH_LONG);
            }
        });


    }

    private void changePasswordDisplay() {
        if (showPassword == 0) {
            Toast.makeText(activity, "show password", Toast.LENGTH_LONG).show();
            showPassword = 1;
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.password, 0, R.drawable.password_eye_closed, 0);
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            Toast.makeText(activity, "dont show password", Toast.LENGTH_LONG).show();
            showPassword = 0;
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.password, 0, R.drawable.password_eye_open, 0);
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }

    private boolean checkValidation() {
        // Get email id and password
        String getEmailId = email.getText().toString();
        String getPassword = password.getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(regEx);

        Matcher m = p.matcher(getEmailId);

        // Check for both field is empty or not
        if (getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0) {
            Toast.makeText(activity, "Enter valid credentials", Toast.LENGTH_LONG).show();
            return false;

        }
        // Check if email id is valid or not
        else if (!m.find()) {
            Toast.makeText(activity, "Your Email is invalid", Toast.LENGTH_LONG).show();
            return false;
        }
        // Else do login and do your stuff
        else
            return true;


    }

    @Override
    public void updateInfo() {

    }

    private class getLoginTask extends AsyncTask<String, Void, Void> {
        private ProgressBar pd;
        private byte statusCode;
        private View view;


        public getLoginTask(View view) {
            this.view = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RelativeLayout layout = view.findViewById(R.id.login_root_display);
            pd = new ProgressBar(activity, null, android.R.attr.progressBarStyleLarge);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300, 300);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(pd, params);
            pd.setVisibility(View.VISIBLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                UserPasswordCredential credential = new UserPasswordCredential(params[0], params[1]);
                client.getAuth().loginWithCredential(credential).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            statusCode = 1;
                            onPostExecute();
                        } else {
                            statusCode = 0;
                            onPostExecute();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("stitch-auth", "Authentication Failed!");
            }
            return null;
        }

        protected void onPostExecute() {
            if (statusCode == 1) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Log.d("stitch-auth", "Authentication Successful.");
                Toast.makeText(activity, "OurHouse welcomes you!", Toast.LENGTH_SHORT).show();
                activity.startActivity(new Intent(activity, MainActivity.class));
                pd.setVisibility(View.GONE);
            } else {
                Log.e("stitch-auth", "Authentication Failed.");
                pd.setVisibility(View.GONE);
                Toast.makeText(activity, "You ain't Real cuz", Toast.LENGTH_SHORT).show();
                password.setText("");
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }
}

