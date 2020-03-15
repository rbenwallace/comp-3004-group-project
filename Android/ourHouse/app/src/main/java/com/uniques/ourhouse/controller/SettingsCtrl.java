package com.uniques.ourhouse.controller;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.LS_Main;
import com.uniques.ourhouse.MainActivity;
import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.LoginFragment;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.MyHousesFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.session.MongoDB;
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
    private MongoDB myDatabase = new MongoDB();
    private ObjectId houseId;

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

        Button btnSwitchHouse = view.findViewById(R.id.settings_btnSwitchHouse);

        houseId = myDatabase.getCurrentLocalHouse(this.activity).getId();

        //todo implement buttons

        observableCards = new ArrayList<>();
        observableCards.add(new TaskRotationCard(new ObservableString("Ben")));
        observableCards.add(new TaskRotationCard(new ObservableString("Seb")));
        observableCards.add(new TaskRotationCard(new ObservableString("Vic")));
        observableCards.add(new TaskRotationCard(new ObservableString("Jon")));

        Button settingsBackButton = (Button) view.findViewById(R.id.settings_btnBackHouse);
        Button settingsSaveButton = (Button) view.findViewById(R.id.settings_btnSaveHouse);
        TextView houseName = (TextView) view.findViewById(R.id.settings_editHouseName);
        CheckBox showTaskDifficultyButton = (CheckBox) view.findViewById(R.id.settings_chkShowDifficulty);
        CheckBox showLateTasksButton = (CheckBox) view.findViewById(R.id.settings_chkShowLateTasks);

        House house = myDatabase.getCurrentLocalHouse(activity);
        houseName.setText(house.getName());
        if(house.getPenalizeLateTasks()){ showLateTasksButton.performClick(); }
        if(house.getShowTaskDifficulty()){ showTaskDifficultyButton.performClick(); }

        btnSwitchHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);=
                myDatabase.clearLocalCurHouse(activity);
                Intent intent = new Intent(activity, LS_Main.class);
                activity.startActivity(intent);
            }
        });

        settingsBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(ManageFragment.TAG));
            }
        });
        settingsSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                ///observableCards.add(new TaskRotationCard(new ObservableString("Test")));
                //updateInfo();
                if(showLateTasksButton.isChecked()){
                    house.setPenalizeLateTasks(true);
                }
                else{
                    house.setPenalizeLateTasks(false);
                }
                if(showTaskDifficultyButton.isChecked()){
                    house.setShowTaskDifficulty(true);
                }
                else{
                    house.setShowTaskDifficulty(false);
                }
                String name = houseName.getText().toString();
                house.setName(name);
                myDatabase.updateHouse(house, aBoolean -> {
                    if(aBoolean){
                        Log.d(AddTaskFragment.TAG, "House saved in database");
                        Toast.makeText(activity, "House Saved", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d(AddTaskFragment.TAG, "House not saved in database");
                        Toast.makeText(activity, "House Not Saved", Toast.LENGTH_SHORT).show();
                    }
                });
                myDatabase.setLocalHouse(house, activity);
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
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
