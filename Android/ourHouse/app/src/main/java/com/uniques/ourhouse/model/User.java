package com.uniques.ourhouse.model;

import android.util.Log;
import com.google.gson.JsonElement;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User implements Model, Observable, Indexable {
    public static final String USER_COLLECTION = "Users";
    private ObjectId userID;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private ArrayList<ObjectId> myHouses;

    //testing
    private int performance;

    //testing int num
    public User(ObjectId userID, String firstName, String lastName, String emailAddress, ArrayList<ObjectId> myHouses, int num) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        //testing
        this.performance = num;
    }

    //testing int num
    public User(String firstName, String lastName, String emailAddress, int num) {
        this.userID = new ObjectId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.myHouses = new ArrayList<>();
        //testing
        this.performance = num;
    }

    public User(String firstName, String lastName, String emailAddress) {
        this.userID = new ObjectId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.myHouses = new ArrayList<>();
        //testing
        this.performance = 0;
    }

    public User() {
    }

    @NonNull
    @Override
    public ObjectId getId() {
        return userID;
    }

    //testing
    public int getPerformance() {
        return performance;
    }

    @Override
    public String getName() {
        return firstName + " " + lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public int getCompareType() {
        return Observable.STRING;
    }

    @Override
    public Comparable getCompareObject() {
        return userID;
    }

    @Override
    public void setName(String name) {
    }

    public void addHouseId(ObjectId id){
        if(myHouses.contains(id)){
            myHouses.add(id);
        }
    }
    public void removeHouseId(ObjectId id){
        if(myHouses.contains(id)){
            myHouses.remove(id);
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + "name: (" + firstName + " " + lastName + "), num: (" + phoneNumber + ")";
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        if (userID != null)
            asDoc.put("_id", userID);
        asDoc.put("firstName", firstName);
        asDoc.put("lastName", lastName);
        asDoc.put("email", emailAddress);
        asDoc.put("performance", performance);
        return asDoc;
    }

    public static User fromBsonDocument(final Document doc){
        Document housesDoc = (Document)doc.get("houses");
        ArrayList<ObjectId> houses = new ArrayList<>();
        housesDoc.forEach((key, value)-> {
            houses.add((ObjectId)value);
        });

        return new User(
                (ObjectId) doc.get("_id"),
                doc.getString("firstName"),
                doc.getString("lastName"),
                doc.getString("email"),
                houses,
                doc.getInteger("performance")
        );
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("userId", userID.toString());
        json.putPrimitive("fname", firstName);
        json.putPrimitive("lname", lastName);
        json.putPrimitive("email", emailAddress);
        json.putArray("houses", myHouses);
        json.putPrimitive("performance", performance);
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        userID = new ObjectId(json.<String>valueOf("userId"));
        firstName = json.valueOf("fname");
        lastName = json.valueOf("lname");
        emailAddress = json.valueOf("email");
        myHouses = json.valueOf("houses");
        performance = json.valueOf("performance");
        consumer.accept(this);
    }

    public static User fromJSON(JsonObject obj) {
        JsonObject id = obj.get("_id").getAsJsonObject();
        String myID = id.get("$oid").getAsString();
        String firstName = obj.get("firstName").getAsString();
        String lastName = obj.get("lastName").getAsString();
        String myEmail = obj.get("email").getAsString();
        JsonObject myHouses = obj.get("houses").getAsJsonObject();
        Set<String> keys = myHouses.keySet();
        Iterator<String> keyIt = keys.iterator();
        ArrayList<ObjectId> houses = new ArrayList<ObjectId>();
        while(keyIt.hasNext()) {
            String key = keyIt.next();
            if (myHouses.get(key) != null) {
                houses.add(objectIdFromJSON(myHouses.get(key)));
            }
        }
        Integer prefNum = obj.get("performance").getAsInt();
        User i = new User(new ObjectId(myID), firstName, lastName, myEmail, houses, prefNum);
        return new User(new ObjectId(myID), firstName, lastName, myEmail, houses, prefNum);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof User) {
            return ((User) obj).userID.equals(userID);
        }
        return false;
    }

    public static ObjectId objectIdFromJSON(JsonElement obj){
        return new ObjectId(obj.getAsString());
    }

    @NonNull
    @Override
    public String toString() {
        return consoleFormat("[User] ");
    }
}
