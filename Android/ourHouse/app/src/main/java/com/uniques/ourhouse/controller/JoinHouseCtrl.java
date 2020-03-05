package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Toast;

import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.session.MongoDB;

import java.util.ArrayList;
import java.util.Timer;
import com.uniques.ourhouse.util.TextChangeListener;

import java.util.ArrayList;
import java.util.function.Consumer;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class JoinHouseCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private ArrayList<House> searchedHouses;
//    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private MongoDB myDatabase = new MongoDB();
    private ArrayList<String> houses;
    private ArrayAdapter adapter;
    private Timer timer;


    public JoinHouseCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Button joinHouse = view.findViewById(R.id.joinHouseBtn);
        EditText houseJoinText = (EditText) view.findViewById(R.id.houseName_join);
        ListView housesList = (ListView) view.findViewById(R.id.housesListJoin);
        searchedHouses = new ArrayList<>();
        houses = new ArrayList<>();
        adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, houses);
        housesList.setAdapter(adapter);

        Consumer<ArrayList<House>> housesConsumer = myList -> {
            Log.d("CheckingHouses", Integer.toString(myList.size()));
            searchedHouses = myList;
            updateInfo();
        };

        houseJoinText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("CheckingHouses", "UPDATE_ONTEXT");
                myDatabase.findHousesByName(houseJoinText.getText().toString().trim(), housesConsumer);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


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
                        Log.d("passwordBOYS", "Users PW: " + houseJoinPW.getText().toString().trim() + " HousePW: " + searchedHouses.get(i).getPassword());
                        if(houseJoinPW.getText().toString().trim().equals(searchedHouses.get(i).getPassword())){
                            ArrayList<House> myHouses = myDatabase.getLocalHouseArray(activity);
                            if(myHouses != null) {
                                if (!myHouses.contains(searchedHouses.get(i))) {
                                    myHouses.add(searchedHouses.get(i));
                                    myDatabase.setLocalHouseArray(myHouses, activity);
                                    popupWindow.dismiss();
                                    activity.pushFragment(FragmentId.GET(MyHousesFragment.TAG));
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


        for(House h : searchedHouses){
            Log.d("checkingHouses " + h.getName(), h.toString());
            houses.add(h.getName());
        }
        joinHouse.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Going to Feed", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(3);
            activity.startActivity(new Intent(activity, MainActivity.class));
        });
    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {
        houses.clear();
        Log.d("CheckingHouses1", Integer.toString(searchedHouses.size()));
        for(int i = 0; i < searchedHouses.size(); i= i+1){
            Log.d("CheckingHousesInside", Integer.toString(i));
            Log.d("CheckingHouses", "ADD");
            houses.add(searchedHouses.get(i).getKeyId());
        }
        adapter.notifyDataSetChanged();
    }


}
