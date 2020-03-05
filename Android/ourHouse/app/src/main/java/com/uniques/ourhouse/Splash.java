package com.uniques.ourhouse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.uniques.ourhouse.session.Session;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    public static StitchAppClient client = Stitch.initializeAppClient("ourhouse-notdj");

    private StitchUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        client = Stitch.getAppClient("ourhouse-notdj");
        currentUser = Splash.client.getAuth().getUser();
        Log.d("Splash", "launching LS");
        delayStart(this, MainActivity.class, true);
    }


    private static void delayStart(AppCompatActivity activity, Class destinationClass, boolean signedIn) {
        new Handler().postDelayed(() -> {
            if (Session.getSession() == null) {
                Session.newSession(activity);
            }
            Intent intent = new Intent(activity, destinationClass).putExtra("loading:connected", signedIn);
            activity.startActivity(intent);
            activity.finish();
        }, SPLASH_TIME_OUT);
    }

}
