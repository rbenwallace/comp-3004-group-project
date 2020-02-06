package com.uniques.ourhouse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!connect(this)) {
            //TODO show snackbar "failed to launch auto-signin"
        }
    }

    static boolean connect(AppCompatActivity activity) {
        if (Session.getSession() == null) {
            boolean signedIn = Session.newSession(activity);
            Log.d("Splash", "session signedIn? " + (signedIn && Settings.USER_LOGIN_KEY.get() != null));
            if (Settings.USER_LOGIN_KEY.get() != null) {
                Log.d("Splash", "launching to main");
                delayStart(activity, MainActivity.class, signedIn);
            } else {
                Log.d("Splash", "launching LS");
                delayStart(activity, LS_Main.class, signedIn);
            }
            return signedIn;
        }
        return true;
    }

    private static void delayStart(AppCompatActivity activity, Class destinationClass, boolean signedIn) {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity, destinationClass).putExtra("loading:connected", signedIn);
            activity.startActivity(intent);
            activity.finish();
        }, SPLASH_TIME_OUT);
    }

}
