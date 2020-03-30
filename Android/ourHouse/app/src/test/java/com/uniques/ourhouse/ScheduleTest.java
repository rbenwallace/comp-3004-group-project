package com.uniques.ourhouse;

import com.uniques.ourhouse.util.Schedule;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ScheduleTest {
    @Test
    public void startSetCorrectly() {
        Schedule schedule = new Schedule();
        Date start = new Date(System.currentTimeMillis() + 234567);
        schedule.pauseStartEndBoundsChecking();
        schedule.setStart(start);
        assertEquals(start.getTime(), schedule.getStart().getTime());
        System.out.println("Passed startSetCorrectly");
    }

    @Test
    public void endSetCorrectly() {
        Schedule schedule = new Schedule();
        Date end = new Date(System.currentTimeMillis() + 324576);
        schedule.setEnd(end);
        assertEquals(end.getTime(), schedule.getEnd().getTime());
        System.out.println("Passed endSetCorrectly");
    }

    @Test
    public void endTypeSetCorrectly() {
        Schedule schedule = new Schedule();
        assertEquals(schedule.getEndType(), Schedule.EndType.ON_DATE);
        schedule.setEndType(Schedule.EndType.AFTER_TIMES);
        assertEquals(schedule.getEndType(), Schedule.EndType.AFTER_TIMES);
        System.out.println("Passed endTypeSetCorrectly");
    }

    @Test
    public void initRepeatWorks() {
        Schedule schedule = new Schedule();
        schedule.initRepeatSchedule();
        assertNotNull(schedule.getRepeatSchedule());
        System.out.println("Passed initRepeatWorks");
    }

    @Test
    public void repeatWorks() {
        Schedule schedule = new Schedule();
        Schedule.Repeat repeat = schedule.initRepeatSchedule();

        repeat.setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
        assertEquals(repeat.getRepeatBasis(), Schedule.RepeatBasis.WEEKLY);

        repeat.setWeeks(2, 4);
        assertArrayEquals(repeat.getDays(), new int[0]);
        assertArrayEquals(repeat.getWeeks(), new int[]{2, 4});
        assertArrayEquals(repeat.getMonths(), new int[0]);

        repeat.setType(Schedule.RepeatType.RELATIVE);
        assertEquals(repeat.getType(), Schedule.RepeatType.RELATIVE);

        repeat.setEndAfterXTimes(5);
        assertEquals(schedule.getEndType(), Schedule.EndType.AFTER_TIMES);
        System.out.println("repeatEndDate " + schedule.getEnd());
        System.out.println("Passed repeatWorks");
    }

    @Test
    public void constructorWorks() {
        Schedule template = new Schedule();
        template.initRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);

        Date start = new Date(System.currentTimeMillis() + 234567);
        Date end = new Date(System.currentTimeMillis() + 324576);

        Schedule schedule = new Schedule(start, end, template.getRepeatSchedule());

        assertEquals(schedule.getStart(), start);
        assertEquals(schedule.getEnd(), end);
        assertNotNull(schedule.getRepeatSchedule());
        assertEquals(schedule.getRepeatSchedule().getRepeatBasis(), template.getRepeatSchedule().getRepeatBasis());
        System.out.println("Passed constructorWorks");
    }

    @Test
    public void iteratorWorks() {
        Schedule schedule = new Schedule();
        Schedule.Repeat repeat = schedule.initRepeatSchedule();
        repeat.setRepeatBasis(Schedule.RepeatBasis.DAILY);
        repeat.setDelay(2);
        repeat.setEndAfterXTimes(3);

        Long previousOccurrence = null;
        long twoDays = 2 * 24 * 60 * 60 * 1000;
        int numOccurrences = 0;

        for (Date occurrence : schedule) {
            numOccurrences++;
            if (previousOccurrence == null) {
                assertEquals(occurrence, schedule.getStart());
            } else {
                assertEquals(Math.abs(occurrence.getTime() - previousOccurrence), twoDays);
            }
            previousOccurrence = occurrence.getTime();
        }

        assertEquals(numOccurrences, 3);
        System.out.println("Passed iteratorWorks");
    }

    @Test
    public void finiteIteratorWorks() {
        Schedule schedule = new Schedule();
        Schedule.Repeat repeat = schedule.initRepeatSchedule();
        repeat.setRepeatBasis(Schedule.RepeatBasis.DAILY);
        repeat.setDelay(1);
        repeat.setEndPseudoIndefinite();

        long oneDay = 24 * 60 * 60 * 1000;
        Date lowerBound = schedule.getStart();
        Date upperBound = new Date(lowerBound.getTime() + 2 * oneDay);
        Long previousOccurrence = null;
        int numOccurrences = 0;

        for (Date occurrence : schedule.finiteIterable(lowerBound, upperBound)) {
            numOccurrences++;
            if (previousOccurrence == null) {
                assertEquals(occurrence, schedule.getStart());
            } else {
                assertEquals(Math.abs(occurrence.getTime() - previousOccurrence), oneDay);
            }
            previousOccurrence = occurrence.getTime();
        }

        assertEquals(numOccurrences, 3);
        System.out.println("Passed finiteIteratorWorks");
    }

    @Test
    public void occursChecksWork() {
        long oneDay = 24 * 60 * 60 * 1000;

        Schedule schedule = new Schedule();
        Date start = new Date(System.currentTimeMillis() + 2 * oneDay);
        schedule.pauseStartEndBoundsChecking();
        schedule.setStart(start);
        Schedule.Repeat repeat = schedule.initRepeatSchedule();
        repeat.setRepeatBasis(Schedule.RepeatBasis.DAILY);
        repeat.setDelay(3);
        repeat.setEndAfterXTimes(3);

        assertFalse(schedule.occursOn(new Date()));
        assertTrue(schedule.occursOn(new Date(System.currentTimeMillis() + 8 * oneDay)));
        assertFalse(schedule.occursInXDays(1));
        assertTrue(schedule.occursInXDays(3));

        assertEquals(schedule.getDaysTillStart(), 2);
        System.out.println("Passed occursChecksWork");
    }

    @Test
    public void changeListenerWorks() {
        boolean[] detections = new boolean[8];
        detections[detections.length - 1] = true;

        Schedule.ChangeListener changeListener = new Schedule.ChangeListener() {
            @Override
            public void onStartChange(Date newStart) {
                detections[0] = true;
                if (!detections[detections.length - 1])
                    detections[detections.length - 1] = true;
            }

            @Override
            public void onEndChange(Date newEnd) {
                detections[1] = true;
            }

            @Override
            public void onEndTypeChange(Schedule.EndType newEndType) {
                detections[2] = true;
            }

            @Override
            public void onRepeatToggled(boolean enabled) {
                detections[3] = true;
            }

            @Override
            public void onRepeatTypeChange(Schedule.RepeatType newType) {
                detections[4] = true;
            }

            @Override
            public void onRepeatBasisChange(Schedule.RepeatBasis newBasis) {
                detections[5] = true;
            }

            @Override
            public void onRepeatDelayChange(int newDelay) {
                detections[6] = true;
            }
        };

        Schedule schedule = new Schedule();
        schedule.attachChangeListener(changeListener);

        schedule.pauseStartEndBoundsChecking();
        schedule.setStart(new Date());
        assertTrue(detections[0]);
        schedule.setEndPseudoIndefinite();
        assertTrue(detections[1]);
        assertTrue(detections[2]);
        assertTrue(detections[3]);
        assertTrue(detections[4]);
        assertTrue(detections[5]);
        assertTrue(detections[6]);

        detections[detections.length - 1] = false;
        schedule.detachChangeListener(changeListener);
        schedule.pauseStartEndBoundsChecking();
        schedule.setStart(new Date());
        assertFalse(detections[detections.length - 1]);
        System.out.println("Passed changeListenerWorks");
    }

    @Test
    public void bsonWorks() {
        Schedule template = new Schedule();
        template.initRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);

        Date start = new Date(System.currentTimeMillis() + 234567);
        Date end = new Date(System.currentTimeMillis() + 324576);

        Schedule orgSchedule = new Schedule(start, end, template.getRepeatSchedule());
        Schedule reLoadedSchedule = new Schedule().fromBsonDocument(orgSchedule.toBsonDocument());

        assertEquals(reLoadedSchedule.getStart(), start);
        assertEquals(reLoadedSchedule.getEnd(), end);
        assertNotNull(reLoadedSchedule.getRepeatSchedule());
        assertEquals(reLoadedSchedule.getRepeatSchedule().getRepeatBasis(), template.getRepeatSchedule().getRepeatBasis());
        System.out.println("Passed bsonWorks");
    }

    @Test
    public void jsonWorks() {
        Schedule template = new Schedule();
        template.initRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);

        Date start = new Date(System.currentTimeMillis() + 234567);
        Date end = new Date(System.currentTimeMillis() + 324576);

        Schedule orgSchedule = new Schedule(start, end, template.getRepeatSchedule());
        Schedule reLoadedSchedule = new Schedule();
        reLoadedSchedule.fromJSON(orgSchedule.toJSON(), s -> {
        });

        assertEquals(reLoadedSchedule.getStart(), start);
        assertEquals(reLoadedSchedule.getEnd(), end);
        assertNotNull(reLoadedSchedule.getRepeatSchedule());
        assertEquals(reLoadedSchedule.getRepeatSchedule().getRepeatBasis(), template.getRepeatSchedule().getRepeatBasis());
        System.out.println("Passed jsonWorks");
    }
}