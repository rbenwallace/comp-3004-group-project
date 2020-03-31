package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.TextChangeListener;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.uniques.ourhouse.controller.LoginCtrl.regEx;

public class CreateHouseCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private EditText houseName, password, confirmPassword, emailFriend;
    private ArrayList<String> friends;
    private Button createHouse, addEmail;
    private CheckBox taskDiff;
    private CheckBox penLateTasks;
    private TextView keyNumber;
    private String key;
    private User myUser;
    private ListView friendList;
    private ArrayAdapter<String> adapter;
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
        emailFriend =  view.findViewById(R.id.friendEmail);
        addEmail = view.findViewById(R.id.addEmail);
        friendList =  view.findViewById(R.id.friendsList);
        friends = new ArrayList<>();
        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, friends);
        friendList.setAdapter(adapter);
        House.Rotation rotation = new House.Rotation();
        ArrayList<ObjectId> occupants = new ArrayList<>();
        occupants.add(myUser.getId());
        rotation.addUserToRotation(myUser);

        addEmail.setOnClickListener(onclick->{
            if(verifyEmail(emailFriend.getText().toString().trim()) && !friends.contains(emailFriend.getText().toString().trim())){
                Log.d("EmailVerified", "Sending");
                friends.add(emailFriend.getText().toString().trim());
                emailFriend.setText("");
                updateInfo();
            }
            else {
                DrawableCompat.setTint(emailFriend.getBackground(), ContextCompat.getColor(activity, R.color.red));
            }
        });

        emailFriend.addTextChangedListener((TextChangeListener) (charSequence, i, i1, i2) -> {
            DrawableCompat.setTint(emailFriend.getBackground(), ContextCompat.getColor(activity, R.color.colorPrimary));
        });

        friendList.setOnItemLongClickListener((adapterView, view13, i, l) -> {
            LayoutInflater inflater = (LayoutInflater)
                    activity.getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.delete_email, null);
            // create the popup window
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(view13, Gravity.CENTER, 0, 0);
            // dismiss the popup window when touched
            Button btnDismiss = popupView.findViewById(R.id.deleteCancelEmail);
            btnDismiss.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
            Button btnDeleteHouse = popupView.findViewById(R.id.deleteEmail);
            btnDeleteHouse.setOnClickListener(view131 -> {
                friends.remove(i);
                popupWindow.dismiss();
            });
            return true;
        });


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
            database.getUser(Session.getSession().getLoggedInUserId(), myUser -> {
                if (!passwordCorrect()) {
                    DrawableCompat.setTint(password.getBackground(), ContextCompat.getColor(activity, R.color.red));
                    DrawableCompat.setTint(confirmPassword.getBackground(), ContextCompat.getColor(activity, R.color.red));
                    return;
                }
                House newHouse = new House(houseName.getText().toString().trim(), myUser.getId(), occupants, rotation, password.getText().toString().trim(), taskDiff.isChecked(), penLateTasks.isChecked());

                for(String email : friends){
                    database.emailFriends(email, email.split("@")[0], myUser.getFirstName(), newHouse.getId(),  bool ->{
                        if (bool)
                            Log.d("EmailingFriends", email + "Emailed");
                        else
                            Log.d("EmailingFriends", email + "Failed to Email");
                    });
                }

                // add the new house to the user's houses
                myUser.addHouse(newHouse.getId());
                database.postHouse(newHouse, successful -> {
                    if (successful) {
                        database.updateUser(myUser, successful2 -> {
                            if (successful2) {
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
        });
    }

    private boolean verifyEmail(String email) {
        Pattern p = Pattern.compile(regEx);

        Matcher m = p.matcher(email);

        // Check for both field is empty or not
        if (email.equals("") || email.length() == 0){
            return false;

        }
        // Check if email id is valid or not
        else if (!m.find()) {
            return false;
        }
        else
            return true;
    }

    @Override
    public void acceptArguments(Object... args) {
    }

    @Override
    public void updateInfo() {
        adapter.notifyDataSetChanged();
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

