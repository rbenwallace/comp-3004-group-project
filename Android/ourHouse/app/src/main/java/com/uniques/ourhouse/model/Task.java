package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.BetterSchedule;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.UUID;

public class Task extends ManageItem implements Model, Indexable, Observable {
    private String type;
    //private int difficulty;
    //private UUID manageItemOwner;

    public Task(){}

    public Task(String name, BetterSchedule schedule){
        super(name, schedule);
        this.type = "Task";
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), name: [" + name + "]   ";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("taskId", manageItemId.toString());
        json.putPrimitive("name", name);
        json.putElement("schedule", schedule.toJSON());
        return json.getRootNode();
    }

    @Override
    public Task fromJSON(JSONElement json) {
        manageItemId = UUID.fromString(json.valueOf("taskId"));
        name = json.valueOf("name");
        schedule = new BetterSchedule().fromJSON(json.search("schedule"));
        return null;
    }

    @Override
    public String getType(){
        return this.type;
    }
}
