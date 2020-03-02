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

public class Fee extends ManageItem implements Model, Indexable, Observable {
    public static final String FEE_COLLECTION = "Fees";
    private float amount;
    private String type;

    public Fee(){}

    public Fee(ObjectId feeId, String name, float amount, Schedule schedule){
        super(name, schedule);
        manageItemId = feeId;
        this.amount = amount;
        this.type = "Fee";
    }

    public Fee(String name, float amount, Schedule schedule){
        super(name, schedule);
        this.amount = amount;
        this.type = "Fee";
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + ": " + type + ", id: (" + manageItemId.toString() + "), name: [" + name + "]   " + ", Amount: " + amount + ", Date Created: " + schedule.getStart().toString();
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("feeId", manageItemId.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("type", type);
        json.putPrimitive("amount", String.valueOf(amount));
        json.putElement("schedule", schedule.toJSON());
        return json.getRootNode();
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        asDoc.put("_id", manageItemId);
        asDoc.put("name", name);
        asDoc.put("amount", amount);
        asDoc.put("scheduel", schedule);
        return asDoc;
    }

    public static Fee fromBsonDocument(final Document doc){
        return new Fee(
                (ObjectId) doc.get("_id"),
                doc.getString("name"),
                (Float)doc.get("amount"),
                (Schedule) doc.get("scheduel")
        );
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        manageItemId = new ObjectId(json.<String>valueOf("feeId"));
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
