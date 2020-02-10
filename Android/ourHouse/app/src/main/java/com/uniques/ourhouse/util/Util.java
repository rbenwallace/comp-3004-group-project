package com.uniques.ourhouse.util;

import android.app.DatePickerDialog;
import android.view.View;

import com.uniques.ourhouse.controller.FeedCard;
import com.uniques.ourhouse.fragment.FragmentActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Util {
    public static final int SCHEDULE_START = 0;
    public static final int SCHEDULE_END = 1;

    public static List<ScheduleHolder> populateScheduleHolderList(Filterable filterable) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(new Date());
        List<ScheduleHolder> modelList = new ArrayList<>();

        // add items occurring today or within next 3 days
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        HashMap<Nameable, Integer> allowedDupes = new HashMap<>();
        int globalAllowedDupes = 2;
        for (int i = 0; i <= 3; i++, globalAllowedDupes--) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            //noinspection unchecked
            for (Nameable n : filterable.filterRecursively(calendar.getTime())) {
                //noinspection ConstantConditions
                if (n instanceof ScheduleHolder &&
                        (!modelList.contains(n) || !allowedDupes.containsKey(n) || allowedDupes.get(n) > 0)) {

                    modelList.add((ScheduleHolder) n);
                    allowedDupes.put(n, globalAllowedDupes);
                }
            }
        }
        return modelList;
    }

    public static void initFeedCardList(List<FeedCard> cardList, List<Observable> modelList, FragmentActivity activity) {
        cardList.clear();
        for (Observable o : modelList) {
            FeedCard.CardType type = FeedCard.CardType.TEST;
            Hue hue = null;
            if (o instanceof HueHolder) hue = ((HueHolder) o).getHue();
            cardList.add(new FeedCard(type, o, hue, activity));
        }
    }

    public static <T extends Comparable> void sortList(List<T> list) {
        int pass = 0;
        int swaps = -1;
        while (swaps != 0) {
            swaps = 0;
            for (int i = 0; i < list.size() - pass - 1; i++) {
                if (list.get(i).compareTo(list.get(i + 1)) > 0) {
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
