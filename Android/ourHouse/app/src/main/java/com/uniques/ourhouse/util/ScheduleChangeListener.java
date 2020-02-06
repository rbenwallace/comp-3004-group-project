package com.uniques.ourhouse.util;


import com.uniques.ourhouse.controller.ActivityCtrl;

import java.util.Date;


public interface ScheduleChangeListener extends Schedule.ChangeListener {

    ActivityCtrl getCtrl();

    @Override
    default void onChange() {
        getCtrl().updateInfo();
    }

    @Override
    default void onStartChange(Date newStart) {
    }

    @Override
    default void onEndChange(Date newEnd) {
    }

    @Override
    default void onEndTypeChange(Schedule.EndType endType, int endAfterTimes) {
    }

    @Override
    default void onRepeatToggled(boolean enabled) {
    }

    @Override
    default void onRepeatTypeChange(Schedule.RepeatType newType) {
    }

    @Override
    default void onRepeatBasisChange(Schedule.RepeatBasis newBasis) {
    }

    @Override
    default void onRepeatIterativeDelayChange(int newDelay) {
    }

    @Override
    default void onRepeatRelativeSelectionChange(int[] selections) {
    }
}
