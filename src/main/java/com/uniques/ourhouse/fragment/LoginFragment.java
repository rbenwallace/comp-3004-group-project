package com.uniques.ourhouse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.uniques.ourhouse.ActivityId;
import com.uniques.ourhouse.LS_Main;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.controller.LoginCtrl;

public class LoginFragment extends Fragment {
    public static final String TAG = "LoginFragment";
    private static final int layoutId = R.layout.fragment_login;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(LoginFragment.class, TAG, layoutId, activityId, true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            controller = new LoginCtrl(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId()));
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
        return setupId(ActivityId.GET(LS_Main.TAG));
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
