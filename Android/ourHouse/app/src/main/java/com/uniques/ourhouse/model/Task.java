package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.Schedule;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.function.Consumer;

public class Task extends ManageItem implements Model, Indexable, Observable {
    public static final String TASK_COLLECTION = "Tasks";

    private String type;
    private int difficulty;
    private ObjectId manageItemOwner;

    public Task(){}

    public Task(ObjectId taskId, String name, Schedule schedule, int difficulty) {
        super(name, schedule);
        this.type = "Task";
        this.difficulty = difficulty;
    }

    public Task(String name, Schedule schedule, int difficulty) {
        super(name, schedule);
        this.type = "Task";
        this.difficulty = difficulty;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), name: [" + name + "] , Difficulty:" + difficulty + ", First Time Task is Due: " + schedule.getStart().toString();
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        if(manageItemId != null)
            asDoc.put("_id", super.manageItemId);
        asDoc.put("name", name);
        asDoc.put("scheduel", schedule);
        asDoc.put("difficulty", difficulty);
        return asDoc;
    }

    public static Task fromBsonDocument(final Document doc){
        return new Task(
                (ObjectId) doc.get("_id"),
                doc.getString("name"),
                (Schedule) doc.get("scheduel"),
                (Integer) doc.get("difficulty")
        );
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("manageItemId", manageItemId.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("type", type);
        json.putPrimitive("difficulty", String.valueOf(difficulty));
        json.putElement("schedule", schedule.toJSON());
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        manageItemId = (ObjectId) json.valueOf("manageItemId");
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
