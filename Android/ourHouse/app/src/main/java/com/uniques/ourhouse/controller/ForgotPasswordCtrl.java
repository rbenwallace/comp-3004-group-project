package com.uniques.ourhouse.controller;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.session.MongoDB;
import com.uniques.ourhouse.session.Session;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

public class ForgotPasswordCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private EditText email;
    private MongoDB remoteDatabase;

    public ForgotPasswordCtrl(FragmentActivity activity) {
        this.activity = activity;
        remoteDatabase = ((MongoDB) Objects.requireNonNull(Session.getSession().getRemoteDatabase()));
    }

    @Override
    public void init(View view) {
        Button backToLogin = (Button) view.findViewById(R.id.backToLoginBtn);
        Button sumbitPwRequest = (Button) view.findViewById(R.id.forgot_button);
        email = view.findViewById(R.id.registered_emailid);
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("loginData", null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> transferedArrayFromSignUp = gson.fromJson(json, type);
        if (transferedArrayFromSignUp != null) {
            try {
                email.setText(transferedArrayFromSignUp.get(2));
            } catch (Error e) {
                Log.d("ForgotPasswordCtrl", e.toString());
            }
        }
        Log.d(ForgotPasswordFragment.TAG, "onCreatedView: Started");
        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.popFragment(FragmentId.GET(ForgotPasswordFragment.TAG));
                //TODO NAVIGATE TO NEXT FRAGMENT
            }
        });
        sumbitPwRequest.setOnClickListener(view1 -> {
            recoverPassword();
        });

    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {

    }

    private void recoverPassword() {
        String em = email.getText().toString().trim();
        UserPasswordAuthProviderClient emailPassClient =
                remoteDatabase.getAuth().getProviderClient(UserPasswordAuthProviderClient.factory);

        if (TextUtils.isEmpty(em)) {
            email.setError(activity.getString(R.string.error_invalid_email));
        } else {
            //send a reset password request to stitch
            emailPassClient.sendResetPasswordEmail(em).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("stitch-auth", "Successfully sent password reset email");
                        Toast.makeText(activity, "Password reset mail sent.", Toast.LENGTH_SHORT).show();
                        activity.popFragment(FragmentId.GET(ForgotPasswordFragment.TAG));
                    } else {
                        Log.e("stitch-auth", "Error sending password reset email:", task.getException());
                        Toast.makeText(activity, "Account not found!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
