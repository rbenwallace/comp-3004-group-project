package com.uniques.ourhouse.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.simple.JSONArray;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User implements Observable, Indexable {
    private ObjectId userID;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private List<ObjectId> myHouses = new ArrayList<>();
    private List<String> myHousesNames = new ArrayList<>();

    //testing
    private int performance;

    //testing int num
    public User(ObjectId userID, String firstName, String lastName, String emailAddress, List<ObjectId> myHouses, List<String> myHousesNames, int num) {
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

    public void addHouseId(ObjectId id) {
        Objects.requireNonNull(id);
        if (myHouses == null) {
            myHouses = new ArrayList<>();
            myHouses.add(id);
        } else if (!myHouses.contains(id)) {
            myHouses.add(id);
        }
    }

    public void removeHouseId(ObjectId id) {
        if (id != null) {
            if (myHouses.contains(id)) {
                myHouses.remove(id);
            }
        }
    }

    public List<ObjectId> getMyHouses() {
        return myHouses;
    }

    public void deleteHouse(House house) {
        myHouses.remove(house.getId());
        myHousesNames.remove(house.getKeyId());
    }

    public void addHouse(House house) {
        myHouses.add(house.getId());
        myHousesNames.add(house.getKeyId());
    }

    public void setMyHouses(List<ObjectId> myHouses) {
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
        return prefix + "name: (" + firstName + " " + lastName + ")";
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        Document housesDoc = new Document();
        for (int i = 0; i < myHouses.size() && i < myHousesNames.size(); i++) {
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

    public static User fromBsonDocument(final Document doc) {
        Document housesDoc = (Document) doc.get("houses");
        ArrayList<ObjectId> houses = new ArrayList<ObjectId>();
        ArrayList<String> housesNames = new ArrayList<String>();
        if (housesDoc != null) {
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
        json.putStructure("houses");
        for (int i = 0; i < myHouses.size(); i++) {
            json.search("houses")
                    .putPrimitive(myHousesNames.get(i), myHouses.get(i).toString());
        }
        // for (ObjectId houseId : myHouses) {
        //     json.search("houses").putPrimitive(houseId.toString());
        // }
        json.putPrimitive("performance", performance);
        // idk why this is necessary but hopefully this fixes the serialization issue
        if (json.elementExists("house_id")) {
            json.putPrimitive("house_id", json.valueOf("house_id").toString());
        }
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        userID = new ObjectId(json.<String>valueOf("userId"));
        firstName = json.valueOf("fname");
        lastName = json.valueOf("lname");
        emailAddress = json.valueOf("email");
        myHouses = new ArrayList<>();
        for (JSONElement houseId : json.search("houses")) {
            if (houseId.getValue() instanceof JSONArray) {
                continue;
            }
            myHouses.add(new ObjectId(houseId.<String>getValue()));
            myHousesNames.add(houseId.getKey());
        }
        performance = json.<Long>valueOf("performance").intValue();
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
        if (obj.get("houses") != null) {
            myHouses = obj.get("houses").getAsJsonObject();
            Set<String> keys = myHouses.keySet();
            Iterator<String> keyIt = keys.iterator();
            while (keyIt.hasNext()) {
                String key = keyIt.next();
                if (myHouses.get(key) != null) {
                    if (Session.getIdFromString(myHouses.get(key).toString()).length() > 5) {
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

    public static ObjectId objectIdFromJSON(JsonElement obj) {
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

    public void changeHouse(String keyId, House newHouse) {
        int i;
        for (i = 0; i < myHousesNames.size(); i++) {
            if (myHousesNames.get(i).equals(keyId)) {
                break;
            }
        }
        myHousesNames.set(i, newHouse.getKeyId());
        for (i = 0; i < myHouses.size(); i++) {
            if (myHouses.get(i).equals(newHouse.getId())) {
                break;
            }
        }
        myHouses.set(i, newHouse.getId());
    }
}
