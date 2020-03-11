package com.uniques.ourhouse;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.uniques.ourhouse.fragment.AddFeeFragment;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.EditFeeFragment;
import com.uniques.ourhouse.fragment.EditTaskFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.Fragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.PerformanceFragment;
import com.uniques.ourhouse.fragment.ScreenMonthFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;

import androidx.fragment.app.FragmentManager;

public class MainActivity extends FragmentActivity {
    public static final String TAG = "MainActivity";
    static final int LAYOUT_ID = R.layout.activity_main;

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    public BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getActivityId().getLayoutId());
        saveInstance(getActivityId(), this);

        Log.d(TAG, "Launching");

        FeedFragment.setupId(getActivityId());
        ManageFragment.setupId(getActivityId());
        AddFeeFragment.setupId(getActivityId());
        AddTaskFragment.setupId(getActivityId());
        SettingsFragment.setupId(getActivityId());
        AmountPaidFragment.setupId(getActivityId());
        CalculateAmountToPayFragment.setupId(getActivityId());
        PerformanceFragment.setupId(getActivityId());
        ScreenMonthFragment.setupId(getActivityId());
        EditTaskFragment.setupId(getActivityId());
        EditFeeFragment.setupId(getActivityId());

        BottomNavigationView navigation = findViewById(R.id.main_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView = navigation;


        pushFragment(FragmentId.GET(FeedFragment.TAG));
    }

    public final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_feed:
                if (currentFragment() == null || currentFragment().getFragmentId() != FragmentId.GET(FeedFragment.TAG))
                    pushFragment(FragmentId.GET(FeedFragment.TAG));
                return true;
            case R.id.navigation_manage:
                if (currentFragment() == null || currentFragment().getFragmentId() != FragmentId.GET(ManageFragment.TAG))
                    pushFragment(FragmentId.GET(ManageFragment.TAG));
                return true;
            case R.id.navigation_stats:
                if (currentFragment() == null || currentFragment().getFragmentId() != FragmentId.GET(AmountPaidFragment.TAG))
                    pushFragment(FragmentId.GET(AmountPaidFragment.TAG));
                return true;
        }
        return false;
    };

    @Override
    protected ActivityId getActivityId() {
        return ActivityId.SET(this.getClass(), TAG, LAYOUT_ID);
    }

    /**
     * Push a fragment to the fragment stack. Note, this will not pop any previous instances of
     * fragments from the stack -- it will simply create a new fragment and push that to the stack.
     *
     * @param fragmentId FragmentId of the fragment to add. It is crucial to specify the right id
     *                   because it's what's used to generate a new instance
     * @param args       any arguments you want to pass. Please note this is a spread argument. Passing
     *                   an array in the form pushFragment(--, Object[]) will result in only a single
     *                   object (representing the array) being passed.
     * @see #popFragment(FragmentId) popping a fragment from the stack
     * @see FragmentId#GET(String) getting a fragmentId
     */
    @Override
    public void pushFragment(FragmentId fragmentId, Object... args) {
        Fragment fragment;
        try {
            fragment = fragmentId.newInstance();
            fragment.offerArguments(args);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
        fragmentStack.push(fragment);
        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment, currentFragment())
                .addToBackStack(String.valueOf(fragmentId))
                .commit();
    }

    /**
     * This method will pop any and all fragments up to and including the first fragment who's
     * fragmentId matches the supplied id. If there are 3 fragments before the fragment you want to
     * pop, all 4 fragments will be removed from the stack. This is done to preserve the order of
     * assignment.
     *
     * @param fragmentId id of the fragment you want to pop
     * @see FragmentId#GET(String) getting a fragmentId
     */
    @Override
    public void popFragment(FragmentId fragmentId) {
        try {
            fragmentManager.popBackStack(
                    String.valueOf(fragmentId), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentStack.pop().destroy();

            if (currentFragment() != null && currentFragment().getFragmentId().isBaseFragment()) {
                navView.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
//            FragmentActivity.getSavedInstance(ActivityId.MAIN_ACTIVITY).popFragment(fragmentId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("=== destroyed");
    }
}
