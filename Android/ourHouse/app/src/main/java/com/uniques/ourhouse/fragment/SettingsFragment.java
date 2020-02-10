package com.uniques.ourhouse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uniques.ourhouse.ActivityId;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
<<<<<<< HEAD
import com.uniques.ourhouse.controller.RecyclerAdapter;
import com.uniques.ourhouse.controller.SettingsCtrl;
import com.uniques.ourhouse.controller.TaskRotationCard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsFragment extends Fragment<SettingsCtrl> {
    public static final String TAG = "SettingsFragment";
    private static final String ACTIVITY_TAG = MainActivity.TAG;
    private static final int LAYOUT_ID = R.layout.fragment_settings;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(SettingsFragment.class, TAG, LAYOUT_ID, activityId, true);
=======
import com.uniques.ourhouse.controller.SettingsCtrl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SettingsFragment extends Fragment<SettingsCtrl> {
    public static final String TAG = "SettingsFragment";
    private static final int layoutId = R.layout.fragment_settings;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(SettingsFragment.class, TAG, layoutId, activityId, true);
>>>>>>> master
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            controller = new SettingsCtrl(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this));
        }
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getFragmentId().getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
<<<<<<< HEAD

        RecyclerView personRecycler = view.findViewById(R.id.settings_recycler);
        personRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        personRecycler.setItemAnimator(new DefaultItemAnimator());

        RecyclerAdapter<TaskRotationCard> adapter = new RecyclerAdapter<>(
                personRecycler,
                controller.observableCards,
                R.layout.roommate_item);
        personRecycler.setAdapter(adapter);
        controller.setRecyclerAdapter(adapter);

=======
>>>>>>> master
        controller.updateInfo();
    }

    @Override
    public FragmentId getFragmentId() {
<<<<<<< HEAD
        return setupId(ActivityId.GET(ACTIVITY_TAG));
=======
        return setupId(ActivityId.GET(MainActivity.TAG));
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

