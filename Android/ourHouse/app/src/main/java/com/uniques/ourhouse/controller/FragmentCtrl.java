package com.uniques.ourhouse.controller;

import android.app.Activity;
import android.view.View;

public interface FragmentCtrl extends ActivityCtrl {

    @Override
    default void init(Activity activity) {
    }

    void init(View view);

    void acceptArguments(Object... args);
}
