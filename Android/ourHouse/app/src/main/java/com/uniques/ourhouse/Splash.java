package com.uniques.ourhouse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //This always gets called after you login successfully once and try coming back to the application
        //Basically Session is not saving its user or itself, how can one save the session?
        if (Session.getSession() == null) {
            Session.newSession(this);
        }
        if (Session.getSession().isLoggedIn()) {
            Session.getSession().getDatabase().getUser(Session.getSession().getLoggedInUserId(), user -> {
                if (user == null) {
                    Toast.makeText(this, "Failed to get loggedInUser object", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("Splash", user.toString());
                    Session.getSession().setLoggedInUser(user);
                    if (Settings.OPEN_HOUSE.get() == null) {
                        Log.d("Splash", "launching LS_Main");
                        delayStart(this, LS_Main.class);
                    } else {
                        Log.d("Splash", "launching MainActivity");
                        delayStart(this, MainActivity.class);
                    }
                }
            });
        } else {
            Log.d("Splash", "launching LS_Main");
            delayStart(this, LS_Main.class);
        }
    }


    private static void delayStart(AppCompatActivity activity, Class destinationClass) {
        int SPLASH_TIME_OUT = 1500;
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity, destinationClass)
                    .putExtra("network_connected", Session.getSession().isNetworkConnected());
            activity.startActivity(intent);
            activity.finish();
        }, SPLASH_TIME_OUT);
    }

}
