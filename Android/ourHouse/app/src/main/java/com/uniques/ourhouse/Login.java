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

public class Login extends Fragment {

    private static final String TAG = "Login";
    private Button backToSignUp;
    private Button forgetPassword;
    private Button loginBtn;
    private Button manageButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        Log.d("titsMagee", view.toString());
        backToSignUp = (Button) view.findViewById(R.id.createAccount);
        loginBtn = (Button) view.findViewById(R.id.loginBtn);
        forgetPassword = (Button) view.findViewById(R.id.forgot_password);
        manageButton = (Button) view.findViewById(R.id.manageButton);
        Log.d("titsMagee", "onCreatedView: Started");
        backToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Going to Sign Up", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
                ((LS_Main)getActivity()).setViewPager(1);
            }
        });
//        backToSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getActivity(), "Going to Sign Up", Toast.LENGTH_LONG).show();
//                //TODO NAVIGATE TO NEXT FRAGMENT
//                //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
//                ((LS_Main) getActivity()).setViewPager(1);
//            }
//        });
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Going to find my pw", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
                ((LS_Main) getActivity()).setViewPager(2);
            }
        });

        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Going to find my pw", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT FRAGMENT
                //((LS_Main) getActivity()) gives us access to any methods that are inside the activity
                ((LS_Main) getActivity()).setViewPager(3);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO CHECK FOR VERIFICATION
                Toast.makeText(getActivity(), "Logging in", Toast.LENGTH_LONG).show();
                //TODO NAVIGATE TO NEXT Main Activity
            }
        });

        return view;
    }
}

