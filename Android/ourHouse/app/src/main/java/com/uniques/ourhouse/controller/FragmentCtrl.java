package com.uniques.ourhouse.controller;

import android.app.Activity;
import android.view.View;

import com.uniques.ourhouse.fragment.FragmentId;

public interface FragmentCtrl extends ActivityCtrl {

    @Override
    default void init(Activity activity) {
    }

    void init(View view);

    /**
     * Use this method to apply arguments supplied during a fragment change
     * @param args offered arguments
     * @see com.uniques.ourhouse.MainActivity#pushFragment(FragmentId, Object...) MainActivity's implementation of pushFragment
     */
    void acceptArguments(Object... args);
}
