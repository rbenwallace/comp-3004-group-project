package com.uniques.ourhouse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.uniques.ourhouse.session.Session;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Session.getSession() == null) {
            Session.newSession(this);
        }
        if (Session.getSession().isLoggedIn()) {
            Session.getSession().getDatabase().getUser(Session.getSession().getLoggedInUserId(), user -> {
                if (user == null) {
                    Toast.makeText(this, "Failed to get loggedInUser object", Toast.LENGTH_LONG).show();
                } else {
                    Session.getSession().setLoggedInUser(user);
                    Log.d("Splash", "launching MainActivity");
                    delayStart(this, MainActivity.class);
                }
            });
        } else {
            Log.d("Splash", "launching LS_Main");
            delayStart(this, LS_Main.class);
        }
    }


    private static void delayStart(AppCompatActivity activity, Class destinationClass) {
        int SPLASH_TIME_OUT = 3000;
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity, destinationClass)
                    .putExtra("network_connected", Session.getSession().isNetworkConnected());
            activity.startActivity(intent);
            activity.finish();
        }, SPLASH_TIME_OUT);
    }

}
