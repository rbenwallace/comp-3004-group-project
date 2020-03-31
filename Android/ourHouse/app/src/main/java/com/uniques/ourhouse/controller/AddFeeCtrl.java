package com.uniques.ourhouse.controller;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddFeeFragment;
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.session.Settings;
import com.uniques.ourhouse.util.Schedule;

import org.bson.types.ObjectId;

import java.util.Calendar;
import java.util.Date;

public class AddFeeCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private boolean isTaxable = true;
    private ObjectId userId;
    private ObjectId houseId;
    private Button addFeeBackButton;
    private Button addFeeAddButton;
    private Button addTaxButton;
    private EditText taxRate;
    private TextView feeName;
    private EditText feeAmount;
    private RadioGroup feeFrequencies;
    private TextView otherFeeFrequency;
    private boolean useNewDay = false;
    private Date newDate;
    private boolean useNewDayMonth = false;
    private Date newDateMonth;

    public AddFeeCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        addFeeBackButton = view.findViewById(R.id.addFee_btnBack);
        addFeeAddButton = view.findViewById(R.id.addFee_btnAdd);
        addTaxButton = view.findViewById(R.id.addFee_addTax);
        taxRate = view.findViewById(R.id.addFee_editTaxRate);
        feeName = view.findViewById(R.id.addFee_editName);
        feeAmount = view.findViewById(R.id.addFee_editAmount);
        feeFrequencies = view.findViewById(R.id.addFee_radioFrequency);
        otherFeeFrequency = view.findViewById(R.id.addFee_editNumberOfDays);

        userId = Session.getSession().getLoggedInUserId();
        houseId = Settings.OPEN_HOUSE.get();

        taxRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isTaxable = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        feeAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isTaxable = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        taxRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isTaxable = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        addTaxButton.setOnClickListener(view13 -> {
            if(feeAmount.getText().toString().equals("")){
                Toast.makeText(activity, "Enter a Fee Amount", Toast.LENGTH_SHORT).show();
                return;
            }
            if(Float.parseFloat(feeAmount.getText().toString()) < 0.0 || Float.parseFloat(taxRate.getText().toString()) < 0.0){
                Toast.makeText(activity, "Enter a positive percentage and amount", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isTaxable){ return; }
            double originalAmount = Double.parseDouble(feeAmount.getText().toString());
            double inputtedTax = Float.parseFloat(taxRate.getText().toString())/100.0;
            double newAmount = Math.round((originalAmount + (originalAmount * inputtedTax))*100.0)/100.0;
            feeAmount.setText(String.valueOf(newAmount));
            isTaxable = false;
        });

        addFeeBackButton.setOnClickListener(view12 -> activity.popFragment(FragmentId.GET(AddFeeFragment.TAG)));

        addFeeAddButton.setOnClickListener(view1 -> {
            String selectedFrequencyText = ((RadioButton) view.findViewById(feeFrequencies.getCheckedRadioButtonId())).getText().toString();
            if(String.valueOf(feeName.getText()).equals("") || String.valueOf(feeAmount.getText()).equals("") || (selectedFrequencyText.equals("Other") && String.valueOf(otherFeeFrequency.getText()).equals(""))){
                Toast.makeText(activity, "Please fill out the whole form", Toast.LENGTH_SHORT).show();
                return;
            }
            if(Float.parseFloat(feeAmount.getText().toString()) < 0.0){
                Toast.makeText(activity, "Please only enter positive values", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = String.valueOf(feeName.getText());
            float amount = Float.parseFloat(String.valueOf(feeAmount.getText()));
            Schedule schedule = new Schedule();
            if(selectedFrequencyText.equals("Once")){
                schedule.setEndType(Schedule.EndType.ON_DATE);
                schedule.pauseStartEndBoundsChecking();
                schedule.setStart(Calendar.getInstance().getTime());
                schedule.setEnd(Calendar.getInstance().getTime());
                schedule.resumeStartEndBoundsChecking();
            }
            else{
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 0);
                Date date = calendar.getTime();
                schedule.setEndType(Schedule.EndType.AFTER_TIMES);
                schedule.pauseStartEndBoundsChecking();
                schedule.setStart(date);
                schedule.setEndPseudoIndefinite();
                schedule.resumeStartEndBoundsChecking();
                if(!String.valueOf(otherFeeFrequency.getText()).equals("")){
                    schedule.getRepeatSchedule().setDelay(Integer.parseInt(String.valueOf(otherFeeFrequency.getText())));
                }
                switch (selectedFrequencyText) {
                    case "Other":
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
                            Toast.makeText(activity, "Yearly recurrence day is set to Feb 28th. Press add to continue", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(activity, "Monthly recurrence date has been set to the 28th. Press add to continue", Toast.LENGTH_LONG).show();
                            return;
                        }
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
                        break;
                    case "Weekly":
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
                        break;
                }
            }
            Fee fee = new Fee(userId, houseId, name, amount, schedule);
            myDatabase.postFee(fee, bool->{
                if(bool){
                    Log.d(AddTaskFragment.TAG, "Fee Added to Database");
                    Toast.makeText(activity, "Fee Added", Toast.LENGTH_SHORT).show();
                    Settings.EVENT_SERVICE_DUTIES_ARE_PRISTINE.set(false);
                    activity.popFragment(FragmentId.GET(ManageFragment.TAG));
                }
                else{
                    Log.d(AddTaskFragment.TAG, "Fee not received by Database");
                    Toast.makeText(activity, "Fee Not Added", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void acceptArguments(Object... args) {

    }

    @Override
    public void updateInfo() {

    }
}
