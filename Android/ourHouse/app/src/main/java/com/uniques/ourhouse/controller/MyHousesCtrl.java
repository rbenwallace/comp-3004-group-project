package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.MongoDB.MongoDB;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MyHousesCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private MongoDB myDatabase;
    private ListView housesList;
    private House selectedHouse;
    private ArrayList<House> myHouses;
    private Button logoutBtn;

    public MyHousesCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        myDatabase = new MongoDB();
        housesList = (ListView) view.findViewById(R.id.myHousesList);
        Button createHouse = view.findViewById(R.id.createHouseBtn);
        Button joinHouse = view.findViewById(R.id.joinHouseBtn);
        logoutBtn = (Button) view.findViewById(R.id.logoutBtnMH);
        //Gather houses if there are any from the shared pref Houses
        myHouses = myDatabase.getLocalHouseArray(activity);
        Log.d("checkingHouses", myHouses.toString());
        if(myHouses == null){
            myHouses = new ArrayList<House>();
            myDatabase.updateLocalHouseArray(myHouses, activity);
        }
        ArrayList<String> houses = new ArrayList<>();
        ArrayAdapter adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, houses);
        housesList.setAdapter(adapter);
        for(House h : myHouses){
            Log.d("checkingHouses " + h.getName(), h.toString());
            houses.add(h.getName());
        }
        adapter.notifyDataSetChanged();
        //Selected a house, change the current house to that house and enter main activity
        housesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHouse = myHouses.get(i);
                myDatabase.setLocalHouse(selectedHouse, activity);
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
        });
        housesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
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
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                // dismiss the popup window when touched
                Button btnDismiss = (Button) popupView.findViewById(R.id.deleteCancelHouse);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                Button btnDeleteHouse = (Button) popupView.findViewById(R.id.deleteConfirmHouse);
                btnDeleteHouse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        House temp = myHouses.get(i);
                        houses.remove(i);
                        //If its the current House change it and delete, else just delete
                        if(myHouses.get(i) == myDatabase.getCurrentLocalHouse(activity)){
                            //TODO set to something right
                            myHouses.remove(i);
                            if(myHouses.size() > 0){
                                myDatabase.setLocalHouse(myHouses.get(1), activity);
                            }
                        }
                        else
                            myHouses.remove(i);
                        //Remove From lists and notify Listview
                        User myUser = MongoDB.getCurrentLocalUser(activity);
                        myUser.removeHouseId(temp.getId());
                        myDatabase.updateLocalHouseArray(myHouses, activity);
                        adapter.notifyDataSetChanged();
                        popupWindow.dismiss();
                    }
                });
                return true;
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDatabase.clearLocalHouses(activity);
                myDatabase.clearLocalCurHouse(activity);
                myDatabase.clearLocalCurUser(activity);
                myDatabase.getAuth().logout();
            }
        });

        createHouse.setOnClickListener(view1 -> {
            Toast.makeText(activity, "Going to CreateHouseFragment", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
            activity.pushFragment(FragmentId.GET(CreateHouseFragment.TAG));
        });
        joinHouse.setOnClickListener(view2 -> {
            Toast.makeText(activity, "Going to JoinHouseFragment", Toast.LENGTH_LONG).show();
            //TODO NAVIGATE TO NEXT FRAGMENT
//            ((LS_Main) activity).setViewPager(3);
            activity.pushFragment(FragmentId.GET(JoinHouseFragment.TAG));
        });
    }

    @Override
    public void updateInfo() {

    }
}
