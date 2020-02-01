package com.uniques.ourhouse;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Manage_Homescreen extends Fragment {
    private static final String TAG = "Sign_Up";
    private Button addFeeButton;
    private Button addTaskButton;
    private Button settingsButton;
    private Button manageSwitchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);
        addFeeButton = (Button) view.findViewById(R.id.add_fee);
        addTaskButton = (Button) view.findViewById(R.id.add_task);
        settingsButton = (Button) view.findViewById(R.id.settings);
        manageSwitchButton = (Button) view.findViewById(R.id.manage_back);
        Log.d(TAG, "onCreatedView: Started");
        addFeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Add Fee", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(4);
            }
        });
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Add Task", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(5);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Settings", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(6);
            }
        });
        manageSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Back", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(0);
            }
        });

        return view;
    }
}

