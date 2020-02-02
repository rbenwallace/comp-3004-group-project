package com.uniques.ourhouse;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public final class ActivityId {
    private static final HashMap<String, ActivityId> activityLayoutIds = new HashMap<>();

    private Class<? extends AppCompatActivity> activityClass;
    private String name;
    private int layoutId;

    public static ActivityId GET(String activityName) {
        return activityLayoutIds.get(activityName);
    }

    static ActivityId SET(Class<? extends AppCompatActivity> activityClass, String name, int layoutId) {
        ActivityId activityId;
        if (activityLayoutIds.containsKey(name)) {
            activityId = activityLayoutIds.get(name);
            activityId.layoutId = layoutId;
        } else {
            activityId = new ActivityId(activityClass, name, layoutId);
            activityLayoutIds.put(name, activityId);
        }
        return activityId;
    }

    private ActivityId(Class<? extends AppCompatActivity> activityClass, String name, int layoutId) {
        this.activityClass = activityClass;
        this.name = name;
        this.layoutId = layoutId;
    }

    public AppCompatActivity newInstance() throws InstantiationException, IllegalAccessException {
        return activityClass.newInstance();
    }

    String getName() {
        return name;
    }

    int getLayoutId() {
        return layoutId;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
