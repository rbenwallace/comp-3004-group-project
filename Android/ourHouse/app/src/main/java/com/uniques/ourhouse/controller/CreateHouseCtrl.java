package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;

import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.SettingsFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.MongoDB.MongoDB;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.function.Consumer;

import static android.content.Context.MODE_PRIVATE;

public class CreateHouseCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private EditText houseName, roomateName;
    private Button createHouse, sendRoomateEmailBtn;
    private ListView roomates;
    private CheckBox taskDiff;
    private CheckBox penLateTasks;
    private TextView keyNumber;
    private String key;
    private User myUser;
    private MongoDB myDatabase;

    public CreateHouseCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        key = "";
        myDatabase = new MongoDB();
        keyNumber = (TextView) view.findViewById(R.id.houseKey);
        Consumer<Boolean> checkFunction = bool-> {
            if(bool){
                keyNumber.setText(key);
            }
            else{
                key = myDatabase.keyGen();
                houseName.setText(houseName.getText());
            }
        };
        Consumer<Boolean> boolConsumer = bool -> {
            if(bool){
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            else {
                Log.d("CreateHouseCrtl", "adding houses error");
            }
        };
        myUser = MongoDB.getCurrentLocalUser(activity);
        houseName = (EditText) view.findViewById(R.id.houseName_create);
        roomateName = (EditText) view.findViewById(R.id.addRoomate);
        createHouse = (Button) view.findViewById(R.id.createBtn);
        roomates = (ListView) view.findViewById(R.id.addRoomatesList);
        taskDiff = (CheckBox) view.findViewById(R.id.showDifficulty);
        penLateTasks = (CheckBox) view.findViewById(R.id.penalizeLateTasks);
        sendRoomateEmailBtn = (Button) view.findViewById(R.id.sendRoomateEmail);

        ArrayList<String> users = new ArrayList<>();
        ArrayAdapter adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, users);



        sendRoomateEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!roomateName.getText().toString().trim().equals("")){
                    if(!users.contains(roomateName.getText().toString().trim())) {
                        users.add(roomateName.getText().toString().trim());
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        House.Rotation rotation = new House.Rotation();
        ArrayList<User> occupants = new ArrayList<User>();
        occupants.add(myUser);
        rotation.addUserToRotation(myUser);
        roomates.setAdapter(adapter);
        houseName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                key = myDatabase.keyGen();
                myDatabase.checkKey(key, checkFunction);
            }
        });
        createHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                House newHouse = new House(houseName.getText().toString().trim(), myUser, occupants, rotation, taskDiff.isChecked(), penLateTasks.isChecked());
                //add the house to the array
                ArrayList<House> myHouses = myDatabase.getLocalHouseArray(activity);
                if(myHouses == null) {
                    myHouses = new ArrayList<>();
                    myHouses.add(newHouse);
                    Log.d("checkingHouses ADD", myHouses.toString());
                    myDatabase.updateLocalHouseArray(myHouses, activity);
                }
                else {
                    myHouses.add(newHouse);
                    Log.d("checkingHouses ADD", myHouses.toString());
                    myDatabase.updateLocalHouseArray(myHouses, activity);
                }
                myDatabase.addMyHouse(newHouse, activity, boolConsumer);
            }
        });
    }

    @Override
    public void updateInfo() {

    }

}

