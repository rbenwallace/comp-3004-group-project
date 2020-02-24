package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User implements Model, Observable, Indexable {

    private UUID userID = UUID.randomUUID();
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;

    //testing
    private int performance;

    //testing int num
    public User(String fullName, String emailAddress, String phoneNumber, int num) {
        parseFullName(fullName);
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        //testing
        this.performance = num;
    }

    public User() {}

    @NonNull
    @Override
    public UUID getId() {
        return userID;
    }

    //testing
    public int getPerformance () { return performance; }

    @Override
    public String getName() {
        return getFullName();
    }

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void parseFullName(String name) {
        if (name.contains(",")) {
            String[] parts = name.split(",");
            firstName = parts[0].trim();
            if (parts.length > 1) {
                lastName = (parts[0] + parts[1]).trim();
            } else {
                lastName = "";
            }
        } else if (name.contains(" ")) {
            firstName = name.substring(0, name.indexOf(' ')).trim();
            lastName = name.substring(name.indexOf(' ') + 1).trim();
        } else {
            firstName = name;
            lastName = "";
        }
    }

    @Override
    public int getCompareType() {
        return Observable.STRING;
    }

    @Override
    public Comparable getCompareObject() {
        return getFullName();
    }

    @Override
    public void setName(String name) {
        parseFullName(name);
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + firstName + lastName + " num: " + phoneNumber;
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("userId", userID.toString());
        json.putPrimitive("fname", firstName);
        json.putPrimitive("lname", lastName);
        json.putPrimitive("phoneNumber", phoneNumber);
        return json.getRootNode();
    }

    @Override
    public User fromJSON(JSONElement json) {
        userID = UUID.fromString(json.valueOf("userId"));
        firstName = json.valueOf("fname");
        lastName = json.valueOf("lname");
        phoneNumber = json.valueOf("phoneNumber");
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof User) {
            return ((User) obj).userID.equals(userID);
        }
        return false;
    }
}
