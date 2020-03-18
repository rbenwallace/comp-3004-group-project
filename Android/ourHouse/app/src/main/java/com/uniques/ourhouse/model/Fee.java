package com.uniques.ourhouse.model;

import android.renderscript.ScriptC;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.Schedule;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.function.Consumer;

public class Fee extends ManageItem implements Indexable, Observable {
    public static final String FEE_COLLECTION = "Fees";
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
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), " + manageItemOwner.toString() + "), " + "id: (" + manageItemHouse.toString() + "name: [" + name + "]   " + ", Amount: " + amount + ", Date Created: " + schedule.getStart().toString();
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("feeId", manageItemId.toString());
        json.putPrimitive("userId", manageItemOwner.toString());
        json.putPrimitive("houseId", manageItemHouse.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("type", type);
        json.putPrimitive("amount", String.valueOf(amount));
        json.putElement("schedule", schedule.toJSON());
        return json.getRootNode();
    }

    public Document toBsonDocument() {
        Document scheduleDoc = new Document();
        scheduleDoc.append("schedule", schedule.toBsonDocument());
        final Document asDoc = new Document();
        asDoc.put("_id", manageItemId);
        asDoc.put("userId", manageItemOwner);
        asDoc.put("houseId", manageItemHouse);
        asDoc.put("name", name);
        asDoc.put("amount", Float.toString(amount));
        asDoc.put("schedule", scheduleDoc);
        return asDoc;
    }

    public static Fee fromBsonDocument(final Document doc){
        Schedule schedule = new Schedule();
        schedule = schedule.fromBsonDocument((Document) Objects.requireNonNull(doc.get("schedule")));
        return new Fee(
                doc.getObjectId("_id"),
                doc.getObjectId("userId"),
                doc.getObjectId("houseId"),
                doc.getString("name"),
                Float.parseFloat(doc.getString("amount")),
                schedule
        );
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        manageItemId = new ObjectId(json.<String>valueOf("feeId"));
        manageItemId = new ObjectId(json.<String>valueOf("userId"));
        manageItemId = new ObjectId(json.<String>valueOf("houseId"));
        name = json.valueOf("name");
        type = json.valueOf("type");
        amount = json.valueOf("amount");
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
