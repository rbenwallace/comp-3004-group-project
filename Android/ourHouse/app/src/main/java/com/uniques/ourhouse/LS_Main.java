package com.uniques.ourhouse;

import android.os.Bundle;
import android.util.Log;

import com.uniques.ourhouse.fragment.CreateHouseFragment;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.Fragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.JoinHouseFragment;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;

import androidx.fragment.app.FragmentManager;

public class LS_Main extends FragmentActivity {
    public static final String TAG = "LS_Main";
    static final int LAYOUT_ID = R.layout.activity_ls__main;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getActivityId().getLayoutId());
        saveInstance(getActivityId(), this);
        Log.d(TAG, "Launching");
        LoginFragment.setupId(getActivityId());
        SignUpFragment.setupId(getActivityId());
        ForgotPasswordFragment.setupId(getActivityId());
        MyHousesFragment.setupId(getActivityId());
        JoinHouseFragment.setupId(getActivityId());
        CreateHouseFragment.setupId(getActivityId());
        pushFragment(FragmentId.GET(LoginFragment.TAG));
    }

    @Override
    protected ActivityId getActivityId() {
        return ActivityId.SET(this.getClass(), TAG, LAYOUT_ID);
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
                .replace(R.id.ls_main_fragment, currentFragment())
                .addToBackStack(String.valueOf(fragmentId))
                .commit();
    }

    @Override
    public void popFragment(FragmentId fragmentId) {
        try {
            fragmentManager.popBackStack(
                    String.valueOf(fragmentId), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentStack.pop().destroy();
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
