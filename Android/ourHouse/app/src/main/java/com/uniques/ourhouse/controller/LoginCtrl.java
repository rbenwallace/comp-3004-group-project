package com.uniques.ourhouse.controller;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private int showPassword = 1;
    private EditText email;
    private EditText password;
    public static final String regEx = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

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
        email = view.findViewById(R.id.login_emailid);
        password = view.findViewById(R.id.login_password);
        password.setTransformationMethod(PasswordTransformationMethod.getInstance());


        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        changePasswordDisplay();
                        return true;
                    }
                }
                return false;
            }
        });


        Log.d("titsMagee", "onCreatedView: Started");

        backToSignUp.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Going to Sign Up", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
            //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
//            ((LS_Main) activity).setViewPager(1);
            activity.pushFragment(FragmentId.GET(SignUpFragment.TAG));
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
            if(checkValidation())
                activity.pushFragment(FragmentId.GET(MyHousesFragment.TAG));

        });
    }

    private void changePasswordDisplay() {
        if(showPassword == 0){
            Toast.makeText(activity, "show password", Toast.LENGTH_LONG).show();
            showPassword = 1;
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.password, 0, R.drawable.password_eye_closed, 0);
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        else {
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
}
