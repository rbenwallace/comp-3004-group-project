package com.uniques.ourhouse.model;

import android.util.Log;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.google.gson.JsonObject;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.bson.Document;
import org.bson.types.ObjectId;

public class User implements Model, Observable, Indexable {
    public static final String USER_COLLECTION = "Users";
    private ObjectId userID;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;

    //testing
    private int performance;

    //testing int num
    public User(ObjectId userID, String firstName, String lastName, String emailAddress, int num) {
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
        //testing
        this.performance = num;
    }

    public User(String firstName, String lastName, String emailAddress) {
        this.userID = new ObjectId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        //testing
        this.performance = 0;
    }

    public User() {}

    @NonNull
    @Override
    public ObjectId getId() {
        return userID;
    }

    //testing
    public int getPerformance () { return performance; }

    @Override
    public String getName() {
        return firstName+" "+lastName;
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
        return prefix + firstName + lastName + " num: " + phoneNumber;
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        if(userID != null)
            asDoc.put("_id", userID);
        asDoc.put("firstName", firstName);
        asDoc.put("lastName", lastName);
        asDoc.put("email", emailAddress);
        asDoc.put("performance", performance);
        return asDoc;
    }

    public static User fromBsonDocument(final Document doc){
        return new User(
                (ObjectId) doc.get("_id"),
                doc.getString("firstName"),
                doc.getString("lastName"),
                doc.getString("email"),
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
        json.putPrimitive("performance", performance);
        return json.getRootNode();
    }

    @Override
    public User fromJSON(JSONElement json) {
        userID = (ObjectId) json.valueOf("userId");
        firstName = json.valueOf("fname");
        lastName = json.valueOf("lname");
        emailAddress = json.valueOf("email");
        performance = json.valueOf("performance");
        return this;
    }

    public static User fromJSON(JsonObject obj) {
        JsonObject id = obj.get("_id").getAsJsonObject();
        String myID = id.get("$oid").getAsString();
        String firstName = obj.get("firstName").getAsString();
        String lastName = obj.get("lastName").getAsString();
        String myEmail = obj.get("email").getAsString();
        Integer prefNum = obj.get("performance").getAsInt();
        User i = new User(new ObjectId(myID), firstName, lastName, myEmail, prefNum);
        return new User(new ObjectId(myID), firstName, lastName, myEmail, prefNum);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof User) {
            return ((User) obj).userID.equals(userID);
        }
        return false;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", performance=" + performance +
                '}';
    }
}
