package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.LS_Main;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.ReadOnlyNameable;
import com.uniques.ourhouse.util.RecyclerCtrl;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsCtrl implements FragmentCtrl, RecyclerCtrl<TaskRotationCard> {
    private FragmentActivity activity;
    private RecyclerView personRecycler;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private ObjectId houseId;
    private Button btnSwitchHouse;
    private Button settingsBackButton;
    private Button settingsSaveButton;
    private TextView houseName;
    private CheckBox showTaskDifficultyButton;
    private CheckBox showLateTasksButton;

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
        RecyclerView personRecycler = view.findViewById(R.id.settings_recycler);

        btnSwitchHouse = view.findViewById(R.id.settings_btnSwitchHouse);

        houseId = Settings.OPEN_HOUSE.get();

        //todo implement buttons

        settingsBackButton = (Button) view.findViewById(R.id.settings_btnBackHouse);
        settingsSaveButton = (Button) view.findViewById(R.id.settings_btnSaveHouse);
        houseName = (TextView) view.findViewById(R.id.settings_editHouseName);
        showTaskDifficultyButton = (CheckBox) view.findViewById(R.id.settings_chkShowDifficulty);
        showLateTasksButton = (CheckBox) view.findViewById(R.id.settings_chkShowLateTasks);

        myDatabase.getHouse(houseId, house -> {
            if (house == null) throw new RuntimeException("OPEN_HOUSE failed to load object");
            houseName.setText(house.getName());
            if (house.getPenalizeLateTasks()) {
                showLateTasksButton.performClick();
            }
            if (house.getShowTaskDifficulty()) {
                showTaskDifficultyButton.performClick();
            }
            System.out.println("wallace occupants: " + house.getOccupants().size());
            observableCards = new ArrayList<>();
            /*for(User user:house.getRotation().getRotation()){
                observableCards.add(new TaskRotationCard(user));
            }*/
            observableCards.add(new TaskRotationCard(new User("Ben", "Wallace", "ben@gmail.com")));
            observableCards.add(new TaskRotationCard(new User("Seb", "Gadzinski", "seb@gmail.com")));
            observableCards.add(new TaskRotationCard(new User("Jon", "Lim", "jon@gmail.com")));
            observableCards.add(new TaskRotationCard(new User("Victor", "Olaitin", "vic@gmail.com")));

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
                activity.pushFragment(FragmentId.GET(ManageFragment.TAG));
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
                /*ArrayList<User> userRotation = new ArrayList<>();
                for(TaskRotationCard user:observableCards){
                    userRotation.add(user.getObject());
                }
                newHouse.getRotation().setRotation(userRotation);*/
                String name = houseName.getText().toString();
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

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {
        /*RecyclerAdapter<TaskRotationCard> adapter = new RecyclerAdapter<>(
                personRecycler,
                observableCards,
                R.layout.roommate_item);
        personRecycler.setAdapter(adapter);
        setRecyclerAdapter(adapter);*/
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
