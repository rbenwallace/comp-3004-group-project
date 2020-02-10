package com.uniques.ourhouse;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

<<<<<<< Updated upstream
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
=======
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
>>>>>>> Stashed changes

public class Settings extends Fragment {
    private static final String TAG = "Sign_Up";
    private Button manageBackButton;
<<<<<<< Updated upstream
=======
    private Button Logout;
    private Button Delete_House;
    private Button Switch_House;
    private ArrayList<String> test;
    private Button save;
    private RecyclerView recyclerView;
    private Adapter adapter;
>>>>>>> Stashed changes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
<<<<<<< Updated upstream
=======
        test = new ArrayList<>();
        test.add("Ben");
        test.add("Seb");
        test.add("Vic");
        test.add("Seb");

>>>>>>> Stashed changes
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Log.d(TAG, "Add Fee Clicked");

        manageBackButton = (Button) view.findViewById(R.id.manage_back);
<<<<<<< Updated upstream
=======
        Logout = (Button) view.findViewById(R.id.logout);
        Delete_House = (Button) view.findViewById(R.id.delete_house);
        Switch_House = (Button) view.findViewById(R.id.switch_house);
        save = (Button) view.findViewById(R.id.save_house);

        /*recyclerView = (RecyclerView) view.findViewById(R.id.rotation_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new Adapter(getActivity(),test);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = dragged.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(test, position_dragged, position_target);
                adapter.notifyItemMoved(position_dragged, position_target);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }
        });
        helper.attachToRecyclerView(recyclerView);*/
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
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(0);
            }
        });

        Delete_House.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(0);
            }
        });

        Switch_House.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
                ((LS_Main) getActivity()).setViewPager(0);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
            }
        });

>>>>>>> Stashed changes
        return view;
    }
}

