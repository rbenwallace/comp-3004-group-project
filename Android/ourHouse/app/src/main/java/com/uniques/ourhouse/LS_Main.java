package com.uniques.ourhouse;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.uniques.ourhouse.fragment.CreateHouseFragment;
import com.uniques.ourhouse.fragment.ForgotPasswordFragment;
import com.uniques.ourhouse.fragment.Fragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.JoinHouseFragment;
import com.uniques.ourhouse.fragment.JoiningHouseFragment;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.fragment.SignUpFragment;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;

import androidx.fragment.app.FragmentManager;

import org.bson.types.ObjectId;

import java.util.List;

public class LS_Main extends FragmentActivity {
    public static final String TAG = "LS_Main";
    static final int LAYOUT_ID = R.layout.activity_ls__main;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri url = getIntent().getData();
        String houseId = null;
        String action = null;
        if(url != null){
            List<String> params = url.getPathSegments();
            action = params.get(params.size() -2);
            houseId = params.get(params.size() -1);
            Toast.makeText(this, "action: " + action + " houseID: " + houseId, Toast.LENGTH_LONG).show();
            if (Session.getSession() == null) {
                Session.newSession(this);
            }
            if (Session.getSession().isLoggedIn()) {
                Session.getSession().getDatabase().getUser(Session.getSession().getLoggedInUserId(), user -> {
                    if (user == null) {
                        Toast.makeText(this, "Failed to get loggedInUser object", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("LS_Main", user.toString());
                        Session.getSession().setLoggedInUser(user);
                    }
                });
            }
            initalize();
            final String HOUSEID = houseId;
            if(action != null && action.equals("joinhouse")) {
                if ((Session.getSession().isLoggedIn())) {
                    Log.d("JoinHouseCtrl", "User Logged in");
                    pushFragment(FragmentId.GET(JoiningHouseFragment.TAG));
                    Session.getSession().getDatabase().getUser(Session.getSession().getLoggedInUserId(), myUser -> {
                        Log.d("JoinHouseCtrl", "Grabbed user: " + myUser);
                        myUser.addHouse(new ObjectId(HOUSEID));
                        Session.getSession().getDatabase().getHouse(new ObjectId(HOUSEID), house ->{
                            if(house != null){
                                Log.d("JoinHouseCtrl", "Grabbed House: " + house);
                                house.addOccupant(myUser);
                                Session.getSession().getDatabase().updateHouse(house, bool ->{
                                    if(bool)  Log.d("JoinHouseCtrl", "House Updated");
                                    else {
                                        Toast.makeText(this, "Could not Join House", Toast.LENGTH_LONG).show();
                                        pushFragment(FragmentId.GET(LoginFragment.TAG));
                                    }
                                });
                                Session.getSession().getDatabase().updateUser(myUser, success -> {
                                    if (success) {
                                        Log.d("JoinHouseCtrl", "User Updated and Joined");
                                        pushFragment(FragmentId.GET(MyHousesFragment.TAG));
                                    } else {
                                        Log.d("JoinHouseCtrl", "User Could not Join House");
                                        pushFragment(FragmentId.GET(MyHousesFragment.TAG));
                                    }
                                });
                            }
                            else {
                                Toast.makeText(this, "Could not Join House", Toast.LENGTH_LONG).show();
                                pushFragment(FragmentId.GET(LoginFragment.TAG));
                            }
                        });
                    });
                }
                else{
                    Toast.makeText(this, "Create a account", Toast.LENGTH_LONG).show();
                    pushFragment(FragmentId.GET(LoginFragment.TAG));
                }
            }
            else{
                pushFragment(FragmentId.GET(LoginFragment.TAG));
            }
        }
        else {
            initalize();
            pushFragment(FragmentId.GET(LoginFragment.TAG));
        }


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
        Log.d(TAG, "Destroyed");
    }

    private void initalize(){
        setContentView(getActivityId().getLayoutId());
        saveInstance(getActivityId(), this);
        Log.d(TAG, "Launching");
        LoginFragment.setupId(getActivityId());
        SignUpFragment.setupId(getActivityId());
        ForgotPasswordFragment.setupId(getActivityId());
        MyHousesFragment.setupId(getActivityId());
        JoinHouseFragment.setupId(getActivityId());
        CreateHouseFragment.setupId(getActivityId());
        JoiningHouseFragment.setupId(getActivityId());
    }
}
