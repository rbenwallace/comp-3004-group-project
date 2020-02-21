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
import com.uniques.ourhouse.util.BetterSchedule;

public class AddFeeCtrl implements FragmentCtrl {
    private FragmentActivity activity;

    public AddFeeCtrl(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void init(View view) {
        Log.d(AddFeeFragment.TAG, "Add Fee Clicked");
        Button addFeeBackButton = (Button) view.findViewById(R.id.addFee_btnBack);
        Button addFeeAddButton = (Button) view.findViewById(R.id.addFee_btnAdd);
        TextView feeName = (TextView) view.findViewById(R.id.addFee_editName);
        TextView feeAmount = (TextView) view.findViewById(R.id.addFee_editAmount);
        RadioGroup feeFrequencies = (RadioGroup) view.findViewById(R.id.addFee_radioFrequency);
        TextView otherFeeFrequency = (TextView) view.findViewById(R.id.addFee_editNumberOfDays);

        addFeeBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                activity.pushFragment(FragmentId.GET(ManageFragment.TAG));
            }
        });
        addFeeAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                //TODO NAVIGATE TO NEXT FRAGMENT
//                ((LS_Main) activity).setViewPager(4);
                String selectedFrequencyText = ((RadioButton) view.findViewById(feeFrequencies.getCheckedRadioButtonId())).getText().toString();
                if(String.valueOf(feeName.getText()).equals("") || String.valueOf(feeAmount.getText()).equals("") || (selectedFrequencyText.equals("Other") && String.valueOf(otherFeeFrequency.getText()).equals(""))){
                    Toast.makeText(activity, "Please fill out the whole form", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = String.valueOf(feeName.getText());
                float amount = Float.parseFloat(String.valueOf(feeAmount.getText()));
                BetterSchedule schedule = new BetterSchedule();
                //schedule.setStart(Calendar.getInstance().getTime());
                if(!selectedFrequencyText.equals("Once")){
                    schedule.initRepeatBetterSchedule();
                }
                if(selectedFrequencyText.equals("Other")){
                    //TODO Implement when user wants custom frequency
                    //Integer.valueOf(String.valueOf(feeAmount.getText()));
                    schedule.getRepeatBetterSchedule().setRepeatBasis(BetterSchedule.RepeatBasis.DAILY);
                }
                else if(selectedFrequencyText.equals("Yearly")){
                    schedule.getRepeatBetterSchedule().setRepeatBasis(BetterSchedule.RepeatBasis.YEARLY);
                }
                else if(selectedFrequencyText.equals("Monthly")){
                    schedule.getRepeatBetterSchedule().setRepeatBasis(BetterSchedule.RepeatBasis.MONTHLY);
                }
                else if(selectedFrequencyText.equals("Weekly")){
                    schedule.getRepeatBetterSchedule().setRepeatBasis(BetterSchedule.RepeatBasis.WEEKLY);
                }
                else if(selectedFrequencyText.equals("Daily")){
                    schedule.getRepeatBetterSchedule().setRepeatBasis(BetterSchedule.RepeatBasis.DAILY);
                }
                Fee fee = new Fee(name, amount, schedule);
                System.out.println(fee.consoleFormat("Wallace"));
                Toast.makeText(activity, "Fee Added", Toast.LENGTH_SHORT).show();
                activity.pushFragment(FragmentId.GET(FeedFragment.TAG));
            }
        });
    }

    @Override
    public void updateInfo() {

    }
}
