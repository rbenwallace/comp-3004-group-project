package com.uniques.ourhouse.util;

import android.app.DatePickerDialog;
import android.view.View;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Util {
    public static final int SCHEDULE_START = 0;
    public static final int SCHEDULE_END = 1;

    public static <T extends Comparable> void sortList(List<T> list, boolean reverse) {
        int pass = 0;
        int swaps = -1;
        while (swaps != 0) {
            swaps = 0;
            for (int i = 0; i < list.size() - pass - 1; ++i) {
                int order = list.get(i).compareTo(list.get(i + 1));
                if (reverse ? order < 0 : order > 0) {
                    T temp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, temp);
                    swaps++;
                }
            }
            pass++;
        }
    }

    public static String formatDate(Schedule schedule) {
        return formatDate(schedule.getStart());
    }

    public static String formatDate(Date date) {
        return DateFormat.getDateInstance().format(date);
    }

    public static DatePickerDialog newDatePicker(View view, Calendar calendar, Schedule schedule, int field, boolean setBoth) {
        return new DatePickerDialog(
                view.getContext(),
                (view1, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    Date newDate = calendar.getTime();
                    if (field == SCHEDULE_START) {
                        if (newDate.after(schedule.getEnd()))
                            schedule.setEnd(newDate);
                        schedule.setStart(newDate);
                    } else if (field == SCHEDULE_END) {
                        if (newDate.before(schedule.getStart()))
                            schedule.setStart(newDate);
                        schedule.setEnd(newDate);
                    }
                    if (setBoth) {
                        if (field == SCHEDULE_START)
                            schedule.setEnd(newDate);
                        else if (field == SCHEDULE_END)
                            schedule.setStart(newDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
}
