package com.uniques.ourhouse.model;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uniques.ourhouse.session.MongoDB;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User implements Model, Observable, Indexable {
    public static final String USER_COLLECTION = "Users";
    private ObjectId userID;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private ArrayList<ObjectId> myHouses;
    private ArrayList<String> myHousesNames;

    //testing
    private int performance;

    //testing int num
    public User(ObjectId userID, String firstName, String lastName, String emailAddress, ArrayList<ObjectId> myHouses, ArrayList<String> myHousesNames, int num) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.myHouses = myHouses;
        //testing
        this.myHouses = myHouses;
        this.performance = num;
        this.myHousesNames = myHousesNames;
    }

    //testing int num
    public User(String firstName, String lastName, String emailAddress, int num) {
        this.userID = new ObjectId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        myHouses = new ArrayList<>();
        myHousesNames = new ArrayList<>();
        //testing
        this.performance = num;
    }

    public User(String firstName, String lastName, String emailAddress) {
        this.userID = new ObjectId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.myHouses = new ArrayList<>();
        myHousesNames = new ArrayList<>();
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
        if(id != null) {
            if(myHouses == null){
                myHouses = new ArrayList<ObjectId>();
                myHouses.add(id);
                return;
            }
            if (!myHouses.contains(id)) {
                for(ObjectId newID : myHouses)
                myHouses.add(id);

            }
        }
    }
    public void addHouseName(String name){
        if(name != null) {
            if(myHousesNames == null){
                myHousesNames = new ArrayList<String>();
                myHousesNames.add(name);
                return;
            }
            if (!myHousesNames.contains(name)) {
                for(String nameTemp : myHousesNames)
                myHousesNames.add(name);
            }
        }
    }
    public void removeHouseName(String name){
        if(name != null) {
            myHouses.remove(name);
        }
    }
    public void removeHouseId(ObjectId id){
        if(id != null) {
            if (myHouses.contains(id)) {
                myHouses.remove(id);
            }
        }
    }

    public ArrayList<ObjectId> getMyHouses() {
        return myHouses;
    }

    public void setMyHouses(ArrayList<ObjectId> myHouses) {
        this.myHouses = myHouses;
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
        return prefix + "name: (" + firstName + " " + lastName + "), num: (";
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        Document housesDoc = new Document();
        for(int i = 0; i < myHouses.size(); i++){
            housesDoc.append(myHousesNames.get(i), myHouses.get(i));//cant have multiple identities of the same name
        }
        asDoc.put("_id", userID);
        asDoc.put("firstName", firstName);
        asDoc.put("lastName", lastName);
        asDoc.put("email", emailAddress);
        asDoc.put("houses", housesDoc);
        asDoc.put("performance", performance);
        return asDoc;
    }

    public static User fromBsonDocument(final Document doc){
        Document housesDoc = (Document)doc.get("houses");
        ArrayList<ObjectId> houses = new ArrayList<ObjectId>();
        ArrayList<String> housesNames = new ArrayList<String>();
        if(housesDoc != null) {
            housesDoc.forEach((key, value) -> {
                housesNames.add(key);
                houses.add((ObjectId) value);
            });
        }
        return new User(
                (ObjectId) doc.get("_id"),
                doc.getString("firstName"),
                doc.getString("lastName"),
                doc.getString("email"),
                houses,
                housesNames,
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
        JsonObject myHouses;
        ArrayList<ObjectId> houses = new ArrayList<ObjectId>();
        ArrayList<String> housesNames = new ArrayList<String>();
        if(obj.get("houses") != null) {
            myHouses = obj.get("houses").getAsJsonObject();
            Set<String> keys = myHouses.keySet();
            Iterator<String> keyIt = keys.iterator();
            while (keyIt.hasNext()) {
                String key = keyIt.next();
                if (myHouses.get(key) != null) {
                    if(Session.getIdFromString(myHouses.get(key).toString()).length() > 5) {
                        housesNames.add(key);
                        houses.add(new ObjectId(Session.getIdFromString(myHouses.get(key).toString())));
                    }
                }
            }
        }
        int prefNum = obj.get("performance").getAsInt();
        return new User(new ObjectId(myID), firstName, lastName, myEmail, houses, housesNames, prefNum);
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


    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", myHouses=" + myHouses +
                ", performance=" + performance +
                '}';
    }
}
