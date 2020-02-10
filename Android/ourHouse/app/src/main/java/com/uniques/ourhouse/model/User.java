package com.uniques.ourhouse.model;

<<<<<<< HEAD
import com.uniques.ourhouse.util.IndexableModel;
=======
import com.uniques.ourhouse.util.Indexable;
>>>>>>> master
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.UUID;

import androidx.annotation.NonNull;

<<<<<<< HEAD
public class User implements Observable, IndexableModel {
=======
public class User implements Observable, Indexable {
>>>>>>> master

    private UUID userID = UUID.randomUUID();
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;

    public User(String fullName, String emailAddress, String phoneNumber) {
        parseFullName(fullName);
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    public User() {}

<<<<<<< HEAD
=======
    /**
     * Same as {@link #getFancyName() getFancyName()}
     */
>>>>>>> master
    public String getFullName() {
        return (firstName + " " + lastName).trim();
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
        }
        if (name.contains(" ")) {
            firstName = name.substring(0, name.indexOf(' ')).trim();
            lastName = name.substring(name.indexOf(' ') + 1).trim();
        }
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
        return getFullName();
    }

    @NonNull
    @Override
    public UUID getId() {
        return userID;
    }

    @Override
    public String getName() {
        return getFullName();
    }

    @Override
    public void setName(String name) {
        parseFullName(name);
    }

<<<<<<< HEAD
=======
    /**
     * Same as {@link #getFullName() getFullName()}
     */
    @Override
    public String getFancyName() {
        return getFullName();
    }

    @Override
    public String getShortName() {
        return firstName;
    }

>>>>>>> master
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
}
