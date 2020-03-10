package com.uniques.ourhouse.controller;

import android.app.UiAutomation;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.MongoDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import java.util.ArrayList;
import java.util.function.Consumer;

import androidx.annotation.RequiresApi;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class JoinHouseCtrl implements FragmentCtrl{
    private FragmentActivity activity;
    private ArrayList<House> searchedHouses;
//    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private MongoDB myDatabase = new MongoDB();
    private ArrayList<String> houses;
    private ArrayAdapter adapter;
    private ListView housesList;
    private SearchView simpleSearchView;
    private RelativeLayout relList;
    private User myUser;

    public JoinHouseCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void init(View view) {
//        EditText houseJoinText = (EditText) view.findViewById(R.id.houseName_join);
        housesList = (ListView) view.findViewById(R.id.housesListJoin);
        relList = (RelativeLayout) view.findViewById(R.id.housesList);
        housesList.setClickable(true);
        searchedHouses = new ArrayList<>();
        houses = new ArrayList<>();
        housesList = view.findViewById(R.id.housesListJoin);
        adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, houses);
        housesList.setAdapter(adapter);
        myUser = myDatabase.getCurrentLocalUser(activity);
        simpleSearchView = (SearchView) view.findViewById(R.id.houseName_join);
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
                Log.d("CheckingHouses", "UPDATE_ONTEXT");
                myDatabase.findHousesByName(simpleSearchView.getQuery().toString().trim(), myList ->{
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
                EditText houseJoinPW = (EditText) popupView.findViewById(R.id.passwordG);
                Button btnDismiss = (Button) popupView.findViewById(R.id.enterPw);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(houseJoinPW.getText().toString().trim().equals(searchedHouses.get(i).getPassword())){
                            ArrayList<House> myHouses = myDatabase.getLocalHouseArray(activity);
                            if(myHouses != null) {
                                if (!myHouses.contains(searchedHouses.get(i))) {
                                    myHouses.add(searchedHouses.get(i));
                                    myDatabase.setLocalHouseArray(myHouses, activity);
                                    myUser.addHouseId(searchedHouses.get(i).getId());
                                    myUser.addHouseName(searchedHouses.get(i).getKeyId());
                                    myDatabase.setLocalUser(myUser, activity);
                                    myDatabase.updateUser(myUser, coolean ->{
                                        if(!coolean)Log.d("CreateHouseCtrl", "updating User failed");
                                        popupWindow.dismiss();
                                        activity.pushFragment(FragmentId.GET(MyHousesFragment.TAG));
                                    });
                                } else {
                                    houseJoinPW.setBackgroundColor(activity.getColor(R.color.red));
                                    houseJoinPW.setBackgroundColor(Color.RED);
                                    Log.d("JoinHouseCtrl", "House inside current houses list");
                                }
                            }
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
