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

public class Settings extends Fragment {
    private static final String TAG = "Sign_Up";
    private Button manageBackButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Log.d(TAG, "Add Fee Clicked");

        manageBackButton = (Button) view.findViewById(R.id.manage_back);

        manageBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Back", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(3);
            }
        });

        return view;
    }
}

