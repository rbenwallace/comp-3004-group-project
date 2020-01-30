package com.uniques.ourhouse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Forget_Password extends Fragment {
    private static final String TAG = "Sign_Up";
    private Button backTOLogin;
    private Button signUpBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget_password, container, false);
        backTOLogin = (Button) view.findViewById(R.id.backToLoginBtn);
        Log.d(TAG, "onCreatedView: Started");
        backTOLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Going to Login", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(0);
            }
        });

        return view;
    }
}

