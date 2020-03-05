package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.fragment.SettingsFragment;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.ReadOnlyNameable;
import com.uniques.ourhouse.util.RecyclerCtrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsCtrl implements FragmentCtrl, RecyclerCtrl<TaskRotationCard> {
    private FragmentActivity activity;
    private RecyclerView personRecycler;

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
        Log.d(SettingsFragment.TAG, "Add Fee Clicked");
        RecyclerView personRecycler = view.findViewById(R.id.settings_recycler);

        Button btnLogout = view.findViewById(R.id.settings_btnLogout);
        Button btnSwitchHouse = view.findViewById(R.id.settings_btnDeleteHouse);
        Button btnDeleteHouse = view.findViewById(R.id.settings_btnSwitchHouse);

        //todo implement buttons

        observableCards = new ArrayList<>();
        observableCards.add(new TaskRotationCard(new ObservableString("Ben")));
        observableCards.add(new TaskRotationCard(new ObservableString("Seb")));
        observableCards.add(new TaskRotationCard(new ObservableString("Vic")));
        observableCards.add(new TaskRotationCard(new ObservableString("Jon")));

        Button settingsBackButton = (Button) view.findViewById(R.id.settings_btnBackHouse);
        Button settingsSaveButton = (Button) view.findViewById(R.id.settings_btnSaveHouse);

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
                observableCards.add(new TaskRotationCard(new ObservableString("Test")));
                updateInfo();
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
