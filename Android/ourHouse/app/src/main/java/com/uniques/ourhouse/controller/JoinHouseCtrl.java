package com.uniques.ourhouse.controller;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import androidx.annotation.RequiresApi;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class JoinHouseCtrl implements FragmentCtrl{
    private FragmentActivity activity;
    private List<House> searchedHouses;
    private DatabaseLink database = Session.getSession().getDatabase();
    private List<String> houses;
    private ArrayAdapter adapter;
    private ListView housesList;

    public JoinHouseCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void init(View view) {
        Button joinHouse = view.findViewById(R.id.joinHouseBtn);
//        EditText houseJoinText = view.findViewById(R.id.houseName_join);
        RelativeLayout relList = view.findViewById(R.id.housesList);
        housesList = view.findViewById(R.id.housesListJoin);
        housesList.setClickable(true);
        searchedHouses = new ArrayList<>();
        houses = new ArrayList<>();
        housesList = view.findViewById(R.id.housesListJoin);
        adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, houses);
        housesList.setAdapter(adapter);

        Consumer<List<House>> housesConsumer = myList -> {
            Log.d("CheckingHouses", Integer.toString(myList.size()));
            searchedHouses = myList;
            updateInfo();
        };

        User myUser = Session.getSession().getLoggedInUser();
        SearchView simpleSearchView = view.findViewById(R.id.houseName_join);

//        houseJoinText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.d("CheckingHouses", "UPDATE_ONTEXT");
//                myDatabase.findHousesByName(houseJoinText.getText().toString().trim(), myList ->{
//                    Log.d("CheckingHouses", Integer.toString(myList.size()));
//                    searchedHouses = myList;
//                    updateInfo();
//                });
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//            }
//        });
        simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                // Log.d("CheckingHouses", "UPDATE_ONTEXT");
                // database.findHousesByName(houseJoinText.getText().toString().trim(), housesConsumer);
                // adapter.notifyDataSetChanged();

                database.findHousesByName(s.trim(), myList ->{
                    Log.d("CheckingHouses", Integer.toString(myList.size()));
                    searchedHouses = myList;
                    houses.clear();
                    for(int i = 0; i < searchedHouses.size(); i= i+1){
                        Log.d("CheckingHousesInside", i + " total: " + searchedHouses.size());
                        houses.add(searchedHouses.get(i).getKeyId());
                    }
                    Log.d("CheckingAdapterList", Integer.toString(adapter.getCount()));
                    Log.d("CheckingHousesStringList", Integer.toString(houses.size()));
                    Log.d("CheckingHousesList", Integer.toString(housesList.getChildCount()));
                    adapter.notifyDataSetChanged();
                    updateInfo();
                });
                return true;
            }
        });
        for(House h : searchedHouses){
            Log.d("checkingHouses " + h.getName(), h.toString());
            houses.add(h.getName());
        }
        housesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LayoutInflater inflater = (LayoutInflater)
                        activity.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.password, null);
                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                // dismiss the popup window when touched
                EditText houseJoinPW = popupView.findViewById(R.id.passwordG);
                Button btnDismiss = popupView.findViewById(R.id.enterPw);
                btnDismiss.setOnClickListener(v -> {
                    Log.d("passwordBOYS", "Users PW: " + houseJoinPW.getText().toString().trim() + " HousePW: " + searchedHouses.get(i).getPassword());
                    if (houseJoinPW.getText().toString().trim().equals(searchedHouses.get(i).getPassword())) {
                        User myUser = Session.getSession().getLoggedInUser();
                        if (myUser.getMyHouses().contains(searchedHouses.get(i).getId())) {
                            houseJoinPW.setBackgroundColor(activity.getColor(R.color.red));
                            houseJoinPW.setBackgroundColor(Color.RED);
                            Log.d("JoinHouseCtrl", "Already joined house");
                        } else {
                            myUser.addHouseId(searchedHouses.get(i).getId());
                            database.updateUser(myUser, success -> {
                                if (success) {
                                    popupWindow.dismiss();
                                    activity.pushFragment(FragmentId.GET(MyHousesFragment.TAG));
                                } else {
                                    Log.d("JoinHouseCtrl", "Failed to add house to user's houses");
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {
    }


}
