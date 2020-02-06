package com.uniques.ourhouse;

import android.os.Bundle;

import com.uniques.ourhouse.fragment.AddFeeFragment;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.CreateHouseFragment;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.Fragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.JoinHouseFragment;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;

import androidx.fragment.app.FragmentManager;

public class LS_Main extends FragmentActivity {
    public static final String TAG = "LS_Main";

    private final FragmentManager fragmentManager = getSupportFragmentManager();

//    private SectionPageAdapter mSectionPageAdapter;
//    private CustomViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getActivityId().getLayoutId());
        saveInstance(getActivityId(), this);

        LoginFragment.setupId(getActivityId());
        SignUpFragment.setupId(getActivityId());
        ForgotPasswordFragment.setupId(getActivityId());
        ManageFragment.setupId(getActivityId());
        AddFeeFragment.setupId(getActivityId());
        AddTaskFragment.setupId(getActivityId());
        SettingsFragment.setupId(getActivityId());
        MyHousesFragment.setupId(getActivityId());
        JoinHouseFragment.setupId(getActivityId());
        CreateHouseFragment.setupId(getActivityId());


        pushFragment(FragmentId.GET(LoginFragment.TAG));

//        mSectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());

//        mViewPager = (CustomViewPager) findViewById(R.id.container);
//        setupViewPager(mViewPager);

    }

    private void setupViewPager(CustomViewPager viewPager) {
//        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());
//        //Inflates first Fragment
//        adapter.addFragment(new LoginFragment(), "LoginFragment");
//        adapter.addFragment(new SignUpFragment(), "Sign Up");
//        adapter.addFragment(new ForgotPasswordFragment(), "Forget Password");
//        adapter.addFragment(new ManageFragment(), "Manage Homescreen");
//        adapter.addFragment(new AddFeeFragment(), "Add Fee");
//        adapter.addFragment(new AddTaskFragment(), "Add Task");
//        adapter.addFragment(new SettingsFragment(), "SettingsFragment");
//        viewPager.setAdapter(adapter);
//        Log.d("titsMagee", "CHECKING IF LOADED");
    }
    //Changes to the fragment with fragment number
//    public void setViewPager(int fragmentNumber){
//        mViewPager.setCurrentItem(fragmentNumber);
//    }

    @Override
    protected ActivityId getActivityId() {
        return ActivityId.SET(this.getClass(), TAG, R.layout.activity_ls__main);
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

            System.out.println("==== got here ====");

            if (currentFragment() != null && currentFragment().getFragmentId().isBaseFragment()) {
                //TODO add bottom navigation
//                navView.setVisibility(View.VISIBLE);
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
