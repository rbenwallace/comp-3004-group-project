package com.uniques.ourhouse.model;

import android.util.Log;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.Schedule;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.function.Consumer;

public class Task extends ManageItem implements Model, Indexable, Observable {
    public static final String TASK_COLLECTION = "Tasks";
    private String type;
    private int difficulty;

    public Task(){}

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
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), " + "id: (" + manageItemOwner.toString() + "), " + "id: (" + manageItemHouse.toString() + "), name: [" + name + "] , Difficulty:" + difficulty + ", First Time Task is Due: " + schedule.getStart().toString();
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        asDoc.put("_id", manageItemId);
        asDoc.put("userId", manageItemOwner);
        asDoc.put("houseId", manageItemHouse);
        asDoc.put("name", name);
        asDoc.put("scheduel", schedule.toBsonDocument());//Fix
        asDoc.put("difficulty", difficulty);
        return asDoc;
    }

    public static Task fromBsonDocument(final Document doc){
        Schedule schedule = new Schedule();
        Log.d("checking", doc.get("scheduel").toString());
        schedule = schedule.fromBsonDocument((Document) Objects.requireNonNull(doc.get("scheduel")));
        return new Task(
                doc.getObjectId("_id"),
                doc.getObjectId("userId"),
                doc.getObjectId("houseId"),
                doc.getString("name"),
                schedule,
                doc.getInteger("difficulty")
        );
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
        difficulty = json.valueOf("difficulty");
        new Schedule().fromJSON(json.search("schedule"), schedule -> {
            this.schedule = (Schedule) schedule;
            consumer.accept(this);
        });
    }

    @Override
    public String getType(){
        return this.type;
    }
}
