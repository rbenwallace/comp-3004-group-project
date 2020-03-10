package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;
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
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.session.MongoDB;
import com.uniques.ourhouse.util.Schedule;

import org.bson.types.ObjectId;

import java.util.Calendar;
import java.util.Date;

public class EditTaskCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private MongoDB myDatabase = new MongoDB();
    private String taskIdStr = "";
    private ObjectId userId;
    private ObjectId houseId;
    private ObjectId taskId;
    private Task modifiedTask;
    private Calendar oldCalendar;

    public EditTaskCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
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
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.addTask_datePicked);
        TextView taskViewTitle = (TextView) view.findViewById(R.id.addTask_title);
        Button saveTask = (Button) view.findViewById(R.id.addTask_btnAdd);

        taskViewTitle.setText("Edit Task");
        saveTask.setText("SAVE");

        userId = myDatabase.getCurrentLocalUser(this.activity).getId();
        houseId = myDatabase.getCurrentLocalHouse(this.activity).getId();

        //taskId = new ObjectId("5e60ad4613d8ee3d69f5004c");
        //taskIdStr = "5e60ad4613d8ee3d69f5004c";

        if(taskIdStr.equals("")){
            Log.d(EditTaskFragment.TAG, "Task Id not received");
            Toast.makeText(activity, "Task Currently Not Editable", Toast.LENGTH_SHORT).show();
            activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
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
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(Calendar.HOUR_OF_DAY, 23);
            currentCalendar.set(Calendar.MINUTE, 59);
            currentCalendar.set(Calendar.SECOND, 59);
            currentCalendar.set(Calendar.MILLISECOND, 0);
            oldCalendar = Calendar.getInstance();
            oldCalendar.setTime(task.getSchedule().getStart());
            String oldDay = (String) DateFormat.format("dd", oldCalendar.getTime());
            String oldMonth = (String) DateFormat.format("MM", oldCalendar.getTime());
            String oldYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
            oldCalendar.set(Calendar.HOUR_OF_DAY, 23);
            oldCalendar.set(Calendar.MINUTE, 59);
            oldCalendar.set(Calendar.SECOND, 59);
            oldCalendar.set(Calendar.MILLISECOND, 0);

            Schedule schedule = task.getSchedule();
            if(schedule.getEndType().equals(Schedule.EndType.ON_DATE) || (currentCalendar.getTimeInMillis() < oldCalendar.getTimeInMillis())){
                onceButton.performClick();
                datePicker.updateDate(Integer.parseInt(oldYear), Integer.parseInt(oldMonth), Integer.parseInt(oldDay));
            }
            else{
                if(schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.YEARLY)){
                    yearlyButton.performClick();
                    while(oldCalendar.getTimeInMillis() < currentCalendar.getTimeInMillis()){
                        oldCalendar.add(Calendar.YEAR, 1);
                    }
                    String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                    String newMonth = (String) DateFormat.format("MM", oldCalendar.getTime());
                    String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                    datePicker.updateDate(Integer.parseInt(newYear), Integer.parseInt(newMonth), Integer.parseInt(newDay));
                }
                else if(schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.MONTHLY)){
                    monthlyButton.performClick();
                    while(oldCalendar.getTimeInMillis() < currentCalendar.getTimeInMillis()){
                        oldCalendar.add(Calendar.MONTH, 1);
                    }
                    String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                    String newMonth = (String) DateFormat.format("MM", oldCalendar.getTime());
                    String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                    datePicker.updateDate(Integer.parseInt(newYear), Integer.parseInt(newMonth), Integer.parseInt(newDay));
                }
                else if(schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.WEEKLY)){
                    weeklyButton.performClick();
                    while(oldCalendar.getTimeInMillis() < currentCalendar.getTimeInMillis()){
                        oldCalendar.add(Calendar.DAY_OF_YEAR, 7);
                    }
                    String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                    String newMonth = (String) DateFormat.format("MM", oldCalendar.getTime());
                    String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                    datePicker.updateDate(Integer.parseInt(newYear), Integer.parseInt(newMonth), Integer.parseInt(newDay));
                }
                else{
                    if(schedule.getRepeatSchedule().getDelay() == 1){
                        dailyButton.performClick();
                        while(oldCalendar.getTimeInMillis() < currentCalendar.getTimeInMillis()){
                            oldCalendar.add(Calendar.DAY_OF_YEAR, 1);
                        }
                        String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                        String newMonth = (String) DateFormat.format("MM", oldCalendar.getTime());
                        String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                        datePicker.updateDate(Integer.parseInt(newYear), Integer.parseInt(newMonth), Integer.parseInt(newDay));
                    }
                    else{
                        otherButton.performClick();
                        editNumberOfDays.setText(schedule.getRepeatSchedule().getDelay());
                        while(oldCalendar.getTimeInMillis() < currentCalendar.getTimeInMillis()){
                            oldCalendar.add(Calendar.DAY_OF_YEAR, schedule.getRepeatSchedule().getDelay());
                        }
                        String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                        String newMonth = (String) DateFormat.format("MM", oldCalendar.getTime());
                        String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                        datePicker.updateDate(Integer.parseInt(newYear), Integer.parseInt(newMonth), Integer.parseInt(newDay));
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
                String selectedFrequencyText = ((RadioButton) view.findViewById(taskFrequencies.getCheckedRadioButtonId())).getText().toString();
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                oldCalendar.set(year, month, day, 23, 59, 59);
                oldCalendar.set(Calendar.MILLISECOND, 0);
                Date date = oldCalendar.getTime();
                if(String.valueOf(taskName.getText()).equals("") || (selectedFrequencyText.equals("Other") && String.valueOf(editNumberOfDays.getText()).equals(""))){
                    Toast.makeText(activity, "Please fill out the whole form", Toast.LENGTH_SHORT).show();
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
                    schedule.setEndType(Schedule.EndType.ON_DATE);
                    Schedule.pauseStartEndBoundsChecking();
                    schedule.setStart(date);
                    schedule.setEnd(date);
                    Schedule.resumeStartEndBoundsChecking();
                }
                else{
                    schedule.setEndType(Schedule.EndType.AFTER_TIMES);
                    Schedule.pauseStartEndBoundsChecking();
                    schedule.setStart(date);
                    schedule.setEndPseudoIndefinite();
                    Schedule.resumeStartEndBoundsChecking();
                    if(selectedFrequencyText.equals("Other")){
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.DAILY);
                        schedule.getRepeatSchedule().setDelay(Integer.parseInt(String.valueOf(editNumberOfDays.getText())));
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
                }
                Task task = new Task(taskId, userId, houseId, name, schedule, selectedDifficultyNum);
                myDatabase.updateTask(task, bool->{
                    if(bool){
                        Log.d(AddTaskFragment.TAG, "Task updated in the Database");
                        Toast.makeText(activity, "Task Saved", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d(AddTaskFragment.TAG, "Task not updated in the  Database");
                    }
                });
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
        });
    }

    @Override
    public void acceptArguments(Object... args) {
        if(!(args[1] == null)){
            taskIdStr = args[1].toString();
            taskId = new ObjectId(taskIdStr);
        }
    }

    @Override
    public void updateInfo() {

    }
}
