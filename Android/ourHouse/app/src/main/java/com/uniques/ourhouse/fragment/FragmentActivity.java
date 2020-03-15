package com.uniques.ourhouse.fragment;

import android.os.Bundle;
import android.util.Log;

import com.uniques.ourhouse.ActivityId;

import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class FragmentActivity extends AppCompatActivity {
    private static HashMap<ActivityId, FragmentActivity> savedInstances;
    protected boolean allowPopAllFragments;

    private static void checkMapNotNull() {
        if (savedInstances == null)
            savedInstances = new HashMap<>();
    }

    protected static void saveInstance(ActivityId activityId, @NonNull FragmentActivity activity) {
        checkMapNotNull();
        Log.d(activityId.getName(), "Activity instance saved");
        savedInstances.put(activityId, activity);
    }

    //    @NonNull
    protected static FragmentActivity getSavedInstance(ActivityId activityId, Fragment fragment) {
        checkMapNotNull();
        FragmentActivity savedInstance = savedInstances.get(activityId);
        if (savedInstance == null) {
            try {
                savedInstance = (FragmentActivity) Objects.requireNonNull(activityId).newInstance((AppCompatActivity) fragment.getActivity(), fragment);
            } catch (InstantiationException | IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return savedInstance;
    }

//    protected static Session getSession() {
//        return Session.getSession();
//    }

    protected final Stack<Fragment> fragmentStack = new Stack<>();

    protected Fragment currentFragment() {
        if (fragmentStack.isEmpty()) return null;
        return fragmentStack.peek();
    }

    protected void pushFragment(FragmentId fragmentId) {
        pushFragment(fragmentId, new Object[0]);
    }

    protected abstract ActivityId getActivityId();

    public abstract void pushFragment(FragmentId fragmentId, Object... args);

    public abstract void popFragment(FragmentId fragmentId);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO fix sessions
//        if (Session.getSession() == null) {
//            if (!Session.newSession(this)) {
////                startActivity(new Intent(this, LoginActivity.class));
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = currentFragment();
        Log.d(getActivityId().getName(), "=== back press");
        if (fragment == null || !fragment.onBackPressed()) {
            if (fragmentStack.size() > 1) super.onBackPressed();
            else if (allowPopAllFragments) super.onBackPressed();
            //TODO press again to exit
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (savedInstances != null) {
            savedInstances.clear();
            savedInstances = null;
        }
    }
}
