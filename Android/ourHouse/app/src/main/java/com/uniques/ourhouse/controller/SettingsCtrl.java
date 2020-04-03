package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.LS_Main;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.EditFeeFragment;
import com.uniques.ourhouse.fragment.EditTaskFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.ReadOnlyNameable;
import com.uniques.ourhouse.util.RecyclerCtrl;
import com.uniques.ourhouse.util.TextChangeListener;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SettingsCtrl implements FragmentCtrl, RecyclerCtrl<TaskRotationCard> {
    private FragmentActivity activity;
    private RecyclerView personRecycler;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private ObjectId houseId;
    private EditText houseName;
    private Button btnSwitchHouse;
    private Button settingsBackButton;
    private Button settingsSaveButton, settingsDeleteHouse;
    private TextView loader, houseKey;
    private String key;
    private CheckBox showTaskDifficultyButton;
    private CheckBox showLateTasksButton;
    private ProgressBar pd;
    private int count;

    public List<TaskRotationCard> observableCards;
    private RecyclerAdapter<TaskRotationCard> recyclerAdapter;
    private ItemTouchHelper helper;

    public SettingsCtrl(FragmentActivity activity) {
        this.activity = activity;
        helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = dragged.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(observableCards, position_dragged, position_target);
                recyclerAdapter.notifyItemMoved(position_dragged, position_target);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }
        });
    }

    @Override
    public void init(View view) {
        key = "";
        /*RelativeLayout layout = view.findViewById(R.id.login_root_display);
        pd = new ProgressBar(activity, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300, 300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(pd, params);
        pd.setVisibility(View.VISIBLE);*/
        personRecycler = view.findViewById(R.id.settings_recycler);
        loader = view.findViewById(R.id.waiter);
        loader.setVisibility(View.VISIBLE);

        count = 0;

        btnSwitchHouse = view.findViewById(R.id.settings_btnSwitchHouse);

        houseId = Settings.OPEN_HOUSE.get();

        //todo implement buttons

        settingsBackButton = (Button) view.findViewById(R.id.settings_btnBackHouse);
        settingsSaveButton = (Button) view.findViewById(R.id.settings_btnSaveHouse);
        settingsDeleteHouse = view.findViewById(R.id.settings_btnDeleteHouse);
        houseName = view.findViewById(R.id.settings_editHouseName);
        houseKey = view.findViewById(R.id.houseKeySettings);
        showTaskDifficultyButton = (CheckBox) view.findViewById(R.id.settings_chkShowDifficulty);
        showLateTasksButton = (CheckBox) view.findViewById(R.id.settings_chkShowLateTasks);


        myDatabase.getHouse(houseId, house -> {
            if (house == null){
                Log.d("checking", "NULL");
                return;
            };
            houseName.setText(house.getName());
            houseKey.setText(grabKeyId(house.getKeyId()));
            if (house.getPenalizeLateTasks()) {
                showLateTasksButton.performClick();
            }
            if (house.getShowTaskDifficulty()) {
                showTaskDifficultyButton.performClick();
            }

            observableCards = new ArrayList<>();
            for(ObjectId userId:house.getRotation().getRotation()){
                myDatabase.getUser(userId, user -> {
                    count++;
                    observableCards.add(new TaskRotationCard(user));
                    if(count == house.getRotation().getRotation().size()){
                        updateInfo();
                        loader.setVisibility(View.GONE);
                    }
                });
            }
            houseName.addTextChangedListener((TextChangeListener) (charSequence, i, i1, i2) -> {
                key = Session.keyGen();
                Session.getSession().getDatabase().checkIfHouseKeyExists(key, bool -> {
                    if (bool) {
                        houseKey.setText(key);
                    } else {
                        key = Session.keyGen();
                        houseName.setText(houseName.getText());
                    }
                });
            });

            /*observableCards.add(new TaskRotationCard(new User("Ben", "Wallace", "ben@gmail.com")));
            observableCards.add(new TaskRotationCard(new User("Seb", "Gadzinski", "seb@gmail.com")));
            observableCards.add(new TaskRotationCard(new User("Jon", "Lim", "jon@gmail.com")));
            observableCards.add(new TaskRotationCard(new User("Victor", "Olaitin", "vic@gmail.com")));*/
            Log.d("RotationFixes", "Before recycler" + house.getRotation().getRotation().toString());
            Log.d("RotationFixes", "Before recycler" + observableCards.toString());
            //pd.setVisibility(View.GONE);

            btnSwitchHouse.setOnClickListener(view13 -> {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);=
                if (Settings.OPEN_HOUSE.set(null)) {
                    Intent intent = new Intent(activity, LS_Main.class);
                    activity.startActivity(intent);
                } else {
                    throw new RuntimeException("Failed to set OPEN_HOUSE to null");
                }
            });

            settingsBackButton.setOnClickListener(view12 -> {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.popFragment(FragmentId.GET(SettingsFragment.TAG));
            });
            settingsDeleteHouse.setOnClickListener(deleteview->{
                int layoutToInflate;
                int eventButton;
                int cancelButton;
                if(house.getOccupants().size() == 1) {
                    layoutToInflate = R.layout.delete_house;
                    eventButton = R.id.deleteConfirmHouse;
                    cancelButton = R.id.deleteCancelHouse;
                }
                else {
                    layoutToInflate = R.layout.leave_house;
                    eventButton = R.id.leaveConfirmHouse;
                    cancelButton = R.id.leaveCancelHouse;
                }
                LayoutInflater inflater = (LayoutInflater)
                        activity.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(layoutToInflate, null);
                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                // dismiss the popup window when touched
                Button btnDismiss = popupView.findViewById(cancelButton);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                Button btnDeleteHouse = popupView.findViewById(eventButton);
                    Session.getSession().getDatabase().getUser(Session.getSession().getLoggedInUserId(), user ->{
                        btnDeleteHouse.setOnClickListener(view1 -> {
                            Session.getSession().getDatabase().deleteUserFromHouse(house, user, success -> {
                                if (!success) {
                                    Log.d("Deletion: ", "Failed" + house.getName());
                                    Toast.makeText(activity, "Could not leave house", Toast.LENGTH_LONG);
                                    popupWindow.dismiss();
                                } else {
                                    Log.d("Deletion: ", "Passed" + house.getName());
                                    Log.d("Deletion: ", "Houses" + user.getMyHouses().toString());
                                    Settings.OPEN_HOUSE.set(null);
                                    Intent intent = new Intent(activity, LS_Main.class)
                                            .putExtra("network_connected", Session.getSession().isNetworkConnected());
                                    activity.startActivity(intent);
                                }
                            });
                    });
                });
            });
            settingsSaveButton.setOnClickListener(view1 -> {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                ///observableCards.add(new TaskRotationCard(new ObservableString("Test")));
                //updateInfo();
                House newHouse = new House(house.getId(), house.getKeyId(), house.getOwner(), house.getName(), house.getOccupants(), house.getRotation(), house.getPassword(), house.getShowTaskDifficulty(), house.getPenalizeLateTasks());
                if (showLateTasksButton.isChecked()) {
                    newHouse.setPenalizeLateTasks(true);
                } else {
                    newHouse.setPenalizeLateTasks(false);
                }
                if (showTaskDifficultyButton.isChecked()) {
                    newHouse.setShowTaskDifficulty(true);
                } else {
                    newHouse.setShowTaskDifficulty(false);
                }
                ArrayList<ObjectId> userRotation = new ArrayList<>();
                for(TaskRotationCard user:observableCards){
                    userRotation.add(user.getObject().getId());
                }
                newHouse.getRotation().setRotation(userRotation);
                String name = houseName.getText().toString();
                newHouse.setHouseKey(houseKey.getText().toString().trim());
                newHouse.setName(name);
                myDatabase.updateHouse(newHouse, aBoolean -> {
                    if (aBoolean) {
                        Log.d(AddTaskFragment.TAG, "House saved in database");
                        Toast.makeText(activity, "House Saved", Toast.LENGTH_SHORT).show();
                        activity.popFragment(FragmentId.GET(SettingsFragment.TAG));
                    } else {
                        Log.d(AddTaskFragment.TAG, "House not saved in database");
                        Toast.makeText(activity, "House Not Saved", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private String grabKeyId(String keyId) {
        return keyId.substring(keyId.length()-5);
    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {
        RecyclerAdapter<TaskRotationCard> adapter = new RecyclerAdapter<>(
                personRecycler,
                observableCards,
                R.layout.roommate_item);
        personRecycler.setAdapter(adapter);
        setRecyclerAdapter(adapter);
    }

    @Override
    public void setRecyclerAdapter(RecyclerAdapter<TaskRotationCard> recyclerAdapter) {
        this.recyclerAdapter = recyclerAdapter;
        helper.attachToRecyclerView(recyclerAdapter.getRecyclerView());
    }

    private class ObservableString implements Observable, ReadOnlyNameable {
        private String string;

        private ObservableString(String string) {
            this.string = string;
        }

        @Override
        public String getName() {
            return string;
        }

        @Override
        public int getCompareType() {
            return Observable.STRING;
        }

        @Override
        public Comparable getCompareObject() {
            return string;
        }
    }
}
