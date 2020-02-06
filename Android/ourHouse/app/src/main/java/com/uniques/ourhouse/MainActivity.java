package com.uniques.ourhouse;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.Fragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;

import androidx.fragment.app.FragmentManager;

public class MainActivity extends FragmentActivity {
    public static final String TAG = "MainActivity";

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getActivityId().getLayoutId());
        saveInstance(getActivityId(), this);

        FeedFragment.setupId(getActivityId());

        BottomNavigationView navigation = findViewById(R.id.main_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView = navigation;


        pushFragment(FragmentId.GET(FeedFragment.TAG));
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
//            case R.id.navigation_home:
//                if (currentFragment() != null)
//                    popFragment(FragmentId.HOME_FRAGMENT);
//                if (currentFragment() == null || currentFragment().getFragmentId() != FragmentId.HOME_FRAGMENT)
//                    pushFragment(FragmentId.HOME_FRAGMENT);
//                return true;
//            case R.id.navigation_calendar:
//                if (currentFragment() != null)
//                    popFragment(FragmentId.CALENDAR_FRAGMENT);
//                if (currentFragment() == null || currentFragment().getFragmentId() != FragmentId.CALENDAR_FRAGMENT)
//                    pushFragment(FragmentId.CALENDAR_FRAGMENT);
//                return true;
//            case R.id.navigation_evaluation:
//                //mTextMessage.setText(R.string.title_evaluation);
//                return true;
        }
        return false;
    };

    @Override
    protected ActivityId getActivityId() {
        return ActivityId.SET(this.getClass(), TAG, R.layout.activity_main);
    }

    @Override
    public void pushFragment(FragmentId fragmentId, Object... args) {
        Fragment fragment = null;
        try {
            fragment = fragmentId.newInstance();
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

    @Override
    public void popFragment(FragmentId fragmentId) {
        try {
            fragmentManager.popBackStack(
                    String.valueOf(fragmentId), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentStack.pop().destroy();

            System.out.println("==== got here ====");

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
