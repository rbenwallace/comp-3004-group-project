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

public class EditFeeCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private String feeIdStr = "";
    private ObjectId userId;
    private ObjectId houseId;
    private ObjectId feeId;
    private Schedule schedule;

    public EditFeeCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void init(View view) {
        Log.d(AddTaskFragment.TAG, "Edit Task Clicked");
        Button editFeeBackButton = (Button) view.findViewById(R.id.addFee_btnBack);
        TextView feeName = (TextView) view.findViewById(R.id.addFee_editName);
        RadioGroup feeFrequencies = (RadioGroup) view.findViewById(R.id.addFee_radioFrequency);
        RadioButton onceButton = (RadioButton) view.findViewById(R.id.addFee_once);
        RadioButton dailyButton = (RadioButton) view.findViewById(R.id.addFee_daily);
        RadioButton weeklyButton = (RadioButton) view.findViewById(R.id.addFee_weekly);
        RadioButton monthlyButton = (RadioButton) view.findViewById(R.id.addFee_monthly);
        RadioButton yearlyButton = (RadioButton) view.findViewById(R.id.addFee_yearly);
        RadioButton otherButton = (RadioButton) view.findViewById(R.id.addFee_other);
        EditText editNumberOfDays = (EditText) view.findViewById(R.id.addFee_editNumberOfDays);
        TextView feeViewTitle = (TextView) view.findViewById(R.id.addFee_title);
        Button saveFee = (Button) view.findViewById(R.id.addFee_btnAdd);
        EditText feeAmount = (EditText) view.findViewById(R.id.addFee_editAmount);
        EditText feeTaxRate = (EditText) view.findViewById(R.id.addFee_editTaxRate);

        feeViewTitle.setText("Edit Task");
        saveFee.setText("SAVE");

        onceButton.setVisibility(View.INVISIBLE);

        userId = Session.getSession().getLoggedInUserId();
        houseId = Settings.OPEN_HOUSE.get();

        if(feeIdStr.equals("")){
            Log.d(EditFeeFragment.TAG, "Fee Id not received");
            Toast.makeText(activity, "Fee Currently Not Editable", Toast.LENGTH_SHORT).show();
            activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
        }
        else{
            Log.d(EditFeeFragment.TAG, "Fee: " + feeId + " received");
        }

        myDatabase.getFee(feeId, fee -> {
            feeName.setText(fee.getName());
            feeAmount.setText(String.valueOf(fee.getAmount()));
            schedule = fee.getSchedule();
            if(schedule.getEndType().equals(Schedule.EndType.ON_DATE)){
                Toast.makeText(activity, "Task not Editable", Toast.LENGTH_SHORT).show();
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
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
        editFeeBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
        });
        saveFee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                String selectedFrequencyText = ((RadioButton) view.findViewById(feeFrequencies.getCheckedRadioButtonId())).getText().toString();
                if(String.valueOf(feeName.getText()).equals("") || String.valueOf(feeAmount.getText()).equals("") || (selectedFrequencyText.equals("Other") && String.valueOf(editNumberOfDays.getText()).equals(""))){
                    Toast.makeText(activity, "Please fill out the whole form", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Float.parseFloat(feeAmount.getText().toString()) < 0.0){
                    Toast.makeText(activity, "Please only enter positive values", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = String.valueOf(feeName.getText());
                float amount = Float.parseFloat(String.valueOf(feeAmount.getText()));
                if(selectedFrequencyText.equals("Once")){
                    schedule.setEndType(Schedule.EndType.ON_DATE);
                    Schedule.pauseStartEndBoundsChecking();
                    schedule.setStart(Calendar.getInstance().getTime());
                    schedule.setEnd(Calendar.getInstance().getTime());
                    Schedule.resumeStartEndBoundsChecking();
                }
                else{
                    schedule.setEndType(Schedule.EndType.AFTER_TIMES);
                    Schedule.pauseStartEndBoundsChecking();
                    schedule.setStart(Calendar.getInstance().getTime());
                    schedule.setEndPseudoIndefinite();
                    Schedule.resumeStartEndBoundsChecking();
                    switch (selectedFrequencyText) {
                        case "Other":
                            schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.DAILY);
                            schedule.getRepeatSchedule().setDelay(Integer.parseInt(String.valueOf(editNumberOfDays.getText())));
                            break;
                        case "Yearly":
                            schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.YEARLY);
                            break;
                        case "Monthly":
                            schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
                            break;
                        case "Weekly":
                            schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
                            break;
                        case "Daily":
                            schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.DAILY);
                            break;
                    }
                }
                Fee fee = new Fee(userId, houseId, name, amount, schedule);
                myDatabase.updateFee(fee, bool->{
                    if(bool){
                        Log.d(AddTaskFragment.TAG, "Fee Saved to Database");
                        Toast.makeText(activity, "Fee Saved", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d(AddTaskFragment.TAG, "Fee not saved in Database");
                        Toast.makeText(activity, "Fee Not Saved", Toast.LENGTH_SHORT).show();
                    }
                });
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
        });
    }

    @Override
    public void acceptArguments(Object... args) {
        if(!(args[1] == null)){
            feeIdStr = args[1].toString();
            feeId = new ObjectId(feeIdStr);
        }
    }

    @Override
    public void updateInfo() {

    }
}
