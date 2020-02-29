package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Schedule;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.UUID;

import androidx.annotation.NonNull;

public class Fee implements Model, Indexable, Observable {

    private UUID feeId = UUID.randomUUID();
    private String name;
    private float amount;
    private Schedule schedule;

    @NonNull
    @Override
    public UUID getId() {
        return feeId;
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
        return STRING;
    }

    @Override
    public Comparable getCompareObject() {
        return name;
    }

    @Override
    public String consoleFormat(String prefix) {
        return "Fee (" + feeId.toString() + ") [" + name + "]";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("feeId", feeId.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("amount", String.valueOf(amount));
        json.putElement("schedule", schedule.toJSON());
        return json.getRootNode();
    }

    @Override
    public Fee fromJSON(JSONElement json) {
        feeId = UUID.fromString(json.valueOf("feeId"));
        name = json.valueOf("name");
        amount = Float.valueOf(json.valueOf("amount"));
        schedule = new Schedule().fromJSON(json.search("schedule"));
        return this;
    }
}
