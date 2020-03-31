package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.Schedule;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.types.ObjectId;

import java.util.function.Consumer;

import androidx.annotation.NonNull;

public class Task extends ManageItem implements Indexable, Observable {

    private String type;
    private int difficulty;

//    public static Task[] testTasks() {
////        Event r = new Event("Recycling Bin Day", new Date(now), new User("Ben", "", ""));
////        r.setDateCompleted(new Date(now + 76543210));
////        return new Event[]{
////                new Event("Dishes", new Date(now + 1000000), new User("Victor", "", "")),
////                r,
////                new Event("Buy us a TV", new Date(now + 2000000), new User("Seb", "", ""))};
//        Schedule.Repeat repeat;
//        Task r = new Task("Recycling", new Schedule(), DIFFICULTY_EASY);
//        repeat = r.schedule.initRepeatSchedule();
//        repeat.setType(Schedule.RepeatType.ITERATIVE);
//        repeat.setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
//        r.schedule.setEndPseudoIndefinite();
//
//        Task d = new Task("Dishes", new Schedule(), DIFFICULTY_MEDIUM);
//        repeat = d.schedule.initRepeatSchedule();
//        repeat.setType(Schedule.RepeatType.ITERATIVE);
//        repeat.setRepeatBasis(Schedule.RepeatBasis.DAILY);
//        d.schedule.setEndPseudoIndefinite();
//
//        Task t = new Task("Buy us a TV", new Schedule(), DIFFICULTY_HARD);
//        t.schedule.setEnd(new Date(new Date().getTime() + 176543210));
//        return new Task[]{r, d, t};
//    }

    public Task() {
    }

    public Task(ObjectId taskId, ObjectId owner, ObjectId house, String name, Schedule schedule, int difficulty) {
        super(taskId, owner, house, name, schedule);
        this.type = "Task";
        this.difficulty = difficulty;
    }

    public Task(ObjectId owner, ObjectId house, String name, Schedule schedule, int difficulty) {
        super(new ObjectId(), owner, house, name, schedule);
        this.type = "Task";
        this.difficulty = difficulty;
    }

    //USED FOR TESTING BECAUSE ITS EVERYWHERE BUT CHANGE YOUR FUNCTIONS TO USE THE ONES ABOVE SO I CAN DELETE THIS
    public Task(String name, Schedule schedule, int difficulty) {
        super(new ObjectId(), new ObjectId(), new ObjectId(), name, schedule);
        this.type = "Task";
        this.difficulty = difficulty;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public int getDifficulty() {
        return difficulty;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), " + "id: (" + manageItemOwner.toString() + "), " + "id: (" + manageItemHouse.toString() + "), name: [" + name + "] , Difficulty:" + difficulty + ", First Time Task is Due: " + schedule.getStart().toString();
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("_id", manageItemId.toString());
        json.putPrimitive("userId", manageItemOwner.toString());
        json.putPrimitive("houseId", manageItemHouse.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("type", type);
        json.putPrimitive("difficulty", String.valueOf(difficulty));
        json.putElement("schedule", schedule.toJSON());//FIX
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        manageItemId = new ObjectId(json.<String>valueOf("_id"));
        manageItemOwner = new ObjectId(json.<String>valueOf("userId"));
        manageItemHouse = new ObjectId(json.<String>valueOf("houseId"));
        name = json.valueOf("name");
        type = json.valueOf("type");
        difficulty = Integer.parseInt(json.valueOf("difficulty"));
        new Schedule().fromJSON(json.search("schedule"), schedule -> {
            this.schedule = (Schedule) schedule;
            consumer.accept(this);
        });
    }

    @NonNull
    @Override
    public String toString() {
        return consoleFormat("Task");
    }
}
