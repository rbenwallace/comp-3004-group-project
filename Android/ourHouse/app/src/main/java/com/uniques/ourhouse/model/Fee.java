package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.Schedule;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import org.bson.types.ObjectId;
import java.util.function.Consumer;
import androidx.annotation.NonNull;

public class Fee extends ManageItem implements Indexable, Observable {
    private float amount;
    private String type;

    public Fee(){}
    //Incoming Fee's
    public Fee(ObjectId feeId, ObjectId owner, ObjectId houseId, String name, float amount, Schedule schedule){
        super(feeId, owner, houseId, name, schedule);
        this.amount = amount;
        this.type = "Fee";
    }
    //Creating Fee's
    public Fee(ObjectId owner, ObjectId houseId, String name, float amount, Schedule schedule){
        super(new ObjectId(), owner, houseId, name, schedule);
        this.amount = amount;
        this.type = "Fee";
    }
    //USED FOR TESTING BECAUSE ITS EVERYWHERE BUT CHANGE YOUR FUNCTIONS TO USE THE ONES ABOVE SO I CAN DELETE THIS
    public Fee(String name, float amount, Schedule schedule){
        super(new ObjectId(), new ObjectId(), new ObjectId(), name, schedule);
        this.amount = amount;
        this.type = "Fee";
    }

    public float getAmount(){ return amount; }

    @Override
    public int getDifficulty() {
        if (amount > 500) {
            return ManageItem.DIFFICULTY_HARD;
        } else if (amount >= 100) {
            return ManageItem.DIFFICULTY_MEDIUM;
        }
        return ManageItem.DIFFICULTY_EASY;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), " + manageItemOwner.toString() + "), " + "id: (" + manageItemHouse.toString() + "name: [" + name + "]   " + ", Amount: " + amount + ", Date Created: " + schedule.getStart().toString();
    }

    @NonNull
    @Override
    public String toString() {
        return "";
        //return consoleFormat("Fee");
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("_id", manageItemId.toString());
        json.putPrimitive("feeId", manageItemId.toString());
        json.putPrimitive("userId", manageItemOwner.toString());
        json.putPrimitive("houseId", manageItemHouse.toString());
        json.putPrimitive("serialVersionId", serialVersionId);
        json.putPrimitive("name", name);
        json.putPrimitive("type", type);
        json.putPrimitive("amount", String.valueOf(amount));
        json.putElement("schedule", schedule.toJSON());
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        manageItemId = new ObjectId(json.<String>valueOf("feeId"));
        manageItemId = new ObjectId(json.<String>valueOf("userId"));
        manageItemId = new ObjectId(json.<String>valueOf("houseId"));
        if (json.elementExists("serialVersionId")) {
            serialVersionId = json.valueOf("serialVersionId");
        }
        name = json.valueOf("name");
        type = json.valueOf("type");
        amount = Float.parseFloat(json.valueOf("amount"));
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
