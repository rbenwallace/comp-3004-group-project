package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.TextChangeListener;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import org.bson.types.ObjectId;

public class CreateHouseCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private EditText houseName, password, confirmPassword;
    private Button createHouse;
    private CheckBox taskDiff;
    private CheckBox penLateTasks;
    private TextView keyNumber;
    private String key;
    private User myUser;
    private DatabaseLink database = Session.getSession().getDatabase();

    public CreateHouseCtrl(FragmentActivity activity) {
        this.activity = activity;
    }
    @Override
    public void init(View view) {
        key = "";

        myUser = Session.getSession().getLoggedInUser();
        Log.d("myHouses", myUser.getMyHouses().toString());

        keyNumber = view.findViewById(R.id.houseKey);
        houseName = view.findViewById(R.id.houseName_create);
        password = view.findViewById(R.id.password1CH);
        confirmPassword = view.findViewById(R.id.password2CH);
        createHouse = view.findViewById(R.id.createBtn);
        taskDiff = view.findViewById(R.id.showDifficulty);
        penLateTasks = view.findViewById(R.id.penalizeLateTasks);

        ArrayList<String> users = new ArrayList<>();
        ArrayAdapter adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, users);

        House.Rotation rotation = new House.Rotation();
        ArrayList<User> occupants = new ArrayList<>();
        occupants.add(myUser);
        rotation.addUserToRotation(myUser);

        houseName.addTextChangedListener((TextChangeListener) (charSequence, i, i1, i2) -> {
            key = Session.keyGen();
            database.checkIfHouseKeyExists(key, bool -> {
                if (bool) {
                    keyNumber.setText(key);
                } else {
                    key = Session.keyGen();
                    houseName.setText(houseName.getText());
                }
            });
        });
        password.addTextChangedListener((TextChangeListener) (charSequence, i, i1, i2) -> {
            DrawableCompat.setTint(password.getBackground(), ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            DrawableCompat.setTint(confirmPassword.getBackground(), ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        });


        createHouse.setOnClickListener(view1 -> {
            if (!passwordCorrect()) {
                DrawableCompat.setTint(password.getBackground(), ContextCompat.getColor(activity, R.color.red));
                DrawableCompat.setTint(confirmPassword.getBackground(), ContextCompat.getColor(activity, R.color.red));
//                    password.setBackgroundColor(Color.RED);
//                    confirmPassword.setBackgroundColor(Color.RED);
                return;
            }
            House newHouse = new House(houseName.getText().toString().trim(), myUser, occupants, rotation, password.getText().toString().trim(), taskDiff.isChecked(), penLateTasks.isChecked());
            //add the house to the array (DatabaseCoordinator handles all this)
//            ArrayList<House> myHouses = myDatabase.getLocalHouseArray(activity);
//            if(myHouses == null) {
//                myHouses = new ArrayList<>();
//                myHouses.add(newHouse);
//            }
//            else {
//                myHouses.add(newHouse);
//            }
//            final ArrayList<House> HousesFinal = myHouses;
//            myDatabase.addMyHouse(newHouse, activity, bool ->{
//                if(bool){
//                    // add the new house to the user's houses
//                    User myUser = Session.getSession().getLoggedInUser();
//                    myUser.addHouseId(newHouse.getId());
//                    Session.ge
//
//                    Intent intent = new Intent(activity, MainActivity.class);
//                    activity.startActivity(intent);
//                    activity.finish();
//                }
//                else {
//                    Log.d("CreateHouseCrtl", "adding houses error");
//                }
//            });

            // add the new house to the user's houses
            User myUser = Session.getSession().getLoggedInUser();
            myUser.addHouseId(newHouse.getId());
            myUser.addHouseId(new ObjectId());
            myUser.addHouseId(new ObjectId());
            Log.d("myHouses", myUser.getMyHouses().toString());
            database.postHouse(newHouse, successful -> {
                if (successful) {
                    database.updateUser(myUser, successful2 -> {
                        if (successful2) {
                            Log.d("myHouses", myUser.getMyHouses().toString());
                            //Need to save the user in the session but this breaks the code:
                                //Session.getSession().setLoggedInUser(myUser);
                            Settings.OPEN_HOUSE.set(newHouse.getId());
                            Intent intent = new Intent(activity, MainActivity.class);
                            activity.startActivity(intent);
                            activity.finish();
                        } else {
                            Log.d("CreateHouseCtrl", "adding new house to user's houses error");
                        }
                    });
                } else {
                    Log.d("CreateHouseCtrl", "adding houses error");
                }
            });
        });
    }

    @Override
    public void acceptArguments(Object... args) {
    }

    @Override
    public void updateInfo() {
    }

    private boolean passwordCorrect() {
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();
        if (passwordText.length() > 0) {
            return passwordText.equals(confirmPasswordText);
        }
        return false;
    }

}

