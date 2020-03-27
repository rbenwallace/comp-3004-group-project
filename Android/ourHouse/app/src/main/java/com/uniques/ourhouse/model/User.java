package com.uniques.ourhouse.model;

import com.google.gson.JsonElement;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.simple.JSONArray;

import org.bson.Document;
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

    //testing
    private int performance;

    //testing int num
    public User(ObjectId userID, @NonNull String firstName, @NonNull String lastName, String emailAddress, List<ObjectId> myHouses, int num) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.myHouses = myHouses;
        this.performance = num;
    }

    //testing int num
    public User(@NonNull String firstName, @NonNull String lastName, String emailAddress, int num) {
        this.userID = new ObjectId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        //testing
        this.performance = num;
    }

    public User(@NonNull String firstName, @NonNull String lastName, String emailAddress) {
        this.userID = new ObjectId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        //testing
        this.performance = 0;
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
            return;
        }
        if (!myHouses.contains(id)) {
            myHouses.add(id);
        }
    }

    public void removeHouseId(ObjectId id) {
        if (id != null) {
            myHouses.remove(id);
        }
    }

    public List<ObjectId> getMyHouses() {
        return myHouses;
    }

    public void deleteHouse(House house) {
        myHouses.remove(house.getId());
    }

    public void addHouse(House house) {
        myHouses.add(house.getId());
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
        return prefix + "name: (" + firstName + " " + lastName + ")";
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        asDoc.put("_id", userID);
        asDoc.put("firstName", firstName);
        asDoc.put("lastName", lastName);
        asDoc.put("email", emailAddress);
        asDoc.put("houses", myHouses);
        asDoc.put("performance", performance);
        return asDoc;
    }

    public static User fromBsonDocument(final Document doc) {
        String firstName = doc.getString("firstName");
        String lastName = doc.getString("lastName");
        return new User(
                (ObjectId) doc.get("_id"),
                firstName == null ? "" : firstName,
                lastName == null ? "" : lastName,
                doc.getString("email"),
                doc.getList("houses", ObjectId.class),
                doc.getInteger("performance")
        );
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("userID", userID.toString());
        json.putPrimitive("fname", firstName);
        json.putPrimitive("lname", lastName);
        json.putPrimitive("email", emailAddress);
        json.putPrimitive("performance", performance);
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
        performance = json.<Long>valueOf("performance").intValue();
        consumer.accept(this);
    }

//    public static User fromJSON(JsonObject obj) {
//        JsonObject id = obj.get("_id").getAsJsonObject();
//        String myID = id.get("$oid").getAsString();
//        String firstName = obj.get("firstName").getAsString();
//        String lastName = obj.get("lastName").getAsString();
//        String myEmail = obj.get("email").getAsString();
//        JsonObject myHouses;
//        List<ObjectId> houses = new ArrayList<ObjectId>();
//        if (obj.get("houses") != null) {
//            myHouses = obj.get("houses").getAsJsonObject();
//            for (Map.Entry<String, JsonElement> entry : myHouses.entrySet()) {
//                houses.add(new ObjectId(entry.getValue().getAsString()));
//            }
//        }
//        int prefNum = obj.get("performance").getAsInt();
//        return new User(new ObjectId(myID), firstName, lastName, myEmail, houses, prefNum);
//    }

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
        for (i = 0; i < myHouses.size(); i++) {
            if (myHouses.get(i).equals(newHouse.getId())) {
                break;
            }
        }
        myHouses.set(i, newHouse.getId());
    }
}
