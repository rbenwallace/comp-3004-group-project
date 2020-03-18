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
import com.uniques.ourhouse.fragment.AddTaskFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
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

public class AddFeeCtrl implements FragmentCtrl {
    private FragmentActivity activity;
    private DatabaseLink myDatabase = Session.getSession().getDatabase();
    private boolean isTaxable = true;
    private ObjectId userId;
    private ObjectId houseId;

    public AddFeeCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Button addFeeBackButton = view.findViewById(R.id.addFee_btnBack);
        Button addFeeAddButton = view.findViewById(R.id.addFee_btnAdd);
        Button addTaxButton = view.findViewById(R.id.addFee_addTax);
        EditText taxRate = view.findViewById(R.id.addFee_editTaxRate);
        TextView feeName = view.findViewById(R.id.addFee_editName);
        EditText feeAmount = view.findViewById(R.id.addFee_editAmount);
        RadioGroup feeFrequencies = view.findViewById(R.id.addFee_radioFrequency);
        TextView otherFeeFrequency = view.findViewById(R.id.addFee_editNumberOfDays);

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

        addTaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                double inputtedTax = Float.parseFloat(taxRate.getText().toString())/100;
                double newAmount = Math.round(originalAmount + (originalAmount * inputtedTax));
                feeAmount.setText(String.valueOf(newAmount));
                isTaxable = false;
            }
        });

        addFeeBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.pushFragment(FragmentId.GET(ManageFragment.TAG));
            }
        });
        addFeeAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
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
                            schedule.getRepeatSchedule().setDelay(Integer.parseInt(String.valueOf(otherFeeFrequency.getText())));
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
                myDatabase.postFee(fee, bool->{
                    if(bool){
                        Log.d(AddTaskFragment.TAG, "Fee Added to Database");
                        Toast.makeText(activity, "Fee Added", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d(AddTaskFragment.TAG, "Fee not received by Database");
                        Toast.makeText(activity, "Fee Not Added", Toast.LENGTH_SHORT).show();
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
