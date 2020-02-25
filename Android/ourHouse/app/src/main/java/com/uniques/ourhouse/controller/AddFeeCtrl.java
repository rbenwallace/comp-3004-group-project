package com.uniques.ourhouse.controller;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uniques.ourhouse.R;
import com.uniques.ourhouse.fragment.AddFeeFragment;
import com.uniques.ourhouse.fragment.FeedFragment;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.fragment.FragmentId;
import com.uniques.ourhouse.fragment.ManageFragment;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.util.Schedule;

import java.util.Calendar;

public class AddFeeCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public AddFeeCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(AddFeeFragment.TAG, "Add Fee Clicked");

        Button addFeeBackButton = view.findViewById(R.id.addFee_btnBack);
        Button addFeeAddButton = view.findViewById(R.id.addFee_btnAdd);
        Button addTaxButton = view.findViewById(R.id.addFee_addTax);
        TextView taxRate = view.findViewById(R.id.addFee_editTaxRate);
        TextView feeName = view.findViewById(R.id.addFee_editName);
        TextView feeAmount = view.findViewById(R.id.addFee_editAmount);
        RadioGroup feeFrequencies = view.findViewById(R.id.addFee_radioFrequency);
        TextView otherFeeFrequency = view.findViewById(R.id.addFee_editNumberOfDays);

        addTaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(feeAmount.getText().toString().equals("")){
                    Toast.makeText(activity, "Enter a Fee Amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Float.valueOf(feeAmount.getText().toString()) < 0.0 || Float.valueOf(taxRate.getText().toString()) < 0.0){
                    Toast.makeText(activity, "Enter a positive percentage", Toast.LENGTH_SHORT).show();
                    return;
                }
                double originalAmount = Double.valueOf(feeAmount.getText().toString());
                double inputtedTax = Float.valueOf(taxRate.getText().toString())/100;
                double newAmount = Math.round(originalAmount + (originalAmount * inputtedTax));
                feeAmount.setText(String.valueOf(newAmount));
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
                if(Float.valueOf(feeAmount.getText().toString()) < 0.0){
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
                    if(selectedFrequencyText.equals("Other")){
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.DAILY);
                        schedule.getRepeatSchedule().setDelay(Integer.valueOf(String.valueOf(otherFeeFrequency.getText())));
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
                Fee fee = new Fee(name, amount, schedule);
                Log.d(AddFeeFragment.TAG, fee.consoleFormat("FEE ADDED: "));
                Toast.makeText(activity, "Fee Added", Toast.LENGTH_SHORT).show();
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
        });
    }

    @Override
    public void updateInfo() {

    }
}
