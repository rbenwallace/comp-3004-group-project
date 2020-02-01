package com.uniques.ourhouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;

public class LS_Main extends AppCompatActivity {

    private static final String TAG = "LS_Main";

    private SectionPageAdapter mSectionPageAdapter;
    private CustomViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ls__main);

        mSectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());

        mViewPager = (CustomViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

    }

    private void setupViewPager(CustomViewPager viewPager){
        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());
        //Inflates first Fragment
        adapter.addFragment(new Login(), "Login");
        adapter.addFragment(new Sign_Up(), "Sign Up");
        adapter.addFragment(new Forget_Password(), "Forget Password");
        adapter.addFragment(new Manage_Homescreen(), "Manage Homescreen");
        adapter.addFragment(new Add_Fee(), "Add Fee");
        adapter.addFragment(new Add_Task(), "Add Task");
        adapter.addFragment(new Settings(), "Settings");
        viewPager.setAdapter(adapter);
        Log.d("titsMagee", "CHECKING IF LOADED");
    }
    //Changes to the fragment with fragment number
    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }

}
