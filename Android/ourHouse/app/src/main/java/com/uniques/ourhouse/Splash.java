package com.uniques.ourhouse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            Intent homeIntent = new Intent(Splash.this, LS_Main.class);
            startActivity(homeIntent);
            finish();
        }, SPLASH_TIME_OUT);
    }

}
