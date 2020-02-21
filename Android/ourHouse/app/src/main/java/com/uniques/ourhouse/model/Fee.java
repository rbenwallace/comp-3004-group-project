package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.BetterSchedule;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.UUID;

public class Fee extends ManageItem implements Model, Indexable, Observable {
    private float amount;
    private String type;

    public Fee(){}

    public Fee(String name, float amount, BetterSchedule schedule){
        super(name, schedule);
        this.amount = amount;
        this.type = "Fee";
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), name: [" + name + "]   " + ", Amount: " + amount;
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("feeId", manageItemId.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("amount", String.valueOf(amount));
        json.putElement("schedule", schedule.toJSON());
        return json.getRootNode();
    }

    @Override
    public Fee fromJSON(JSONElement json) {
        manageItemId = UUID.fromString(json.valueOf("feeId"));
        name = json.valueOf("name");
        amount = Float.valueOf(json.valueOf("amount"));
        schedule = new BetterSchedule().fromJSON(json.search("schedule"));
        return this;
    }

    @Override
    public String getType(){
        return this.type;
    }
}
