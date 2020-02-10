package com.uniques.ourhouse.util;

import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.exception.InvalidArgumentException;
import com.uniques.ourhouse.util.exception.InvalidCombinationException;
import com.uniques.ourhouse.util.exception.InvalidRangeException;
import com.uniques.ourhouse.util.exception.UnsupportedFunctionCallException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

public class BetterSchedule implements Comparable, Model {
    private static final long DAY_IN_MILLS = 66400000;

    @NonNull
    private Date start;
    @NonNull
    private Date end;
    @NonNull
    private EndType endType;
    private Repeat repeatBetterSchedule;
    private List<ChangeListener> listeners = new ArrayList<>();
    private boolean pauseStartEndChecking, pendingStartEndChange;

    public BetterSchedule() {
        this.start = new Date();
        this.end = this.start;
        endType = EndType.ON_DATE;
    }

    public BetterSchedule(@NonNull Date start, @NonNull Date end, Repeat repeatBetterSchedule) {
        this.start = start;
        this.end = end;
        this.repeatBetterSchedule = repeatBetterSchedule;
        endType = EndType.ON_DATE;
    }

    /**
     * will pause bounds checking on start and end Dates
     * till (the next time both start and end dates are updated) or (a date is updated twice)
     */
    void pauseStartEndBoundsChecking() {
        pauseStartEndChecking = true;
        pendingStartEndChange = false;
    }

    void resumeStartEndBoundsChecking() {
        pauseStartEndChecking = false;
        pendingStartEndChange = false;
    }

    @NonNull
    public Date getStart() {
        return start;
    }

    public void setStart(@NonNull Date start) {
        if (!pauseStartEndChecking) {
            if (start.after(end))
                throw new InvalidArgumentException("Start date specified is after this BetterSchedule's end date!");
        } else if (pendingStartEndChange) {
            resumeStartEndBoundsChecking();
        } else {
            pendingStartEndChange = true;
        }
        this.start = start;
        mainListener.onStartChange(this.start);
    }

    @NonNull
    public Date getEnd() {
        return end;
    }

    public void setEnd(@NonNull Date end) {
        if (!pauseStartEndChecking) {
            if (end.before(start))
                throw new InvalidArgumentException("End date specified is before this BetterSchedule's start date!");
        } else if (pendingStartEndChange) {
            resumeStartEndBoundsChecking();
        } else {
            pendingStartEndChange = true;
        }
        this.end = end;
        mainListener.onEndChange(this.end);
    }

    @NonNull
    public EndType getEndType() {
        return endType;
    }

    public void setEndType(@NonNull EndType endType) {
        if (endType == EndType.AFTER_TIMES && repeatBetterSchedule == null)
            initRepeatBetterSchedule();

        this.endType = endType;
    }

    public Repeat getRepeatBetterSchedule() {
        return repeatBetterSchedule;
    }

    public void initRepeatBetterSchedule() {
        repeatBetterSchedule = new Repeat();
        mainListener.onRepeatToggled(true);
        mainListener.onRepeatTypeChange(repeatBetterSchedule.type);
        mainListener.onRepeatBasisChange(repeatBetterSchedule.repeatBasis);
        mainListener.onRepeatDelayChange(repeatBetterSchedule.delay);
    }

    private void removeRepeatBetterSchedule() {
        repeatBetterSchedule = null;
        mainListener.onRepeatToggled(false);
    }

    public boolean occursOn(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);

        calendar.setTime(end);
        if (calendar.get(Calendar.YEAR) < year || calendar.get(Calendar.DAY_OF_YEAR) < dayOYear)
            return false;

        calendar.setTime(start);
        if (calendar.get(Calendar.YEAR) > year || calendar.get(Calendar.DAY_OF_YEAR) > dayOYear)
            return false;

        if (occursOn(calendar, start, year, dayOYear) || occursOn(calendar, end, year, dayOYear))
            return true;

        if (repeatBetterSchedule == null)
            return false;

        return repeatBetterSchedule.occursOn(calendar, year, dayOYear);
    }

    private boolean occursOn(Calendar calendar, Date myDate, int year, int dayOYear) {
        calendar.setTime(myDate);
        return calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.DAY_OF_YEAR) == dayOYear;
    }

    /**
     * @param range exclusive of today i.e.) checks (today, today + range]
     * @return true if this BetterSchedule occurs within the range, false otherwise
     */
    public boolean occursInXDays(int range) {
        Date today = new Date();
        if (today.before(start) || today.after(end)) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        for (int i = 1; i <= range; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (occursOn(calendar.getTime()))
                return true;
        }
        return false;
    }

    public int getDaysDueIn() {
        Date today = new Date();
        if (!today.before(start))
            return 0;
        return (int) Math.ceil((start.getTime() - today.getTime()) / DAY_IN_MILLS);
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public int getCompareType() {
        return DATE;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return start;
    }

    @NonNull
    @Override
    public String toString() {
        return consoleFormat("");
    }

    @NonNull
    @Override
    public String consoleFormat(String p) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String format = "";

        if (start.equals(end)) {
            format += dateFormat.format(start);
        } else {
            format += "start " + dateFormat.format(start) + ", end " + dateFormat.format(end);
        }

        if (repeatBetterSchedule != null) {
            format += ", repeats every " +
                    repeatBetterSchedule.delay + " " + repeatBetterSchedule.formatRepeatBasis();

            if (repeatBetterSchedule.hasMonths()) {
                format += " on months " + repeatBetterSchedule.formatMonths();
            } else if (repeatBetterSchedule.hasWeeks()) {
                format += " on " + repeatBetterSchedule.formatWeeks() + "weeks";
            } else if (repeatBetterSchedule.hasDays()) {
                format += " on days " + repeatBetterSchedule.formatMonths();
            }
        }

        return format.trim();
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("start", start.getTime());
        json.putPrimitive("end", end.getTime());
        json.putPrimitive("endType", endType.toString());
        if (repeatBetterSchedule != null)
            json.putStructure("repeatBetterSchedule", repeatBetterSchedule.toJSON());
        return json.getRootNode();
    }

    @Override
    public BetterSchedule fromJSON(JSONElement json) {
        start = new Date(json.<Long>valueOf("start"));
        end = new Date(json.<Long>valueOf("end"));
        endType = EndType.valueOf(json.valueOf("endType"));
        if (json.elementExists("repeatBetterSchedule"))
            repeatBetterSchedule = new Repeat().fromJSON(json.search("repeatBetterSchedule"));
        return this;
    }

    public enum EndType {
        ON_DATE, AFTER_TIMES
    }

    public enum RepeatType {
        ITERATIVE, RELATIVE
    }

    public enum RepeatBasis {
        DAILY(0), WEEKLY(1), MONTHLY(2), YEARLY(3);

        int index;

        RepeatBasis(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public class Repeat {

        private RepeatType type = RepeatType.ITERATIVE;
        private RepeatBasis repeatBasis = RepeatBasis.WEEKLY;
        private int delay = 1;
        private int[] days = new int[0];
        private int[] weeks = new int[0];
        private int[] months = new int[0];

        public RepeatType getType() {
            return type;
        }

        public void setType(RepeatType type) {
            if (type == RepeatType.RELATIVE) {
                if (repeatBasis == RepeatBasis.DAILY)
                    throw new InvalidCombinationException(
                            "Repeat type " + type + " cannot be applied with repeat basis " + repeatBasis);
                else if (!hasDays() && !hasWeeks() && !hasMonths())
                    throw new UnsupportedFunctionCallException(
                            "Please set the days/weeks/months of recurrence first");
            }
            this.type = type;
            mainListener.onRepeatTypeChange(this.type);
        }

        public RepeatBasis getRepeatBasis() {
            return repeatBasis;
        }

        public void setRepeatBasis(RepeatBasis repeatBasis) {
            if (type == RepeatType.RELATIVE && repeatBasis == RepeatBasis.DAILY)
                throw new InvalidCombinationException(
                        "Repeat type " + type + " cannot be applied with repeat basis " + repeatBasis);
            this.repeatBasis = repeatBasis;
            mainListener.onRepeatBasisChange(this.repeatBasis);
        }

        private String formatRepeatBasis() {
            String[] ref = {"days", "weeks", "months", "years"};
            return ref[repeatBasis.getIndex()];
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
            mainListener.onRepeatDelayChange(this.delay);
        }

        public void clearRelatives() {
            setDays();
            setWeeks();
            setMonths();
        }

        public boolean hasDays() {
            return days.length > 0;
        }

        public int[] getDays() {
            return days;
        }

        public void setDays(int... days) {
            checkRange(7, days);
            Arrays.sort(days);
            this.days = days;
        }

        private String formatDays() {
            String[] ref = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};

            StringBuilder sb = new StringBuilder();
            for (int i : days)
                sb.append(ref[i]).append(", ");

            return sb.toString().trim();
        }

        public boolean hasWeeks() {
            return weeks.length > 0;
        }

        int[] getWeeks() {
            return weeks;
        }

        public void setWeeks(int... weeks) {
            checkRange(4, weeks);
            Arrays.sort(weeks);
            this.weeks = weeks;
        }

        private String formatWeeks() {
            String[] ref = {"1st", "2nd", "3rd", "4th"};

            StringBuilder sb = new StringBuilder();
            for (int i : weeks)
                sb.append(ref[i]).append(", ");

            return sb.toString().trim();
        }

        public boolean hasMonths() {
            return months.length > 0;
        }

        int[] getMonths() {
            return months;
        }

        public void setMonths(int... months) {
            checkRange(12, months);
            Arrays.sort(months);
            this.months = months;
        }

        private String formatMonths() {
            String[] ref = {
                    "jan", "feb", "mar", "apr", "may", "jun",
                    "jul", "aug", "sep", "oct", "nov", "dec"};

            StringBuilder sb = new StringBuilder();
            for (int i : weeks)
                sb.append(ref[i]).append(", ");

            return sb.toString().trim();
        }

        private void checkRange(int limit, int... selectors) {
            for (int i = 0; i < selectors.length; i++) {
                int selector = selectors[i];
                if (selector < 1 || selector > limit)
                    throw new InvalidRangeException(selector + " is not valid! Must be between [" +
                            1 + ", " + limit + "]");

                for (int j = 0; j < i; j++) {
                    if (selectors[j] == selector)
                        throw new InvalidArgumentException(
                                "Selector " + selector + " has already been chosen!");
                }
            }
        }

        private boolean occursOn(Calendar calendar, int year, int dayOYear) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.DAY_OF_YEAR, dayOYear);
            long target = calendar.getTimeInMillis();
            calendar.setTime(start);
            long next = calendar.getTimeInMillis() + getIntervalInMillis(calendar);
            while (next < end.getTime() && next <= target) {
                calendar.setTimeInMillis(next);
                if (calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.DAY_OF_YEAR) == dayOYear)
                    return true;

                next += getIntervalInMillis(calendar);
            }
            return false;
        }

        private long getIntervalInMillis(Calendar cal) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cal.getTimeInMillis());
            if (type == RepeatType.ITERATIVE && !(repeatBasis == RepeatBasis.WEEKLY && hasDays())) {
                switch (repeatBasis) {
                    case DAILY:
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        break;
                    case WEEKLY:
                        calendar.add(Calendar.WEEK_OF_YEAR, 1);
                        break;
                    case MONTHLY:
                        calendar.add(Calendar.MONTH, 1);
                        break;
                    case YEARLY:
                        calendar.add(Calendar.YEAR, 1);
                        break;
                }
            } else {
                int nextDay = -1;
                int nextWeek = -1;
                int nextMonth = -1;
                boolean addWeek = false, addMonth = false, addYear = false;
                outer:
                switch (repeatBasis) {
                    case YEARLY:
                        if (hasMonths()) {
                            int monthOYear = calendar.get(Calendar.MONTH);
                            if (monthOYear >= months[months.length - 1]) {
                                nextMonth = months[0];
                            } else {
                                for (int month : months) {
                                    if (monthOYear < month) {
                                        nextMonth = month;
                                        break outer;
                                    }
                                }
                                nextMonth = months[0];
                                addYear = true;
                            }
                        }
                    case MONTHLY:
                        if (hasWeeks()) {
                            int weekOMonth = calendar.get(Calendar.WEEK_OF_MONTH);
                            if (weekOMonth >= weeks[weeks.length - 1]) {
                                nextWeek = weeks[0];
                            } else {
                                for (int week : weeks) {
                                    if (weekOMonth < week) {
                                        nextWeek = week;
                                        break outer;
                                    }
                                }
                                nextWeek = weeks[0];
                                addMonth = true;
                            }
                        }
                        break;
                    case WEEKLY:
                        if (hasDays()) {
                            int dayOWeek = calendar.get(Calendar.DAY_OF_WEEK);
                            if (dayOWeek >= days[days.length - 1]) {
                                nextDay = days[0];
                            } else {
                                for (int day : days) {
                                    if (dayOWeek < day) {
                                        nextDay = (day + 1) > 7 ? 1 : day + 1;
                                        break outer;
                                    }
                                }
                                nextDay = days[0];
                                addWeek = true;
                            }
                        }
                }
                if (nextDay > 0)
                    calendar.set(Calendar.DAY_OF_WEEK, nextDay);
                if (nextWeek > 0)
                    calendar.set(Calendar.WEEK_OF_MONTH, nextWeek);
                if (nextMonth > 0)
                    calendar.set(Calendar.MONTH, nextMonth);

                if (addWeek)
                    calendar.add(Calendar.WEEK_OF_MONTH, 1);
                else if (addMonth)
                    calendar.add(Calendar.MONTH, 1);
                else if (addYear)
                    calendar.add(Calendar.YEAR, 1);
            }
            return calendar.getTimeInMillis() - cal.getTimeInMillis();
        }

        public void endAfterXTimes(int times) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            for (int i = 1; i <= times; i++) {
                System.out.println("INTERVAL=" + getIntervalInMillis(calendar));
                calendar.add(Calendar.MILLISECOND, (int) getIntervalInMillis(calendar));
            }
            setEndType(EndType.AFTER_TIMES);
            setEnd(calendar.getTime());
        }

        public int getMaxOccurrences() {
            if (start.equals(end))
                return 1;

            int times = 0;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            while (calendar.getTime().before(end) || calendar.getTime().equals(end)) {
                times++;
                calendar.add(Calendar.MILLISECOND, (int) getIntervalInMillis(calendar));
            }
            return times;
        }

        public JSONElement toJSON() {
            EasyJSON json = EasyJSON.create();
            json.putPrimitive("type", type.toString());
            json.putPrimitive("repeatBasis", repeatBasis.toString());
            json.putPrimitive("delay", delay);
            json.putArray("days");
            for (int day : days) {
                json.search("days").putPrimitive(day);
            }
            json.putArray("weeks");
            for (int week : weeks) {
                json.search("weeks").putPrimitive(week);
            }
            json.putArray("months");
            for (int month : months) {
                json.search("months").putPrimitive(month);
            }
            return json.getRootNode();
        }

        public Repeat fromJSON(JSONElement json) {
            type = RepeatType.valueOf(json.valueOf("type"));
            repeatBasis = RepeatBasis.valueOf(json.valueOf("repeatBasis"));
            delay = json.<Long>valueOf("delay").intValue();
            days = new int[json.search("days").getChildren().size()];
            for (int i = 0; i < days.length; i++) {
                days[i] = json.search("days").getChildren().get(i).<Long>getValue().intValue();
            }
            weeks = new int[json.search("weeks").getChildren().size()];
            for (int i = 0; i < weeks.length; i++) {
                weeks[i] = json.search("weeks").getChildren().get(i).<Long>getValue().intValue();
            }
            months = new int[json.search("months").getChildren().size()];
            for (int i = 0; i < months.length; i++) {
                months[i] = json.search("months").getChildren().get(i).<Long>getValue().intValue();
            }
            return this;
        }
    }

    public interface ChangeListener {
        void onStartChange(Date newStart);

        void onEndChange(Date newEnd);

        void onEndTypeChange(EndType newEndType);

        void onRepeatToggled(boolean enabled);

        void onRepeatTypeChange(RepeatType newType);

        void onRepeatBasisChange(RepeatBasis newBasis);

        void onRepeatDelayChange(int newDelay);
    }

    private ChangeListener mainListener = new ChangeListener() {
        @Override
        public void onStartChange(Date newStart) {
            for (ChangeListener listener : listeners) {
                listener.onStartChange(newStart);
            }
            if (repeatBetterSchedule != null && repeatBetterSchedule.type == RepeatType.RELATIVE) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(newStart);
                switch (repeatBetterSchedule.repeatBasis) {
                    case WEEKLY:
                        if (repeatBetterSchedule.getDays().length == 1) {
                            if (repeatBetterSchedule.hasDays() && !repeatBetterSchedule.hasWeeks() && !repeatBetterSchedule.hasMonths())
                                repeatBetterSchedule.clearRelatives();
                            repeatBetterSchedule.setDays(calendar.get(Calendar.DAY_OF_WEEK));
                        }
                        break;
                    case MONTHLY:
                        if (repeatBetterSchedule.getWeeks().length == 1) {
                            if (!repeatBetterSchedule.hasDays() && repeatBetterSchedule.hasWeeks() && !repeatBetterSchedule.hasMonths())
                                repeatBetterSchedule.clearRelatives();
                            repeatBetterSchedule.setWeeks(calendar.get(Calendar.WEEK_OF_MONTH));
                        }
                        break;
                    case YEARLY:
                        if (repeatBetterSchedule.getMonths().length == 1) {
                            if (!repeatBetterSchedule.hasDays() && !repeatBetterSchedule.hasWeeks() && repeatBetterSchedule.hasMonths())
                                repeatBetterSchedule.clearRelatives();
                            repeatBetterSchedule.setMonths(calendar.get(Calendar.MONTH));
                        }
                        break;
                }
            }
        }

        @Override
        public void onEndChange(Date newEnd) {
            for (ChangeListener listener : listeners) {
                listener.onEndChange(newEnd);
            }
        }

        @Override
        public void onEndTypeChange(EndType newEndType) {
            for (ChangeListener listener : listeners) {
                listener.onEndTypeChange(newEndType);
            }
        }

        @Override
        public void onRepeatToggled(boolean enabled) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatToggled(enabled);
            }
        }

        @Override
        public void onRepeatBasisChange(RepeatBasis newBasis) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatBasisChange(newBasis);
            }
        }

        @Override
        public void onRepeatDelayChange(int newDelay) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatDelayChange(newDelay);
            }
        }

        @Override
        public void onRepeatTypeChange(RepeatType newType) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatTypeChange(newType);
            }
        }
    };
}