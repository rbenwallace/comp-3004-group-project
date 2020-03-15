package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Schedule;

import org.bson.types.ObjectId;

import java.util.Calendar;
import java.util.Date;

public class AddTaskCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private ObjectId userId;
    private ObjectId houseId;

    public AddTaskCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Button addTaskBackButton = (Button) view.findViewById(R.id.addTask_btnBack);
        Button addTaskAddButton = (Button) view.findViewById(R.id.addTask_btnAdd);
        TextView taskName = (TextView) view.findViewById(R.id.addTask_editDescription);
        RadioGroup taskFrequencies = (RadioGroup) view.findViewById(R.id.addTask_radioFrequency);
        RadioGroup taskDifficulty = (RadioGroup) view.findViewById(R.id.addTask_radioDifficulty);
        TextView otherTaskFrequency = (TextView) view.findViewById(R.id.addTask_editNumberOfDays);
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.addTask_datePicked);
        TextView taskViewTitle = (TextView) view.findViewById(R.id.addTask_title);

        userId = Session.getSession().getLoggedInUserId();
        houseId = Settings.OPEN_HOUSE.get();

        taskViewTitle.setText("Add Task");
        addTaskAddButton.setText("ADD");

        addTaskBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(ManageFragment.TAG));
            }
        });
        addTaskAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                String selectedFrequencyText = ((RadioButton) view.findViewById(taskFrequencies.getCheckedRadioButtonId())).getText().toString();
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, 23, 59, 59);
                calendar.set(Calendar.MILLISECOND, 0);
                Date date = calendar.getTime();
                if(String.valueOf(taskName.getText()).equals("") || (selectedFrequencyText.equals("Other") && String.valueOf(otherTaskFrequency.getText()).equals(""))){
                    Toast.makeText(activity, "Please fill out the whole form", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!date.after(Calendar.getInstance().getTime())){
                    Toast.makeText(activity, "Please choose a date later than today", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = String.valueOf(taskName.getText());
                String selectedDifficulty = ((RadioButton) view.findViewById(taskDifficulty.getCheckedRadioButtonId())).getText().toString();
                int selectedDifficultyNum = 1;
                if(selectedDifficulty.equals("Medium")){
                    selectedDifficultyNum = 2;
                }
                else if(selectedDifficulty.equals("Hard")){
                    selectedDifficultyNum = 3;
                }
                Schedule schedule = new Schedule();
                if(selectedFrequencyText.equals("Once")){
                    Schedule.pauseStartEndBoundsChecking();
                    schedule.setStart(date);
                    schedule.setEnd(date);
                    Schedule.resumeStartEndBoundsChecking();
                    schedule.setEndType(Schedule.EndType.ON_DATE);
                }
                else{
                    Schedule.pauseStartEndBoundsChecking();
                    schedule.setStart(date);
                    schedule.setEndPseudoIndefinite();
                    Schedule.resumeStartEndBoundsChecking();
                    if(selectedFrequencyText.equals("Other")){
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.DAILY);
                        schedule.getRepeatSchedule().setDelay(Integer.parseInt(String.valueOf(otherTaskFrequency.getText())));
                    }
                    else if(selectedFrequencyText.equals("Yearly")){
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.YEARLY);
                    }
                    else if(selectedFrequencyText.equals("Monthly")){
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
                    }
                    else if(selectedFrequencyText.equals("Weekly")){
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
                    }
                    else if(selectedFrequencyText.equals("Daily")){
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.DAILY);
                    }
                    schedule.setEndType(Schedule.EndType.AFTER_TIMES);
                }
                Task task = new Task(userId, houseId, name, schedule, selectedDifficultyNum);
                myDatabase.postTask(task, bool->{
                    if(bool){
                        Log.d(AddTaskFragment.TAG, "Task Added to Database");
                        Toast.makeText(activity, "Task Added", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d(AddTaskFragment.TAG, "Task not received by Database");
                        Toast.makeText(activity, "Task Not Added", Toast.LENGTH_SHORT).show();
                    }
                });
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
        });
    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {

    }
}
