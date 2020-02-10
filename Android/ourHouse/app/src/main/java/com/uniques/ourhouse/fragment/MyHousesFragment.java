package com.uniques.ourhouse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uniques.ourhouse.ActivityId;
import com.uniques.ourhouse.LS_Main;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.controller.MyHousesCtrl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyHousesFragment extends Fragment<MyHousesCtrl> {
    public static final String TAG = "MyHousesFragment";
<<<<<<< HEAD
    private static final String ACTIVITY_TAG = LS_Main.TAG;
    private static final int LAYOUT_ID = R.layout.fragment_my_houses;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(MyHousesFragment.class, TAG, LAYOUT_ID, activityId, true);
=======
    private static final int layoutId = R.layout.fragment_my_houses;
    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(MyHousesFragment.class, TAG, layoutId, activityId, true);
>>>>>>> master
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            controller = new MyHousesCtrl(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this));
        }

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getFragmentId().getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
        controller.updateInfo();
    }

    @Override
    public FragmentId getFragmentId() {
<<<<<<< HEAD
        return setupId(ActivityId.GET(ACTIVITY_TAG));
=======
        return setupId(ActivityId.GET(LS_Main.TAG));
>>>>>>> master
    }

    @Override
    public boolean onHomeUpPressed() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
