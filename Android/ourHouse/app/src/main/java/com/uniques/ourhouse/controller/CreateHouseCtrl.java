package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.MongoDB;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.TextChangeListener;

import java.util.ArrayList;

public class CreateHouseCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private EditText houseName, password, confirmPassword;
    private Button createHouse;
    private CheckBox taskDiff;
    private CheckBox penLateTasks;
    private TextView keyNumber;
    private String key;
    private User myUser;
//    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private MongoDB myDatabase = new MongoDB();

    public CreateHouseCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        key = "";
        myDatabase = new MongoDB();
        keyNumber = (TextView) view.findViewById(R.id.houseKey);
        myUser = myDatabase.getCurrentLocalUser(activity);
        houseName = (EditText) view.findViewById(R.id.houseName_create);
        password = (EditText) view.findViewById(R.id.password1CH);
        confirmPassword = (EditText) view.findViewById(R.id.password2CH);
        createHouse = (Button) view.findViewById(R.id.createBtn);
        taskDiff = (CheckBox) view.findViewById(R.id.showDifficulty);
        penLateTasks = (CheckBox) view.findViewById(R.id.penalizeLateTasks);

        ArrayList<String> users = new ArrayList<>();
        ArrayAdapter adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, users);

        House.Rotation rotation = new House.Rotation();
        ArrayList<User> occupants = new ArrayList<User>();
        occupants.add(myUser);
        rotation.addUserToRotation(myUser);

        houseName.addTextChangedListener(new TextChangeListener() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                key = Session.keyGen();
                myDatabase.checkKey(key, bool ->{
                    if(bool){
                        keyNumber.setText(key);
                    }
                    else{
                        key = Session.keyGen();
                        houseName.setText(houseName.getText());
                    }
                });
            }
        });
        password.addTextChangedListener(new TextChangeListener() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                DrawableCompat.setTint(password.getBackground(), ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                DrawableCompat.setTint(confirmPassword.getBackground(), ContextCompat.getColor(activity, R.color.colorPrimaryDark));

            }
        });


        createHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!passwordCorrect()){
                    DrawableCompat.setTint(password.getBackground(), ContextCompat.getColor(activity, R.color.red));
                    DrawableCompat.setTint(confirmPassword.getBackground(), ContextCompat.getColor(activity, R.color.red));
//                    password.setBackgroundColor(Color.RED);
//                    confirmPassword.setBackgroundColor(Color.RED);
                    return;
                }
                House newHouse = new House(houseName.getText().toString().trim(), myUser, occupants, rotation, password.getText().toString().trim(), taskDiff.isChecked(), penLateTasks.isChecked());
                //add the house to the array
                ArrayList<House> myHouses = myDatabase.getLocalHouseArray(activity);
                if(myHouses == null) {
                    myHouses = new ArrayList<>();
                    myHouses.add(newHouse);
                }
                else {
                    myHouses.add(newHouse);
                }
                final ArrayList<House> HousesFinal = myHouses;
                myDatabase.addMyHouse(newHouse, activity, bool ->{
                    if(bool){
                        User myUser = myDatabase.getCurrentLocalUser(activity);
                        myUser.addHouseId(newHouse.getId());
                        myDatabase.setLocalUser(myUser, activity);
                        myUser = myDatabase.getCurrentLocalUser(activity);
                        myDatabase.setLocalHouseArray(HousesFinal, activity);
                        ArrayList<House> wow = myDatabase.getLocalHouseArray(activity);
                        Intent intent = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                    else {
                        Log.d("CreateHouseCrtl", "adding houses error");
                    }
                });
            }
            myDatabase.addMyHouse(newHouse, activity, boolConsumer);
        });
    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {

    }
    public boolean passwordCorrect(){
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();
        if(passwordText.length() > 0){
            if(passwordText.equals(confirmPasswordText))return true;
            else return false;
        }
        return false;
    }

}

