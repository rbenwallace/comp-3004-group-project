package com.uniques.ourhouse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uniques.ourhouse.ActivityId;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.controller.FeeListCtrl;
import com.uniques.ourhouse.controller.FeeListItemCard;
import com.uniques.ourhouse.controller.RecyclerAdapter;
import com.uniques.ourhouse.controller.TaskRotationCard;

public class FeeListFragment extends Fragment<FeeListCtrl> {
    public static final String TAG = "FeeListFragment";
    private static final String ACTIVITY_TAG = MainActivity.TAG;
    private static final int LAYOUT_ID = R.layout.fragment_fee_list;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(FeeListFragment.class, TAG, LAYOUT_ID, activityId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            setController(new FeeListCtrl(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this)));
        }
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getFragmentId().getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);

        RecyclerView personRecycler = view.findViewById(R.id.listOfFees);
        personRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        personRecycler.setItemAnimator(new DefaultItemAnimator());

        RecyclerAdapter<FeeListItemCard> adapter = new RecyclerAdapter<>(
                personRecycler,
                controller.observableCards,
                R.layout.fee_list_item);
        personRecycler.setAdapter(adapter);
        controller.setRecyclerAdapter(adapter);

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

