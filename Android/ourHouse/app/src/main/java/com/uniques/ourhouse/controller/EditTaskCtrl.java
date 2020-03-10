package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.EditTaskFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.MongoDB;
import com.uniques.ourhouse.util.Schedule;

import org.bson.types.ObjectId;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

public class EditTaskCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private MongoDB myDatabase = new MongoDB();
    private String taskIdStr = "";
    private ObjectId userId;
    private ObjectId houseId;
    private ObjectId taskId;
    private Task modifiedTask;

    public EditTaskCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(AddTaskFragment.TAG, "Edit Task Clicked");
        Button editTaskBackButton = (Button) view.findViewById(R.id.addTask_btnBack);
        TextView taskName = (TextView) view.findViewById(R.id.addTask_editDescription);
        RadioGroup taskFrequencies = (RadioGroup) view.findViewById(R.id.addTask_radioFrequency);
        RadioButton onceButton = (RadioButton) view.findViewById(R.id.addTask_once);
        RadioButton dailyButton = (RadioButton) view.findViewById(R.id.addTask_daily);
        RadioButton weeklyButton = (RadioButton) view.findViewById(R.id.addTask_weekly);
        RadioButton monthlyButton = (RadioButton) view.findViewById(R.id.addTask_monthly);
        RadioButton yearlyButton = (RadioButton) view.findViewById(R.id.addTask_yearly);
        RadioButton otherButton = (RadioButton) view.findViewById(R.id.addTask_other);
        EditText editNumberOfDays = (EditText) view.findViewById(R.id.addTask_editNumberOfDays);
        RadioGroup taskDifficulty = (RadioGroup) view.findViewById(R.id.addTask_radioDifficulty);
        RadioButton easyButton = (RadioButton) view.findViewById(R.id.addTask_easy);
        RadioButton mediumButton = (RadioButton) view.findViewById(R.id.addTask_medium);
        RadioButton hardButton = (RadioButton) view.findViewById(R.id.addTask_hard);
        TextView otherTaskFrequency = (TextView) view.findViewById(R.id.addTask_editNumberOfDays);
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.addTask_datePicked);
        TextView taskViewTitle = (TextView) view.findViewById(R.id.addTask_title);
        Button saveTask = (Button) view.findViewById(R.id.addTask_btnAdd);

        taskViewTitle.setText("Edit Task");
        saveTask.setText("SAVE");

        userId = myDatabase.getCurrentLocalUser(this.activity).getId();
        houseId = myDatabase.getCurrentLocalHouse(this.activity).getId();

        taskId = new ObjectId("5e60ad4613d8ee3d69f5004c");
        taskIdStr = "5e60ad4613d8ee3d69f5004c";

        if(taskIdStr.equals("")){
            Log.d(EditTaskFragment.TAG, "Task Id not received");
            Toast.makeText(activity, "Task Currently Not Editable", Toast.LENGTH_SHORT).show();
            //activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
        }
        else{
            Log.d(EditTaskFragment.TAG, "Task: " + taskId + " received");
        }

        myDatabase.getTask(taskId, task -> {
            modifiedTask = new Task(userId, houseId, task.getName(), task.getSchedule(), task.getDifficulty());
            taskName.setText(modifiedTask.getName());
            int difficulty = modifiedTask.getDifficulty();
            if(difficulty == 1){
                easyButton.performClick();
            }
            else if(difficulty == 2){
                mediumButton.performClick();
            }
            else{
                hardButton.performClick();
            }
            Schedule schedule = modifiedTask.getSchedule();
            if(schedule.getEndType().equals(Schedule.EndType.ON_DATE)){
                onceButton.performClick();
            }
            else{
                if(schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.YEARLY)){
                    yearlyButton.performClick();
                }
                else if(schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.MONTHLY)){
                    monthlyButton.performClick();
                }
                else if(schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.WEEKLY)){
                    weeklyButton.performClick();
                }
                else{
                    if(schedule.getRepeatSchedule().getDelay() == 1){
                        dailyButton.performClick();
                    }
                    else{
                        otherButton.performClick();
                        editNumberOfDays.setText(schedule.getRepeatSchedule().getDelay());
                    }
                }
            }
        });


        editTaskBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
        });
        saveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                myDatabase.updateTask(modifiedTask, bool->{
                    if(bool){
                        Log.d(AddTaskFragment.TAG, "Task updated in the Database");
                    }
                    else{
                        Log.d(AddTaskFragment.TAG, "Task not updated in the  Database");
                    }
                });
                Toast.makeText(activity, "Task Saved", Toast.LENGTH_SHORT).show();
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
        });
    }

    @Override
    public void acceptArguments(Object... args) {
        if(!(args[0] == null)){
            taskIdStr = args[0].toString();
            taskId = new ObjectId(args[0].toString());
        }
    }

    @Override
    public void updateInfo() {

    }
}
