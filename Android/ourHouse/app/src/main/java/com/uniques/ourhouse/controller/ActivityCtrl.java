package com.uniques.ourhouse.controller;

import android.app.Activity;

public interface ActivityCtrl {

    void init(Activity activity);

    void updateInfo();

    default void onDestroy() {
    }
}
