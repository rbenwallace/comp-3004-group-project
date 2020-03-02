package com.uniques.ourhouse.controller;

import android.content.SharedPreferences;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.MongoDB;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

import org.bson.types.ObjectId;

import static android.content.Context.MODE_PRIVATE;

public class LoginCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int showPassword = 1;
    private EditText email;
    private EditText password;
    private StitchUser currentUser;
    private String firstNameCurUser;
    private String lastNameCurUser;
    private ArrayList<String> transferedArrayFromSignUp;
    public MongoDB myDatabase = new MongoDB();


    public static final String regEx = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public LoginCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        if (myDatabase.autoAuth()) {
            //If there is a user but its not currently set up with shared pref, set it up
            if (MongoDB.getCurrentLocalUser(activity) == null) {
                Consumer<User> myUser = user -> {
                    if (user != null) {
                        myDatabase.setLocalUser(user, activity);
                    }
                };
                myDatabase.getUser(new ObjectId(myDatabase.getAuth().getUser().getId()), myUser);
            }
            else {
                activity.pushFragment(FragmentId.GET(MyHousesFragment.TAG));
            }
        }
        email = view.findViewById(R.id.login_emailid);
        password = view.findViewById(R.id.login_password);
        Button backToSignUp = view.findViewById(R.id.createAccount);
        Button loginBtn = view.findViewById(R.id.loginBtn);
        Button forgetPassword = view.findViewById(R.id.forgot_password);
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("loginData", null);
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            transferedArrayFromSignUp = gson.fromJson(json, type);
        } catch (Error e) {
            Log.d("SharedPref", "SharedPref loginData not set up");
        }

        if (transferedArrayFromSignUp != null) {
            Toast.makeText(activity, transferedArrayFromSignUp.get(0), Toast.LENGTH_LONG).show();
            firstNameCurUser = transferedArrayFromSignUp.get(0);
            lastNameCurUser = transferedArrayFromSignUp.get(1);
            if (transferedArrayFromSignUp.size() > 2)
                email.setText(transferedArrayFromSignUp.get(2));
        }

        backToSignUp.setOnClickListener(view1 -> {
            activity.pushFragment(FragmentId.GET(SignUpFragment.TAG));
        });
        forgetPassword.setOnClickListener(view12 -> {
            activity.pushFragment(FragmentId.GET(ForgotPasswordFragment.TAG));
        });
        loginBtn.setOnClickListener(view14 -> {
            if (checkValidation()) {
                Log.d("sitch-auth", email.getText().toString().trim() + ", " + password.getText().toString().trim());
                new getLoginTask(view).execute(email.getText().toString().trim(), password.getText().toString().trim());
            } else {
                Log.d(LoginFragment.TAG, "Login failed");
                Snackbar.make(view, "Invalid email or password", BaseTransientBottomBar.LENGTH_LONG);
            }
        });
    }
    private void changePasswordDisplay () {
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

    private boolean checkValidation () {
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
    public void updateInfo () {

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
                myDatabase.getAuth().loginWithCredential(credential).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            statusCode = 1;
                            onPostExecute(task, params[0], params[1]);
                        } else {
                            statusCode = 0;
                            onPostExecute(task, params[0], params[1]);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("stitch-auth", "Authentication Failed!" + e);
            }
            return null;
        }

        protected void onPostExecute(com.google.android.gms.tasks.Task<StitchUser> loggedInUser, String email, String passwd) {
            if (statusCode == 1) {
                User newUser = new User(new ObjectId(loggedInUser.getResult().getId()), firstNameCurUser, lastNameCurUser, email, 0);
                Consumer<User> userConsumer = user -> {
                    if(user != null){
                        myDatabase.setLocalUser(user, activity);
                        //TODO SET LOCAL DATA TO THIS GUY
                        Toast.makeText(activity, "OurHouse welcomes you!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        myDatabase.addMyUser(newUser, activity);
                    }
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    activity.pushFragment(FragmentId.GET(MyHousesFragment.TAG));
                    pd.setVisibility(View.GONE);
                };
                myDatabase.getUser(newUser.getId(), userConsumer);
            } else {
                Log.e("stitch-auth", "Authentication Failed.");
                pd.setVisibility(View.GONE);
                Toast.makeText(activity, "Confirm you email first before logging in", Toast.LENGTH_LONG).show();
                password.setText("");
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }
}

