package com.uniques.ourhouse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class Sign_Up extends Fragment {
    private static final String TAG = "Sign_Up";
    private Button backTOLogin;
    private Button signUpBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        backTOLogin = (Button) view.findViewById(R.id.already_user);
        signUpBtn = (Button) view.findViewById(R.id.signUpBtn);
        Log.d(TAG, "onCreatedView: Started");
        backTOLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Going to Login", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(0);
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO CHECK FOR VERIFICATION
                Toast.makeText(getActivity(), "Signed Up", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT Main Activity
            }
        });

        return view;
    }
}
