package com.uniques.ourhouse.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.uniques.ourhouse.ActivityId;
import com.uniques.ourhouse.LS_Main;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.controller.SignUpCtrl;

public class SignUpFragment extends Fragment<SignUpCtrl> {
    public static final String TAG = "SignUpFragment";
    private static final int LAYOUT_ID = R.layout.fragment_sign_up;
    private static final String ACTIVITY_TAG = LS_Main.TAG;
    private StitchUser currentUser;
    public static StitchAppClient client;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(SignUpFragment.class, TAG, LAYOUT_ID, activityId, true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            controller = new SignUpCtrl(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this));
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

    @Override
    public void onResume() {
        super.onResume();
        client = Stitch.getAppClient("ourhouse-notdj");
        currentUser = client.getAuth().getUser();
        if(currentUser != null){

            FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this).startActivity(new Intent(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this), MainActivity.class));
        }
    }
}
