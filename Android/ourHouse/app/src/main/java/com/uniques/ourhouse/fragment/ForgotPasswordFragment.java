package com.uniques.ourhouse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uniques.ourhouse.ActivityId;
import com.uniques.ourhouse.LS_Main;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.controller.ForgotPasswordCtrl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ForgotPasswordFragment extends Fragment<ForgotPasswordCtrl> {
    public static final String TAG = "ForgotPasswordFragment";
    private static final String ACTIVITY_TAG = LS_Main.TAG;
    private static final int LAYOUT_ID = R.layout.fragment_forget_password;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(ForgotPasswordFragment.class, TAG, LAYOUT_ID, activityId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            setController(new ForgotPasswordCtrl(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this)));
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

    @Override
    public boolean onHomeUpPressed() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}

