package com.uniques.ourhouse.model;

import com.google.gson.JsonElement;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.simple.JSONArray;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User implements Observable, Indexable {
    private ObjectId userID;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    private String emailAddress;
    private List<ObjectId> myHouses = new ArrayList<>();

    public User(ObjectId userID, @NonNull String firstName, @NonNull String lastName, String emailAddress, List<ObjectId> myHouses) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.myHouses = myHouses;
    }

    public User(@NonNull String firstName, @NonNull String lastName, String emailAddress) {
        this.userID = new ObjectId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
    }

    public User() {
        firstName = "";
        lastName = "";
    }

    @NonNull
    @Override
    public ObjectId getId() {
        return userID;
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

    public List<ObjectId> getMyHouses() {
        return myHouses;
    }

    public void addHouse(ObjectId id) {
        Objects.requireNonNull(id);
        if (myHouses == null) {
            myHouses = new ArrayList<>();
            myHouses.add(id);
            return;
        }
        if (!myHouses.contains(id)) {
            myHouses.add(id);
        }
    }

    public void removeHouse(ObjectId id) {
        myHouses.remove(id);
    }

    public void setMyHouses(List<ObjectId> myHouses) {
        this.myHouses = myHouses;
    }

    @NonNull
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NonNull String firstName) {
        this.firstName = firstName;
    }

    @NonNull
    public String getLastName() {
        return lastName;
    }

    public void setLastName(@NonNull String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + "name: (" + firstName + " " + lastName + "), email: " + emailAddress + ", houses: " + myHouses;
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("_id", userID.toString());
        json.putPrimitive("userID", userID.toString());
        json.putPrimitive("fname", firstName);
        json.putPrimitive("lname", lastName);
        json.putPrimitive("email", emailAddress);
        json.putArray("houses");
        for (ObjectId houseId : myHouses) {
            json.search("houses").putPrimitive(houseId.toString());
        }
        // idk why this is necessary but hopefully this fixes the serialization issue
//        if (json.elementExists("house_id")) {
//            json.putPrimitive("house_id", json.valueOf("house_id").toString());
//        }
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        userID = new ObjectId(json.<String>valueOf("userID"));
        firstName = json.valueOf("fname");
        lastName = json.valueOf("lname");
        emailAddress = json.valueOf("email");
        myHouses = new ArrayList<>();
        for (JSONElement houseId : json.search("houses")) {
            if (houseId.getValue() instanceof JSONArray) {
                continue;
            }
            myHouses.add(new ObjectId(houseId.<String>getValue()));
        }
        consumer.accept(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof User) {
            return ((User) obj).userID.equals(userID);
        }
        return false;
    }

    public static ObjectId objectIdFromJSON(JsonElement obj) {
        return new ObjectId(obj.getAsString());
    }

    @NonNull
    @Override
    public String toString() {
        return consoleFormat("[User]");
    }
}
