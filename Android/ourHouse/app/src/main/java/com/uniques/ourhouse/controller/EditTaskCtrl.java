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
import com.uniques.ourhouse.fragment.EditTaskFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Schedule;

import org.bson.types.ObjectId;

import java.util.Calendar;
import java.util.Date;

public class EditTaskCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private String taskIdStr = "";
    private ObjectId userId;
    private ObjectId houseId;
    private ObjectId taskId;
    private Calendar oldCalendar;
    private Button editTaskBackButton;
    private TextView taskName;
    private RadioGroup taskFrequencies;
    private RadioButton onceButton;
    private RadioButton dailyButton;
    private RadioButton weeklyButton;
    private RadioButton monthlyButton;
    private RadioButton yearlyButton;
    private EditText editNumberOfDays;
    private RadioGroup taskDifficulty;
    private RadioButton easyButton;
    private RadioButton mediumButton;
    private RadioButton hardButton;
    private DatePicker datePicker;
    private TextView taskViewTitle;
    private Button saveTask;
    private Schedule.RepeatBasis sameSchedule;
    private Schedule oldSchedule;

    public EditTaskCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void init(View view) {
        Log.d(EditTaskFragment.TAG, "Edit Task Clicked");
        editTaskBackButton = view.findViewById(R.id.addTask_btnBack);
        taskName = view.findViewById(R.id.addTask_editDescription);
        taskFrequencies = view.findViewById(R.id.addTask_radioFrequency);
        onceButton = view.findViewById(R.id.addTask_once);
        dailyButton = view.findViewById(R.id.addTask_daily);
        weeklyButton = view.findViewById(R.id.addTask_weekly);
        monthlyButton = view.findViewById(R.id.addTask_monthly);
        yearlyButton = view.findViewById(R.id.addTask_yearly);
        editNumberOfDays = view.findViewById(R.id.addTask_editNumberOfDays);
        taskDifficulty = view.findViewById(R.id.addTask_radioDifficulty);
        easyButton = view.findViewById(R.id.addTask_easy);
        mediumButton = view.findViewById(R.id.addTask_medium);
        hardButton = view.findViewById(R.id.addTask_hard);
        datePicker = view.findViewById(R.id.addTask_datePicked);
        taskViewTitle = view.findViewById(R.id.addTask_title);
        saveTask = view.findViewById(R.id.addTask_btnAdd);

        taskViewTitle.setText("Edit Task");
        saveTask.setText("SAVE");

        userId = Session.getSession().getLoggedInUserId();
        houseId = Settings.OPEN_HOUSE.get();

        if (taskIdStr.equals("")) {
            Log.d(EditTaskFragment.TAG, "Task Id not received");
            Toast.makeText(activity, "Task Currently Not Editable", Toast.LENGTH_SHORT).show();
            activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
        } else {
            Log.d(EditTaskFragment.TAG, "Task: " + taskId + " received");
        }

        myDatabase.getTask(taskId, task -> {
            Log.d(EditTaskFragment.TAG, "Trying to get Task from database");
            taskName.setText(task.getName());
            int difficulty = task.getDifficulty();
            if (difficulty == 1) {
                easyButton.performClick();
            } else if (difficulty == 2) {
                mediumButton.performClick();
            } else {
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
            String oldMonth = String.valueOf(oldCalendar.getTime().getMonth());
            String oldYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
            System.out.println("wallace date: " + oldDay + " " + oldMonth + " " + oldYear);
            Schedule schedule = task.getSchedule();
            oldSchedule = task.getSchedule();
            if (schedule.getEndType().equals(Schedule.EndType.ON_DATE)) {
                onceButton.performClick();
                datePicker.updateDate(Integer.parseInt(oldYear), Integer.parseInt(oldMonth), Integer.parseInt(oldDay));
            } else {
                sameSchedule = schedule.getRepeatSchedule().getRepeatBasis();
                editNumberOfDays.setText(String.valueOf(schedule.getRepeatSchedule().getDelay()));
                if (schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.YEARLY)) {
                    yearlyButton.performClick();
                    while (oldCalendar.getTime().before(currentCalendar.getTime())) {
                        oldCalendar.add(Calendar.YEAR, schedule.getRepeatSchedule().getDelay());
                    }
                    String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                    String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                    datePicker.updateDate(Integer.parseInt(newYear), oldCalendar.getTime().getMonth(), Integer.parseInt(newDay));
                } else if (schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.MONTHLY)) {
                    monthlyButton.performClick();
                    while (oldCalendar.getTime().before(currentCalendar.getTime())) {
                        oldCalendar.add(Calendar.MONTH, schedule.getRepeatSchedule().getDelay());
                    }
                    String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                    String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                    datePicker.updateDate(Integer.parseInt(newYear), oldCalendar.getTime().getMonth(), Integer.parseInt(newDay));
                } else if (schedule.getRepeatSchedule().getRepeatBasis().toString().equals(Schedule.RepeatBasis.WEEKLY.toString())) {
                    weeklyButton.performClick();
                    while (oldCalendar.getTime().before(currentCalendar.getTime())) {
                        oldCalendar.add(Calendar.DAY_OF_YEAR, 7);
                    }
                    String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                    String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                    datePicker.updateDate(Integer.parseInt(newYear), oldCalendar.getTime().getMonth(), Integer.parseInt(newDay));
                } else {
                    dailyButton.performClick();
                    while (oldCalendar.getTime().before(currentCalendar.getTime())) {
                        oldCalendar.add(Calendar.DAY_OF_YEAR, schedule.getRepeatSchedule().getDelay());
                    }
                    String newDay = (String) DateFormat.format("dd", oldCalendar.getTime());
                    String newYear = (String) DateFormat.format("yyyy", oldCalendar.getTime());
                    datePicker.updateDate(Integer.parseInt(newYear), oldCalendar.getTime().getMonth(), Integer.parseInt(newDay));
                }
            }

        });
        editTaskBackButton.setOnClickListener(view12 -> {
            //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
            activity.popFragment(FragmentId.GET(EditTaskFragment.TAG));
        });
        saveTask.setOnClickListener(view1 -> {
            //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
            String selectedFrequencyText = ((RadioButton) view.findViewById(taskFrequencies.getCheckedRadioButtonId())).getText().toString();
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year = datePicker.getYear();
            oldCalendar.set(year, month, day, 23, 59, 59);
            oldCalendar.set(Calendar.MILLISECOND, 0);
            Calendar currentDate = Calendar.getInstance();
            currentDate.set(Calendar.HOUR_OF_DAY, 23);
            currentDate.set(Calendar.MINUTE, 59);
            currentDate.set(Calendar.SECOND, 59);
            currentDate.set(Calendar.MILLISECOND, 0);
            Date date = oldCalendar.getTime();
            if (String.valueOf(taskName.getText()).equals("")) {
                Toast.makeText(activity, "Please fill out the whole form", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!date.after(currentDate.getTime())) {
                Toast.makeText(activity, "Please choose a date later than today", Toast.LENGTH_LONG).show();
                return;
            }
            if(!String.valueOf(editNumberOfDays.getText()).equals("")){
                try{
                    int checkNum = Integer.parseInt(String.valueOf(editNumberOfDays.getText()));
                    if(checkNum < 0){
                        Toast.makeText(activity, "For frequency number please enter a number greater than 1", Toast.LENGTH_LONG).show();
                        return;
                    }
                }catch (NumberFormatException ex) {
                    Toast.makeText(activity, "Please enter a whole number for frequency number", Toast.LENGTH_LONG).show();
                }
            }
            String name = String.valueOf(taskName.getText());
            String selectedDifficulty = ((RadioButton) view.findViewById(taskDifficulty.getCheckedRadioButtonId())).getText().toString();
            int selectedDifficultyNum = 1;
            if (selectedDifficulty.equals("Medium")) {
                selectedDifficultyNum = 2;
            } else if (selectedDifficulty.equals("Hard")) {
                selectedDifficultyNum = 3;
            }
            Schedule schedule = new Schedule();
            if (selectedFrequencyText.equals("Once")) {
                schedule.pauseStartEndBoundsChecking();
                schedule.setStart(date);
                schedule.setEnd(date);
                schedule.resumeStartEndBoundsChecking();
                schedule.setEndType(Schedule.EndType.ON_DATE);
            } else {
                schedule.pauseStartEndBoundsChecking();
                schedule.setStart(date);
                schedule.setEndPseudoIndefinite();
                schedule.resumeStartEndBoundsChecking();
                if(!String.valueOf(editNumberOfDays.getText()).equals("")){
                    schedule.getRepeatSchedule().setDelay(Integer.parseInt(String.valueOf(editNumberOfDays.getText())));
                }
                switch (selectedFrequencyText) {
                    case "Daily":
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.DAILY);
                        break;
                    case "Yearly":
                        if(day > 28 && month == 1){
                            datePicker.updateDate(year, month, 28);
                            Toast.makeText(activity, "Recurrence date set to Feb 28th, press add to continue", Toast.LENGTH_LONG).show();
                            return;
                        }
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.YEARLY);
                        break;
                    case "Monthly":
                        if(day > 28){
                            datePicker.updateDate(year, month, 28);
                            Toast.makeText(activity, "Recurrence date set to 28th. Press add to continue", Toast.LENGTH_LONG).show();
                            return;
                        }
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
                        break;
                    case "Weekly":
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
                        break;
                }
                schedule.setEndType(Schedule.EndType.AFTER_TIMES);
            }
            Task task;
            if(sameSchedule == schedule.getRepeatSchedule().getRepeatBasis()){
                task = new Task(taskId, userId, houseId, name, oldSchedule, selectedDifficultyNum);
            }
            else{
                task = new Task(taskId, userId, houseId, name, schedule, selectedDifficultyNum);
            }
            myDatabase.updateTask(task, bool -> {
                if (bool) {
                    Log.d(EditTaskFragment.TAG, "Task updated in the Database");
                    Toast.makeText(activity, "Task Saved", Toast.LENGTH_SHORT).show();
                    Settings.EVENT_SERVICE_DUTIES_ARE_PRISTINE.set(false);
                    activity.popFragment(FragmentId.GET(EditTaskFragment.TAG));
                } else {
                    Log.d(EditTaskFragment.TAG, "Task not updated in the  Database");
                    Toast.makeText(activity, "Task Not Saved", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void acceptArguments(Object... args) {
        if (args[0] != null) {
            taskIdStr = args[0].toString();
            taskId = new ObjectId(taskIdStr);
            Log.d("CheckingTask", taskId.toString());
        }
    }

    @Override
    public void updateInfo() {

    }
}
