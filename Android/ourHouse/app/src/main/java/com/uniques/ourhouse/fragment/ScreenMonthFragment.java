package com.uniques.ourhouse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uniques.ourhouse.ActivityId;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.controller.ScreenMonthCtrl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScreenMonthFragment extends Fragment<ScreenMonthCtrl> {
    public static final String TAG = "ScreenMonthFragment";
    private static final String ACTIVITY_TAG = MainActivity.TAG;
    private static final int LAYOUT_ID = R.layout.fragment_screen_month;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(ScreenMonthFragment.class, TAG, LAYOUT_ID, activityId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            setController(new ScreenMonthCtrl(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this)));
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
        return setupId(ActivityId.GET(ACTIVITY_TAG));
    }

    // NOTE FROM Victor: Fragment (base class) now forwards arguments to the controller
//    @Override
//    public void acceptArguments(Object... args) {
//        Log.d("wallace: ", month);
//        Log.d("wallace: ", year);
//    }

    @Override
    public boolean onHomeUpPressed() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}

