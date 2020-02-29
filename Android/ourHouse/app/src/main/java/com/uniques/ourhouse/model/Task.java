package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Schedule;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.UUID;

public class Task extends ManageItem implements Model, Indexable, Observable {
    private String type;
    private int difficulty;
    //private UUID manageItemOwner;

    public Task(){}

    public Task(String name, Schedule schedule, int difficulty) {
        super(name, schedule);
        this.type = "Task";
        this.difficulty = difficulty;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), name: [" + name + "] , Difficulty:" + difficulty + ", First Time Task is Due: " + schedule.getStart().toString();
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
    public Task fromJSON(JSONElement json) {
        manageItemId = UUID.fromString(json.valueOf("manageItemId"));
        name = json.valueOf("name");
        type = json.valueOf("type");
        difficulty = Integer.valueOf(json.valueOf("difficulty"));
        schedule = new Schedule().fromJSON(json.search("schedule"));
        return null;
    }

    @Override
    public String getType(){
        return this.type;
    }
}
