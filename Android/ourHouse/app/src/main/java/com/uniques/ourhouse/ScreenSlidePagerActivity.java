package com.uniques.ourhouse;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.uniques.ourhouse.fragment.AddFeeFragment;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.AmountPaidFragment;
import com.uniques.ourhouse.fragment.CalculateAmountToPayFragment;
import com.uniques.ourhouse.fragment.EditFeeFragment;
import com.uniques.ourhouse.fragment.EditTaskFragment;
import com.uniques.ourhouse.fragment.FeeListFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.Fragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.fragment.PerformanceFragment;
import com.uniques.ourhouse.fragment.ScreenMonthFragment;
import com.uniques.ourhouse.fragment.ScreenSlidePageFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;

import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ScreenSlidePagerActivity extends FragmentActivity {
    private static final int NUM_PAGES = 3;

    private ViewPager mPager;

    private PagerAdapter pagerAdapter;

    public static final String TAG = "MainActivity";
    static final int LAYOUT_ID = R.layout.activity_main;

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private ObjectId houseId;
    private ObjectId userId;
    private BottomNavigationView navView;
    private boolean navViewUpdatedByCode;
    private int currentMonth;
    private String strYear;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        mPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if(mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

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
        Log.d(TAG, "Pushing fragment " + fragmentId.getName());
        fragmentStack.push(fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.main_fragment, currentFragment());
        if (fragmentStack.size() > 1) {
            fragmentTransaction = fragmentTransaction.addToBackStack(String.valueOf(fragmentId));
        }
        fragmentTransaction.commit();
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
            Log.d(TAG, "Popping till fragment " + fragmentId.getName() + "(inclusive)");

            // pop in Android fragment manager
            fragmentManager.popBackStack(
                    String.valueOf(fragmentId), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // pop in ourHouse fragment stack
            boolean poppedDesiredFragment = false;
            while (!poppedDesiredFragment && !fragmentStack.isEmpty()) {
                if (fragmentStack.peek().getFragmentId().getName().equals(fragmentId.getName())) {
                    poppedDesiredFragment = true;
                }
                fragmentStack.pop().destroy();
            }

            if (currentFragment() != null && currentFragment().getFragmentId().isBaseFragment()) {
                navView.setVisibility(View.VISIBLE);
            }

            for (int i = fragmentStack.size() - 1; i >= 0; --i) {
                String name = fragmentStack.get(i).getFragmentId().getName();
                Log.d(TAG, i + " " + name);
                if (name.equals(FeedFragment.TAG)) {
                    Log.d(TAG, "last nav item in stack is feed");
                    navViewUpdatedByCode = true;
                    navView.setSelectedItemId(R.id.navigation_feed);
                    break;
                } else if (name.equals(ManageFragment.TAG)) {
                    Log.d(TAG, "last nav item in stack is manage");
                    navViewUpdatedByCode = true;
                    navView.setSelectedItemId(R.id.navigation_manage);
                    break;
                } else if (name.equals(AmountPaidFragment.TAG)) {
                    Log.d(TAG, "last nav item in stack is stats");
                    navViewUpdatedByCode = true;
                    navView.setSelectedItemId(R.id.navigation_stats);
                    break;
                }
            }
        } catch (Exception ignored) {
//            FragmentActivity.getSavedInstance(ActivityId.MAIN_ACTIVITY).popFragment(fragmentId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroyed");
    }
}