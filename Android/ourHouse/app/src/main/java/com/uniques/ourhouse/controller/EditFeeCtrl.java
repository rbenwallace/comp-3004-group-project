package com.uniques.ourhouse.controller;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.EditFeeFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Schedule;

import org.bson.types.ObjectId;

import java.util.Calendar;
import java.util.Date;

public class EditFeeCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private String feeIdStr = "";
    private ObjectId userId;
    private ObjectId houseId;
    private ObjectId feeId;
    private Schedule schedule;
    private Button editFeeBackButton;
    private Button deleteFeeButton;
    private TextView feeName;
    private RadioGroup feeFrequencies;
    private RadioButton onceButton;
    private RadioButton dailyButton;
    private RadioButton weeklyButton;
    private RadioButton monthlyButton;
    private RadioButton yearlyButton;
    private EditText editNumberOfDays;
    private TextView feeViewTitle;
    private Button saveFee;
    private ObjectId currentFee;
    private EditText feeAmount;
    private EditText feeTaxRate;
    private boolean useNewDay = false;
    private Date newDate;
    private boolean useNewDayMonth = false;
    private Date newDateMonth;
    private Schedule.RepeatBasis sameSchedule;
    private Schedule oldSchedule;

    public EditFeeCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void init(View view) {
        Log.d(AddTaskFragment.TAG, "Edit Task Clicked");
        editFeeBackButton = view.findViewById(R.id.addFee_btnBack);
        deleteFeeButton = view.findViewById(R.id.addFee_btnDelete);
        feeName = view.findViewById(R.id.addFee_editName);
        feeFrequencies = view.findViewById(R.id.addFee_radioFrequency);
        onceButton = view.findViewById(R.id.addFee_once);
        dailyButton = view.findViewById(R.id.addFee_daily);
        weeklyButton = view.findViewById(R.id.addFee_weekly);
        monthlyButton = view.findViewById(R.id.addFee_monthly);
        yearlyButton = view.findViewById(R.id.addFee_yearly);
        editNumberOfDays = view.findViewById(R.id.addFee_editNumberOfDays);
        feeViewTitle = view.findViewById(R.id.addFee_title);
        saveFee = view.findViewById(R.id.addFee_btnAdd);
        feeAmount = view.findViewById(R.id.addFee_editAmount);
        feeTaxRate = view.findViewById(R.id.addFee_editTaxRate);

        feeViewTitle.setText("Edit Task");
        saveFee.setText("SAVE");

        //onceButton.setVisibility(View.INVISIBLE);

        userId = Session.getSession().getLoggedInUserId();
        houseId = Settings.OPEN_HOUSE.get();

        if (feeIdStr.equals("")) {
            Log.d(EditFeeFragment.TAG, "Fee Id not received");
            Toast.makeText(activity, "Fee Currently Not Editable", Toast.LENGTH_SHORT).show();
            activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
        } else {
            Log.d(EditFeeFragment.TAG, "Fee: " + feeId + " received");
        }

        myDatabase.getFee(feeId, fee -> {
            Log.d(EditFeeFragment.TAG, "Trying to get Fee from database");
            if(fee == null){
                Log.d(EditFeeFragment.TAG, "Fee not received from the Database");
                Toast.makeText(activity, "Fee not found", Toast.LENGTH_SHORT).show();
                activity.popFragment(FragmentId.GET(EditFeeFragment.TAG));
                return;
            }
            currentFee = fee.getId();
            feeName.setText(fee.getName());
            feeAmount.setText(String.valueOf(fee.getAmount()));
            schedule = fee.getSchedule();
            oldSchedule = fee.getSchedule();
            if (schedule.getEndType().equals(Schedule.EndType.ON_DATE)) {
                onceButton.performClick();
            } else {
                editNumberOfDays.setText(String.valueOf(schedule.getRepeatSchedule().getDelay()));
                sameSchedule = fee.getSchedule().getRepeatSchedule().getRepeatBasis();
                if (schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.YEARLY)) {
                    yearlyButton.performClick();
                } else if (schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.MONTHLY)) {
                    monthlyButton.performClick();
                } else if (schedule.getRepeatSchedule().getRepeatBasis().equals(Schedule.RepeatBasis.WEEKLY)) {
                    weeklyButton.performClick();
                } else {
                    dailyButton.performClick();
                }
            }

        });
        editFeeBackButton.setOnClickListener(view12 -> {
            activity.popFragment(FragmentId.GET(EditFeeFragment.TAG));
        });
        deleteFeeButton.setOnClickListener(view123 -> {
            myDatabase.getFee(currentFee, deleteFee -> {
                deleteFee.setDeletedDate(Calendar.getInstance().getTime());
                myDatabase.updateFee(deleteFee, deleteBool -> {
                    if (deleteBool) {
                        Log.d(EditFeeFragment.TAG, "Fee delete date set in the Database");
                        Toast.makeText(activity, "Fee Deleted", Toast.LENGTH_SHORT).show();
                        Settings.EVENT_SERVICE_DUTIES_ARE_PRISTINE.set(false);
                        activity.popFragment(FragmentId.GET(EditFeeFragment.TAG));
                    } else {
                        Log.d(EditFeeFragment.TAG, "Fee delete date not set in the  Database");
                        Toast.makeText(activity, "Fee Not Deleted", Toast.LENGTH_SHORT).show();
                        activity.popFragment(FragmentId.GET(EditFeeFragment.TAG));
                    }
                });
            });

        });
        saveFee.setOnClickListener(view1 -> {
            String selectedFrequencyText = ((RadioButton) view.findViewById(feeFrequencies.getCheckedRadioButtonId())).getText().toString();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 0);
            Date date = calendar.getTime();
            if(String.valueOf(feeName.getText()).equals("") || String.valueOf(feeAmount.getText()).equals("")){
                Toast.makeText(activity, "Please fill out the whole form", Toast.LENGTH_SHORT).show();
                return;
            }
            if(Float.parseFloat(feeAmount.getText().toString()) < 0.0){
                Toast.makeText(activity, "Please only enter positive values", Toast.LENGTH_SHORT).show();
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
            String name = String.valueOf(feeName.getText());
            float amount = Float.parseFloat(String.valueOf(feeAmount.getText()));
            Schedule schedule = new Schedule();
            if(selectedFrequencyText.equals("Once")){
                schedule.pauseStartEndBoundsChecking();
                schedule.setStart(date);
                schedule.setEnd(date);
                schedule.resumeStartEndBoundsChecking();
                schedule.setEndType(Schedule.EndType.ON_DATE);
            }
            else{
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
                        if(useNewDay){
                            schedule.pauseStartEndBoundsChecking();
                            schedule.setStart(newDate);
                            schedule.resumeStartEndBoundsChecking();
                            schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.YEARLY);
                            break;
                        }
                        if(date.getDate() > 28 && date.getMonth() == 1){
                            newDate = date;
                            newDate.setDate(28);
                            useNewDay = true;
                            Toast.makeText(activity, "Recurrence date set to Feb 28th. Press add to continue", Toast.LENGTH_LONG).show();
                            return;
                        }
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.YEARLY);
                        break;
                    case "Monthly":
                        if(useNewDayMonth){
                            schedule.pauseStartEndBoundsChecking();
                            schedule.setStart(newDateMonth);
                            schedule.resumeStartEndBoundsChecking();
                            schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
                            break;
                        }
                        if(date.getDate() > 28){
                            newDateMonth = date;
                            newDateMonth.setDate(28);
                            useNewDayMonth = true;
                            Toast.makeText(activity, "Recurrence date set to the 28th. Press add to continue", Toast.LENGTH_LONG).show();
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
            Fee fee;
            if(sameSchedule == schedule.getRepeatSchedule().getRepeatBasis() && oldSchedule.getRepeatSchedule().getDelay() == schedule.getRepeatSchedule().getDelay()){
                fee = new Fee(userId, houseId, name, amount, oldSchedule);
            }
            else{
                fee = new Fee(userId, houseId, name, amount, schedule);
            }
            myDatabase.updateFee(fee, bool -> {
                if (bool) {
                    Log.d(AddTaskFragment.TAG, "Fee Saved to Database");
                    Toast.makeText(activity, "Fee Saved", Toast.LENGTH_SHORT).show();
                    Settings.EVENT_SERVICE_DUTIES_ARE_PRISTINE.set(false);
                    activity.popFragment(FragmentId.GET(EditFeeFragment.TAG));
                } else {
                    Log.d(AddTaskFragment.TAG, "Fee not saved in Database");
                    Toast.makeText(activity, "Fee Not Saved", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void acceptArguments(Object... args) {
        if (!(args[1] == null)) {
            feeIdStr = args[1].toString();
            feeId = new ObjectId(feeIdStr);
        }
    }

    @Override
    public void updateInfo() {

    }
}
