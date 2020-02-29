package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Schedule;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Task implements Model, Indexable, Observable {

    private UUID taskId = UUID.randomUUID();
    private String name;
    private Schedule schedule;

    @NonNull
    @Override
    public UUID getId() {
        return taskId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public Comparable getCompareObject() {
        return schedule;
    }

    @Override
    public String consoleFormat(String prefix) {
        return "Task (" + taskId.toString() + ") [" + name + "]";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("taskId", taskId.toString());
        json.putPrimitive("name", name);
        json.putElement("schedule", schedule.toJSON());
        return json.getRootNode();
    }

    @Override
    public Task fromJSON(JSONElement json) {
        taskId = UUID.fromString(json.valueOf("taskId"));
        name = json.valueOf("name");
        schedule = new Schedule().fromJSON(json.search("schedule"));
        return null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Task) {
            return ((Task) obj).taskId.equals(taskId);
        }
        return false;
    }
}
