package com.uniques.ourhouse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uniques.ourhouse.ActivityId;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.controller.FeedCard;
import com.uniques.ourhouse.controller.FeedCtrl;
import com.uniques.ourhouse.controller.RecyclerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FeedFragment extends Fragment<FeedCtrl> {
    public static final String TAG = "FeedFragment";
    private static final String ACTIVITY_TAG = MainActivity.TAG;
    private static final int LAYOUT_ID = R.layout.fragment_feed;

    public static FragmentId setupId(ActivityId activityId) {
        return FragmentId.SET(FeedFragment.class, TAG, LAYOUT_ID, activityId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            setController(new FeedCtrl(FragmentActivity.getSavedInstance(getFragmentId().getDefaultActivityId(), this)));
        }
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getFragmentId().getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);

        RecyclerView homeRecycler = view.findViewById(R.id.feed_recycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecycler.setItemAnimator(new DefaultItemAnimator());

        RecyclerAdapter<FeedCard> adapter = new RecyclerAdapter<>(
                homeRecycler,
                controller.observableCards,
                R.layout.content_card_feed_bubble);
        homeRecycler.setAdapter(adapter);
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

