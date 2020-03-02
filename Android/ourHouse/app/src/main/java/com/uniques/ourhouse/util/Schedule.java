package com.uniques.ourhouse.util;

import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.exception.InvalidArgumentException;
import com.uniques.ourhouse.util.exception.InvalidCombinationException;
import com.uniques.ourhouse.util.exception.InvalidRangeException;
import com.uniques.ourhouse.util.exception.UnsupportedFunctionCallException;

import org.bson.Document;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import androidx.annotation.NonNull;

/**
 * <p>
 * This class helps simplify the process of managing 'schedules'
 * A schedule could be a one-time event but typically, it is a recurring event.
 * The event can occur at pre-defined daily/weekly/monthly/yearly intervals, or it could occur
 * after a certain number of days/weeks/months/years, or a combination of the two (e.g. 2nd Wednesday of each month)
 * </p>
 * <br/>
 * <p>
 * To create a schedule, first get a new object by calling <i><code>new Schedule()</code></i>.
 * Then you have a few things available to play with, namely:
 * <ol>
 * <li>start date</li>
 * <li>end date</li>
 * <li>end type <i>(end by a specific date, or after the event occurs n times)</i></li>
 * <li>repeat schedule</li>
 * </ol>
 * The first 3 behave exactly as the name describes, whilst the 4th (repeat schedule) has further functionality, namely:
 * <ol>
 * <li>{@link RepeatType}: ITERATIVE (i.e. every n days/weeks/months/years) vs RELATIVE (e.g. every 2nd & 4th Thursday of the month)</li>
 * <li>{@link RepeatBasis} DAILY/WEEKLY/MONTHLY/YEARLY</li>
 * <li>delay (the value of n in the above repeat-type example) (only applies to ITERATIVE repeat schedules</li>
 * <li>days, weeks, months (i.e. on which days-of-the-week/weeks-of-the-month/months-of-the-year should an event occur)</li>
 * </ol>
 * </p>
 * <br/>
 * <p>
 * <b>On the topic of updating views...</b>
 * <br/>
 * Simply create a new {@link ChangeListener} and attach it to this schedule with {@link #attachChangeListener(ChangeListener)}
 * to listen for changes to this schedule. This is exceptionally useful for auto-updating views.
 * </p>
 * <br/>
 *
 * @author Victor Olaitan (victorolaitan.xyz)
 * @see Repeat#setDays(int...) Repeat.setDays(int...)
 * @see Repeat#setWeeks(int...) Repeat.setWeeks(int...)
 * @see Repeat#setMonths(int...) Repeat.setMonths(int...)
 */
@SuppressWarnings("unused | WeakerAccess")
public class Schedule implements Comparable, Model {
    private static final long DAY_IN_MILLS = 66400000;

    @NonNull
    private Date start;
    @NonNull
    private Date end;
    @NonNull
    private EndType endType;
    private Repeat repeatBetterSchedule;
    private List<ChangeListener> listeners = new ArrayList<>();
    private static boolean pauseStartEndChecking, pendingStartEndChange;

    public Schedule() {
        this.start = new Date();
        this.end = this.start;
        endType = EndType.ON_DATE;
    }

    public Schedule(@NonNull Date start, @NonNull Date end, Repeat repeatBetterSchedule) {
        this.start = start;
        this.end = end;
        this.repeatBetterSchedule = repeatBetterSchedule;
        endType = EndType.ON_DATE;
    }

    /**
     * will pause bounds checking on start and end Dates
     * till (the next time both start and end dates are updated) or (a date is updated twice).
     * <br/>
     * Use this if you want to set the end date to be before the start date or vice versa.
     * <br/>
     * Don't forget to call {@link #resumeStartEndBoundsChecking()} when you're done.
     */
    public static void pauseStartEndBoundsChecking() {
        pauseStartEndChecking = true;
        pendingStartEndChange = false;
    }

    /**
     * Resumes bounds checking on start and end Dates. <b>This method MUST be called after calling</b>
     * {@link #pauseStartEndBoundsChecking()}
     */
    public static void resumeStartEndBoundsChecking() {
        pauseStartEndChecking = false;
        pendingStartEndChange = false;
    }

    /**
     * Get's the initial date of this schedule
     */
    @NonNull
    public Date getStart() {
        return start;
    }

    /**
     * Update the initial date of this schedule.
     * <br/>
     * <p>NOTE: this method will perform bounds checking on the supplied start date and will throw
     * {@link InvalidArgumentException} if the supplied start date occurs after this schedule's end date</p>
     * <br/>
     * <p>To temporarily disable this functionality, call {@link #pauseStartEndBoundsChecking()} before setting the start/end dates</p>
     */
    public void setStart(@NonNull Date start) {
        if (!pauseStartEndChecking) {
            if (start.after(end))
                throw new InvalidArgumentException("Start date specified is after this Schedule's end date!");
        } else if (pendingStartEndChange) {
            resumeStartEndBoundsChecking();
        } else {
            pendingStartEndChange = true;
        }
        this.start = start;
        mainListener.onStartChange(this.start);
    }

    /**
     * Gets the (strict) final end date of this schedule
     */
    @NonNull
    public Date getEnd() {
        return end;
    }

    /**
     * Update the end date of this schedule.
     * <br/>
     * <p>NOTE: this method will perform bounds checking on the supplied end date and will throw
     * {@link InvalidArgumentException} if the supplied end date occurs before this schedule's start date</p>
     * <br/>
     * <p>To temporarily disable this functionality, call {@link #pauseStartEndBoundsChecking()} before setting the end/start dates</p>
     */
    public void setEnd(@NonNull Date end) {
        if (!pauseStartEndChecking) {
            if (end.before(start))
                throw new InvalidArgumentException("End date specified is before this Schedule's start date!");
        } else if (pendingStartEndChange) {
            resumeStartEndBoundsChecking();
        } else {
            pendingStartEndChange = true;
        }
        this.end = end;
        if (this.endType != EndType.ON_DATE) {
            this.endType = EndType.ON_DATE;
            mainListener.onEndTypeChange(this.endType);
        }
        mainListener.onEndChange(this.end);
    }

    /**
     * Gets this schedule's end type
     */
    @NonNull
    public EndType getEndType() {
        return endType;
    }

    /**
     * Updates this schedule's end type. This method will auto-initialize this schedule's repeat-schedule.
     */
    public void setEndType(@NonNull EndType endType) {
        if (endType == EndType.AFTER_TIMES && repeatBetterSchedule == null)
            initRepeatSchedule();

        this.endType = endType;
    }

    /**
     * Makes this Schedule end after 1000 occurrences.
     * <br/>
     * Will initialise a repeat-schedule if one does not exist yet.
     * <br/>
     * Note: this method can take a little longer than usual (due to it trying to find the date of the final occurrence)
     */
    public void setEndPseudoIndefinite() {
        if (repeatBetterSchedule == null) {
            initRepeatSchedule();
        }
        repeatBetterSchedule.endAfterXTimes(1000);
    }

    public Repeat getRepeatSchedule() {
        return repeatBetterSchedule;
    }

    /**
     * Initializes this schedule's repeat-schedule
     *
     * @see Repeat Repeat class
     */
    public void initRepeatSchedule() {
        repeatBetterSchedule = new Repeat();
        mainListener.onRepeatToggled(true);
        mainListener.onRepeatTypeChange(repeatBetterSchedule.type);
        mainListener.onRepeatBasisChange(repeatBetterSchedule.repeatBasis);
        mainListener.onRepeatDelayChange(repeatBetterSchedule.delay);
    }

    /**
     * Prunes the current repeat-schedule from this schedule.
     * Has the same effect whether or not a repeat-schedule is set.
     */
    private void removeRepeatBetterSchedule() {
        repeatBetterSchedule = null;
        mainListener.onRepeatToggled(false);
    }

    /**
     * Checks if this schedule has an event occurring on the specified date.
     * <b>NOTE: this method is blind to the specific time of day on the date.
     * It only cares about the date's year and day-of-year</b>
     * <br/>
     * <p>If this schedule has a repeat-schedule, this method will also check the repeat-schedule to see
     * if there ever could be an event occurring on the specified date</p>
     *
     * @param date date to check
     * @return true if this schedule has an event occuring on the specified date, false otherwise
     */
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
     * Will check if this schedule has an event occurring within the next n days (where n === range)
     *
     * @param range exclusive of today i.e.) checks <br/><code>(today, today + range]</code>
     * @return true if this Schedule occurs within the range, false otherwise
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

    /**
     * @return number of days (ceiling) till the initial date of this schedule (0 if the initial date has passed)
     */
    public int getDaysDueIn() {
        Date today = new Date();
        if (!today.before(start))
            return 0;
        return (int) Math.ceil((start.getTime() - today.getTime()) / DAY_IN_MILLS);
    }

    /**
     * Listen to the changes of this schedule. This is VERY useful for updating views based on changes.
     * <br/>
     * <b>NOTE:</b>
     * <br/>
     * Change listeners remain attached till the end of this schedule object's lifetime.
     * Specifically, the listener will not detach until this object has been destroyed by the JVM garbage collector.
     * Be careful when adding change listeners so as not to create memory leaks.
     * To detach a change listener prematurely, call {@link #detachChangeListener(ChangeListener)}
     *
     * @param listener listener at attach
     */
    public void attachChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Detach a change listener from this schedule.
     *
     * @param listener listener to detach
     * @return true if the listener was successfully detached, false otherwise
     */
    public boolean detachChangeListener(ChangeListener listener) {
        return listeners.remove(listener);
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

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        //idk what this class is
        return asDoc;
    }

    public static Schedule fromBsonDocument(final Document doc){
        return new Schedule(
                //what the guy above said
        );
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
    public void fromJSON(JSONElement json, Consumer consumer) {
        start = new Date(json.<Long>valueOf("start"));
        end = new Date(json.<Long>valueOf("end"));
        endType = EndType.valueOf(json.valueOf("endType"));
        if (json.elementExists("repeatBetterSchedule"))
            repeatBetterSchedule = new Repeat().fromJSON(json.search("repeatBetterSchedule"));
        consumer.accept(this);
    }

    /**
     * ON_DATE = end by a specific date
     * <br/>
     * AFTER_TIMES = end after the event occurs n times
     */
    public enum EndType {
        ON_DATE, AFTER_TIMES
    }

    /**
     * ITERATIVE (i.e. every n days/weeks/months/years) vs RELATIVE (e.g. every 2nd & 4th Thursday of the month)
     */
    public enum RepeatType {
        ITERATIVE, RELATIVE
    }

    /**
     * How <i>often</i> should an event occur? daily/weekly/monthly/yearly?
     */
    public enum RepeatBasis {
        DAILY(0), WEEKLY(1), MONTHLY(2), YEARLY(3);

        int index;

        RepeatBasis(int index) {
            this.index = index;
        }

        /**
         * Used for console output
         *
         * @see Repeat#formatRepeatBasis() formatRepeatBasis
         * @see #consoleFormat(String) consoleFormat
         */
        public int getIndex() {
            return index;
        }
    }

    /**
     * To get a new Repeat schedule, call new RepeatSchedule().
     * <ul>
     * <li>{@link RepeatType}: ITERATIVE (i.e. every n days/weeks/months/years) vs RELATIVE (e.g. every 2nd & 4th Thursday of the month)</li>
     * <li>{@link RepeatBasis} DAILY/WEEKLY/MONTHLY/YEARLY</li>
     * <li>delay (the value of n in the above repeat-type example) (only applies to ITERATIVE repeat schedules</li>
     * <li>days, weeks, months (i.e. on which days-of-the-week/weeks-of-the-month/months-of-the-year should an event occur)</li>
     * </ul>
     * The repeat type defaults to ITERATIVE and repeat basis defaults to WEEKLY.
     *
     * @see Repeat#setDays(int...) Repeat.setDays(int...)
     * @see Repeat#setWeeks(int...) Repeat.setWeeks(int...)
     * @see Repeat#setMonths(int...) Repeat.setMonths(int...)
     */
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

        /**
         * @param type new repeat type
         * @throws InvalidCombinationException      for the illegal combination ({@link RepeatType#RELATIVE} and {@link RepeatBasis#DAILY})
         * @throws UnsupportedFunctionCallException if you attempt to change the type to {@link RepeatType#RELATIVE} before setting the days/weeks/months of recurrence first
         * @see Repeat#setDays(int...) Repeat.setDays(int...)
         * @see Repeat#setWeeks(int...) Repeat.setWeeks(int...)
         * @see Repeat#setMonths(int...) Repeat.setMonths(int...)
         */
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

        /**
         * @param repeatBasis new repeat basis
         * @throws InvalidCombinationException for the illegal combination ({@link RepeatType#RELATIVE} and {@link RepeatBasis#DAILY})
         */
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

        /**
         * Clear the days/weeks/months of recurrence
         */
        public void clearRelatives() {
            setDays();
            setWeeks();
            setMonths();
        }

        /**
         * @return true if there is at least one day of recurrence set, false otherwise
         */
        public boolean hasDays() {
            return days.length > 0;
        }

        /**
         * @return days of recurrence
         */
        public int[] getDays() {
            return days;
        }

        /**
         * Update the days of recurrence. Will check that the range is within [1, 7]
         */
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

        /**
         * @return true if there is at least one week of recurrence set, false otherwise
         */
        public boolean hasWeeks() {
            return weeks.length > 0;
        }

        /**
         * @return weeks of recurrence
         */
        int[] getWeeks() {
            return weeks;
        }

        /**
         * Update the weeks (of the month) of recurrence. Will check that the range is within [1, 4]
         */
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

        /**
         * @return true if there is at least one month of recurrence set, false otherwise
         */
        public boolean hasMonths() {
            return months.length > 0;
        }

        /**
         * @return months of recurrence
         */
        int[] getMonths() {
            return months;
        }

        /**
         * Update the months of recurrence. Will check that the range is within [1, 12]
         */
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

        /**
         * Will force the repeat-schedule to end after x number of occurrences (where x === times).
         * <br/>
         * <b>Note:</b>
         * <br/>
         * This will also update the repeat-schedule's end type to {@link EndType#AFTER_TIMES}
         */
        public void endAfterXTimes(int times) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            for (int i = 1; i <= times; i++) {
//                System.out.println("INTERVAL=" + getIntervalInMillis(calendar));
                calendar.add(Calendar.MILLISECOND, (int) getIntervalInMillis(calendar));
            }
            setEndType(EndType.AFTER_TIMES);
            setEnd(calendar.getTime());
        }

        /**
         * @return the maximum number of times an event can occur within this repeat-schedule
         */
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

    /**
     * Interface for listening to events and propagating them in views.
     * Each method will be called every time their respective event occurs.
     */
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
