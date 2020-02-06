package com.uniques.ourhouse.util;


import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.exception.InvalidArgumentException;
import com.uniques.ourhouse.util.exception.InvalidCombinationException;
import com.uniques.ourhouse.util.exception.InvalidRangeException;
import com.uniques.ourhouse.util.exception.UnsupportedFunctionCallException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Schedule implements Comparable, Model {
    private static final long DAY_IN_MILLS = 66400000;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.getDefault());

    @NonNull
    private Date start;
    @NonNull
    private Date end;
    @Nullable
    private RepeatSchedule repeatSchedule;
    @NonNull
    private EndType endType;
    private int endAfterTimes;
    private List<ChangeListener> listeners;

    public Schedule() {
        start = new Date();
        end = start;
        endType = EndType.ON_DATE;
        endAfterTimes = 1;
        listeners = new ArrayList<>();
    }

    @NonNull
    public Date getStart() {
        return start;
    }

    public void setStart(@NonNull Date start) {
        this.start = start;
        mainListener.onStartChange(start);
    }

    @NonNull
    public Date getEnd() {
        return end;
    }

    public void setEnd(@NonNull Date end) {
        this.end = end;
        boolean notOnDate = endType != EndType.ON_DATE;
        if (notOnDate) {
            this.endType = EndType.ON_DATE;
            this.endAfterTimes = 1;
        }
        mainListener.onEndChange(end);
        if (notOnDate) mainListener.onEndTypeChange(EndType.ON_DATE, endAfterTimes);
    }

    @NonNull
    public EndType getEndType() {
        return endType;
    }

    public int getEndAfterTimes() {
        return endAfterTimes;
    }

    public void initRepeatSchedule() {
        repeatSchedule = new RepeatSchedule();
        mainListener.onRepeatToggled(true);
    }

    public void cancelRepeatSchedule() {
        repeatSchedule = null;
        end = start;
        mainListener.onRepeatToggled(false);
    }

    public boolean hasRepeatSchedule() {
        return repeatSchedule != null;
    }

    public void setRepeatToIterative() {
        Objects.requireNonNull(repeatSchedule);
        repeatSchedule.relativeSelection = new int[0];
        repeatSchedule.repeatType = RepeatType.ITERATIVE;
        mainListener.onRepeatTypeChange(RepeatType.ITERATIVE);
    }

    public int getRepeatIterativeDelay() {
        return Objects.requireNonNull(repeatSchedule).iterativeDelay;
    }

    public void setRepeatIterativeDelay(int delay) {
        Objects.requireNonNull(repeatSchedule);
        if (repeatSchedule.repeatType == RepeatType.ITERATIVE) {
            repeatSchedule.iterativeDelay = delay;
            mainListener.onRepeatIterativeDelayChange(delay);
        } else {
            throw new UnsupportedFunctionCallException(
                    "Schedule " + this + " doesn't hve an ITERATIVE repeatSchedule");
        }
    }

    public void setRepeatToRelative(int... selections) {
        Objects.requireNonNull(repeatSchedule);
        if (selections.length == 0)
            throw new InvalidArgumentException("int[] selections cannot be empty");
        for (int i = 0; i + 1 < selections.length; i++) {
            if (selections[i] >= selections[i + 1])
                throw new InvalidRangeException(
                        "int[] selections must be an array set sorted in ascending order!");
        }
        repeatSchedule.iterativeDelay = 1;
        repeatSchedule.relativeSelection = selections;
        repeatSchedule.repeatType = RepeatType.RELATIVE;
        mainListener.onRepeatTypeChange(RepeatType.RELATIVE);
        mainListener.onRepeatRelativeSelectionChange(selections);
    }

    public boolean hasRepeatRelativeSelections() {
        return Objects.requireNonNull(repeatSchedule).relativeSelection.length > 0;
    }

    public void clearRepeatRelativeSelections() {
        Objects.requireNonNull(repeatSchedule).relativeSelection = new int[0];
    }

    public int[] getRepeatRelativeSelections() {
        return Objects.requireNonNull(repeatSchedule).relativeSelection;
    }

    public RepeatType getRepeatType() {
        return Objects.requireNonNull(repeatSchedule).repeatType;
    }

    public RepeatBasis getRepeatBasis() {
        return Objects.requireNonNull(repeatSchedule).repeatBasis;
    }

    public void setRepeatBasis(RepeatBasis basis) {
        Objects.requireNonNull(repeatSchedule);
        if (repeatSchedule.repeatType == RepeatType.RELATIVE && (basis == RepeatBasis.DAILY || basis == RepeatBasis.YEARLY))
            throw new InvalidCombinationException(
                    "Schedule " + this + " cannot have a (DAILY or YEARLY) and RELATIVE repeatSchedule");

        repeatSchedule.repeatBasis = basis;
        mainListener.onRepeatBasisChange(basis);
    }

    /**
     * Checks if the given day falls within this schedule's range
     *
     * @param date date object representing today's date
     * @return true if today is within this schedule's range
     */
    public boolean occursOn(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dayOYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);

        calendar.setTime(start);
        if (calendar.get(Calendar.DAY_OF_YEAR) > dayOYear || calendar.get(Calendar.YEAR) > year)
            return false;
        if (calendar.get(Calendar.DAY_OF_YEAR) == dayOYear && calendar.get(Calendar.YEAR) == year)
            return true;

        calendar.setTime(end);
        if (calendar.get(Calendar.DAY_OF_YEAR) < dayOYear || calendar.get(Calendar.YEAR) < year)
            return false;
        if (calendar.get(Calendar.DAY_OF_YEAR) == dayOYear && calendar.get(Calendar.YEAR) == year)
            return true;

        if (repeatSchedule != null) {
            calendar.setTimeInMillis(repeatSchedule.getNextOccurrenceInMills(start.getTime()));
            while (!(calendar.getTime().after(end))) {
                if (calendar.get(Calendar.DAY_OF_YEAR) == dayOYear && calendar.get(Calendar.YEAR) == year)
                    return true;

                calendar.setTimeInMillis(repeatSchedule.getNextOccurrenceInMills(calendar.getTimeInMillis()));
            }
        }
        return false;
    }

    /**
     * @param range exclusive of today i.e.) checks (today, today + range]<br>
     *              if range == 0 the function returns the value of {@link #occursOn(Date) occursOn(Date)}
     * @return true if this schedule occurs within the range, false otherwise
     */
    public boolean occursInXDays(int range) {
        if (range == 0) return occursOn(new Date());
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        int dayOYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        calendar.setTime(end);
        if (calendar.get(Calendar.DAY_OF_YEAR) < dayOYear || calendar.get(Calendar.YEAR) < year)
            return false;

        calendar.setTime(new Date());
        for (int i = 1; i <= range; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (occursOn(calendar.getTime()))
                return true;
        }
        return false;
    }

    public void endAfterXTimes(int times) {
        if (repeatSchedule == null) throw new UnsupportedFunctionCallException(
                "endAfterXTimes(int) is only usable when the Schedule has a RepeatSchedule");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.DATE, -1);
        for (int i = 1; i <= times; i++) {
            calendar.setTimeInMillis(repeatSchedule.getNextOccurrenceInMills(calendar.getTimeInMillis()));
        }
        end = calendar.getTime();
        calendar.setTime(start);
        calendar.add(Calendar.DATE, -1);
        calendar.setTimeInMillis(repeatSchedule.getNextOccurrenceInMills(calendar.getTimeInMillis()));
        if (calendar.getTime().after(start)) {
            start = calendar.getTime();
            mainListener.onStartChange(start);
        }
        boolean notAfterTimes = endType != EndType.AFTER_TIMES;
        if (notAfterTimes) {
            this.endType = EndType.AFTER_TIMES;
            this.endAfterTimes = times;
        }
        mainListener.onEndChange(end);
        if (notAfterTimes) mainListener.onEndTypeChange(EndType.AFTER_TIMES, endAfterTimes);
    }

    public int getMaxOccurrences() {
        if (repeatSchedule == null) return 1;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        int occurrences = 0;
        while (!(calendar.getTime().after(end))) {
            occurrences++;
            calendar.setTimeInMillis(repeatSchedule.getNextOccurrenceInMills(calendar.getTimeInMillis()));
        }
        return occurrences;
    }

    public int getDaysTillFirstOccurrence() {
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
        if (occursOn(start) || repeatSchedule == null) {
            return start;
        } else {
            return new Date(repeatSchedule.getNextOccurrenceInMills(start.getTime()));
        }
    }

    @Override
    public String consoleFormat(String prefix) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        if (repeatSchedule == null || start.equals(end)) {
            return dateFormat.format(start);
        } else {
            StringBuilder sb = new StringBuilder("Every ");
            if (repeatSchedule.repeatType == RepeatType.ITERATIVE) {
                sb.append(repeatSchedule.iterativeDelay);
                switch (repeatSchedule.repeatBasis) {
                    case DAILY:
                        sb.append(" days");
                        break;
                    case WEEKLY:
                        sb.append(" weeks");
                        break;
                    case MONTHLY:
                        sb.append(" months");
                        break;
                    case YEARLY:
                        sb.append(" years");
                        break;
                }
            } else {
                Calendar cal = Calendar.getInstance();
                for (int i = 0; i < repeatSchedule.relativeSelection.length; i++) {
                    switch (repeatSchedule.repeatBasis) {
                        case WEEKLY:
                            cal.set(Calendar.DAY_OF_WEEK, repeatSchedule.relativeSelection[i]);
                            if (repeatSchedule.relativeSelection.length > 2) {
                                sb.append(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
                            } else {
                                sb.append(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
                            }
                            break;
                        case MONTHLY:
                            cal.set(Calendar.MONTH, repeatSchedule.relativeSelection[i]);
                            if (repeatSchedule.relativeSelection.length > 2) {
                                sb.append(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
                            } else {
                                sb.append(cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
                            }
                            break;
                    }
                    if (i < repeatSchedule.relativeSelection.length - 2)
                        sb.append(", ");
                    else if (i == repeatSchedule.relativeSelection.length - 2)
                        sb.append(" and ");
                }
            }
            return sb.toString().trim();
        }
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("start", dateFormat.format(start));
        json.putPrimitive("end", dateFormat.format(end));
        json.putPrimitive("endType", endType.toString());
        json.putPrimitive("endAfterTimes", endAfterTimes);
        if (repeatSchedule != null)
            json.putStructure("repeatSchedule", repeatSchedule.toJSON());
        return json.getRootNode();
    }

    @Override
    public Schedule fromJSON(JSONElement json) {
        try {
            start = dateFormat.parse(json.valueOf("start"));
        } catch (ParseException e) {
            e.printStackTrace();
//            start = new Date(json.<Long>valueOf("start"));
        }
        try {
            end = dateFormat.parse(json.valueOf("end"));
        } catch (ParseException e) {
            e.printStackTrace();
//            end = new Date(json.<Long>valueOf("end"));
        }
        endType = EndType.valueOf(json.valueOf("endType"));
        endAfterTimes = json.<Long>valueOf("endAfterTimes").intValue();
        if (json.elementExists("repeatSchedule"))
            repeatSchedule = new RepeatSchedule().fromJSON(json.search("repeatSchedule"));
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return consoleFormat("");
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

    private class RepeatSchedule implements Model {
        @NonNull
        private RepeatType repeatType = RepeatType.ITERATIVE;
        @NonNull
        private RepeatBasis repeatBasis = RepeatBasis.WEEKLY;
        private int iterativeDelay = 1;
        @NonNull
        private int[] relativeSelection = new int[0];

        private long getNextOccurrenceInMills(long fromTime) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(fromTime);
            if (repeatType == RepeatType.ITERATIVE) {
                switch (repeatBasis) {
                    case DAILY:
                        calendar.add(Calendar.DAY_OF_MONTH, iterativeDelay);
                        break;
                    case WEEKLY:
                        calendar.add(Calendar.WEEK_OF_MONTH, iterativeDelay);
                        break;
                    case MONTHLY:
                        calendar.add(Calendar.MONTH, iterativeDelay);
                        break;
                    case YEARLY:
                        calendar.add(Calendar.YEAR, iterativeDelay);
                        break;
                }
            } else {
                basis:
                switch (repeatBasis) {
                    case DAILY:
                        throw new InvalidCombinationException(
                                "Schedule " + this + " cannot have a DAILY & RELATIVE repeatSchedule");
                    case WEEKLY:
                        int dayOWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        for (int i : relativeSelection) {
                            if (dayOWeek < i) {
                                calendar.set(Calendar.DAY_OF_WEEK, i);
                                break basis;
                            }
                        }
                        calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        calendar.set(Calendar.DAY_OF_WEEK, relativeSelection[0]);
                        break;
                    case MONTHLY:
                        int dayOWeekOMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                        for (int i : relativeSelection) {
                            if (dayOWeekOMonth < i) {
                                calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, i);
                                break basis;
                            }
                        }
                        calendar.add(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, relativeSelection[0]);
                        break;
                    case YEARLY:
                        throw new InvalidCombinationException(
                                "Schedule doesn't support RELATIVE repeatSchedules with a YEARLY basis");
                }
            }
            return calendar.getTimeInMillis();
        }

        @Override
        public String consoleFormat(String prefix) {
            StringBuilder sb = new StringBuilder("repeats ");
            sb.append(repeatType.toString()).append("ly every ");
            if (repeatType == RepeatType.ITERATIVE) {
                sb.append(iterativeDelay).append(repeatBasis.toString());
            } else {
                sb.append(Arrays.toString(relativeSelection));
                if (repeatBasis == RepeatBasis.WEEKLY) sb.append(" days of the week");
                else if (repeatBasis == RepeatBasis.MONTHLY) sb.append(" weeks of the month");
                else sb.append(" months of the year");
            }
            return sb.toString();
        }

        @Override
        public JSONElement toJSON() {
            EasyJSON json = EasyJSON.create();
            json.putPrimitive("repeatType", repeatType.toString());
            json.putPrimitive("repeatBasis", repeatBasis.toString());
            json.putPrimitive("iterativeDelay", iterativeDelay);
            json.putArray("relativeSelection");
            for (int i : relativeSelection) {
                json.search("relativeSelection").putPrimitive(i);
            }
            return json.getRootNode();
        }

        @Override
        public RepeatSchedule fromJSON(JSONElement json) {
            repeatType = RepeatType.valueOf(json.valueOf("repeatType"));
            repeatBasis = RepeatBasis.valueOf(json.valueOf("repeatBasis"));
            iterativeDelay = json.<Long>valueOf("iterativeDelay").intValue();
            relativeSelection = new int[json.search("relativeSelection").getChildren().size()];
            for (int i = 0; i < relativeSelection.length; i++) {
                relativeSelection[i] = json.search("relativeSelection").getChildren()
                        .get(i).<Long>getValue().intValue();
            }
            return this;
        }
    }

    public interface ChangeListener {
        void onStartChange(Date newStart);

        void onEndChange(Date newEnd);

        void onEndTypeChange(EndType endType, int endAfterTimes);

        void onRepeatToggled(boolean enabled);

        void onRepeatTypeChange(RepeatType newType);

        void onRepeatBasisChange(RepeatBasis newBasis);

        void onRepeatIterativeDelayChange(int newDelay);

        void onRepeatRelativeSelectionChange(int[] selections);

        void onChange();
    }

    private ChangeListener mainListener = new ChangeListener() {
        @Override
        public void onStartChange(Date newStart) {
            for (ChangeListener listener : listeners) {
                listener.onStartChange(newStart);
                listener.onChange();
            }
        }

        @Override
        public void onEndChange(Date newEnd) {
            for (ChangeListener listener : listeners) {
                listener.onEndChange(newEnd);
                listener.onChange();
            }
        }

        @Override
        public void onEndTypeChange(EndType endType, int endAfterTimes) {
            for (ChangeListener listener : listeners) {
                listener.onEndTypeChange(endType, endAfterTimes);
                listener.onChange();
            }
        }

        @Override
        public void onRepeatToggled(boolean enabled) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatToggled(enabled);
                listener.onChange();
            }
        }

        @Override
        public void onRepeatBasisChange(RepeatBasis newBasis) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatBasisChange(newBasis);
                listener.onChange();
            }
        }

        @Override
        public void onRepeatIterativeDelayChange(int newDelay) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatIterativeDelayChange(newDelay);
                listener.onChange();
            }
        }

        @Override
        public void onRepeatRelativeSelectionChange(int[] selections) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatRelativeSelectionChange(selections);
                listener.onChange();
            }
        }

        @Override
        public void onRepeatTypeChange(RepeatType newType) {
            for (ChangeListener listener : listeners) {
                listener.onRepeatTypeChange(newType);
                listener.onChange();
            }
        }

        @Override
        public void onChange() {
            for (ChangeListener listener : listeners) {
                listener.onChange();
            }
        }
    };
}
