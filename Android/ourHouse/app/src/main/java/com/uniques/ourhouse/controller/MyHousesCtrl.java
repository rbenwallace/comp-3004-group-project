package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.CreateHouseFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.JoinHouseFragment;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;

import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import java.io.IOException;

public class MyHousesCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private ListView housesList;
    private House selectedHouse;
    private ArrayList<House> myHouses;
    private Button logoutBtn;
    ArrayAdapter adapter;
    private DatabaseLink database = Session.getSession().getDatabase();
    private List<String> houses = new ArrayList<>();
    private Consumer<House> filler;

    public MyHousesCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        housesList = view.findViewById(R.id.myHousesList);
        logoutBtn = view.findViewById(R.id.logoutBtnMH);
        // Gather houses if there are any from the shared pref Houses
//        User myUser = Session.getSession().getLoggedInUser();
        database.getUser(Session.getSession().getLoggedInUserId(), myUser -> {
            myUser = myUser != null ? myUser : Session.getSession().getLoggedInUser();
            if (myUser.getMyHouses() == null) {
                myUser.setMyHouses(new ArrayList<>());
                database.updateUser(myUser, success -> {
                });
            }
            if (myHouses == null) {
                myHouses = new ArrayList<>();
                fetchMyHouses(view, myUser.getMyHouses(), myHouses);
            } else
                onPostFetchMyHouses(view);

            logoutBtn.setOnClickListener(view14 -> {
                Session.getSession().getSecureAuthenticator().logout(activity, b1 -> {
                    database.clearLocalState(b2 -> {
                        FragmentId loginFragmentId = FragmentId.GET(LoginFragment.TAG);
                        activity.popFragment(loginFragmentId);
                        activity.pushFragment(loginFragmentId);
                    });
                });
            });
        });
    }

    private void fetchMyHouses(View view, List<ObjectId> houseIds, List<House> fetchedHouses) {
        if (filler != null) {
            return;
        }
        Log.d("MongoDB", houseIds.toString());
        if (houseIds.isEmpty()) {
            onPostFetchMyHouses(view);
            return;
        }
        filler = house -> {
            if (house != null) {
                fetchedHouses.add(house);
            }
            if (houseIds.isEmpty()) {
                filler = null;
                Log.d("MyHousesCtrl", "got all houses to go");
                onPostFetchMyHouses(view);
            } else {
                Log.d("MyHousesCtrl", houseIds.toString());
                database.getHouse(houseIds.remove(0), filler);
            }
        };
        Log.d("MyHousesCtrl", houseIds.toString());
        database.getHouse(houseIds.remove(0), filler);
    }

    private void onPostFetchMyHouses(View view) {
        Button createHouse = view.findViewById(R.id.createHouseBtn);
        Button joinHouse = view.findViewById(R.id.joinHouseBtn);
        //check for duplicates idk why this is happening
        for (int i = 0; i < myHouses.size(); i++) {
            int check = 0;
            for (int j = 0; j < myHouses.size(); j++) {
                if (myHouses.get(i).getId() == myHouses.get(j).getId()) {
                    check++;
                    if (check > 1) {
                        myHouses.remove(j);
                    }
                }
            }
        }

        User myUser = Session.getSession().getLoggedInUser();
        Log.d("myHouses", "myUser myHouses " + myUser.getMyHouses().toString());
        Log.d("myHouses", "arrayList myhouses " + myHouses.toString());
        Log.d("myHouses", "size myhouses " + myHouses.size());


        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, houses);
        houses.clear();
        for (int i = 0; i < myHouses.size(); i++) {
            if (myHouses.get(i) == null) continue;
            houses.add(myHouses.get(i).getName());
        }
        housesList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //Selected a house, change the current house to that house and enter main activity
        housesList.setOnItemClickListener((adapterView, view12, i, l) -> {
            selectedHouse = myHouses.get(i);
            Settings.OPEN_HOUSE.set(selectedHouse.getId());
            Intent intent = new Intent(activity, MainActivity.class);
            activity.startActivity(intent);
            activity.finish();
        });
        housesList.setOnItemLongClickListener((adapterView, view13, i, l) -> {
            LayoutInflater inflater = (LayoutInflater)
                    activity.getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.delete_house, null);
            // create the popup window
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(view13, Gravity.CENTER, 0, 0);
            // dismiss the popup window when touched
            Button btnDismiss = popupView.findViewById(R.id.deleteCancelHouse);
            btnDismiss.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
            Button btnDeleteHouse = popupView.findViewById(R.id.deleteConfirmHouse);
            btnDeleteHouse.setOnClickListener(view131 -> {
                database.deleteUserFromHouse(myHouses.get(i), myUser, success -> {
                    if (!success) {
                        Log.d("deleteUserFromHouse: ", "Failed");
                    } else {
                        //TODO Remove the user from the House from the house
                        popupWindow.dismiss();
                        activity.pushFragment(FragmentId.GET(MyHousesFragment.TAG));
                    }
                });
            });
            return true;
        });
        createHouse.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Going to CreateHouseFragment", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
            activity.pushFragment(FragmentId.GET(CreateHouseFragment.TAG));
        });
        joinHouse.setOnClickListener(view2 -> {
//            Toast.makeText(activity, "Going to JoinHouseFragment", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(3);
            activity.pushFragment(FragmentId.GET(JoinHouseFragment.TAG));
        });
    }


    @Override
    public void acceptArguments(Object... args) {
    }

    @Override
    public void updateInfo() {
    }
}
