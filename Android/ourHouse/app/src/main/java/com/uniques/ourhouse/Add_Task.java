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

public class Add_Task extends Fragment {
    private static final String TAG = "Sign_Up";
    private Button manageBackButton;
<<<<<<< Updated upstream
=======
    private Button addTask;
>>>>>>> Stashed changes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);
        Log.d(TAG, "Add Fee Clicked");

        manageBackButton = (Button) view.findViewById(R.id.manage_back);
<<<<<<< Updated upstream
=======
        addTask = (Button) view.findViewById(R.id.feed_add_task);
>>>>>>> Stashed changes

        manageBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Back", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(3);
            }
        });

<<<<<<< Updated upstream
=======
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Back", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
            }
        });

>>>>>>> Stashed changes
        return view;
    }
}

