package com.uniques.ourhouse.session;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchAuth;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteDeleteResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;
import com.uniques.ourhouse.Splash;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

public class MongoDB extends SecurityLink implements DatabaseLink {
    private static final String DATABASE = "ourHouseD";
    public static final String TAG = "MongoDB";
    private StitchAppClient client = Splash.client;
    private RemoteMongoClient mongoClient = client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
    private RemoteMongoCollection<Document> userColl = mongoClient.getDatabase(DATABASE).getCollection(User.USER_COLLECTION);
    private RemoteMongoCollection<Document> housesColl = mongoClient.getDatabase(DATABASE).getCollection(House.HOUSE_COLLECTION);
    private RemoteMongoCollection<Document> eventColl = mongoClient.getDatabase(DATABASE).getCollection(Event.EVENT_COLLECTION);
    private RemoteMongoCollection<Document> taskColl = mongoClient.getDatabase(DATABASE).getCollection(Task.TASK_COLLECTION);
    private RemoteMongoCollection<Document> feeColl = mongoClient.getDatabase(DATABASE).getCollection(Fee.FEE_COLLECTION);
    private Long count;

    //Stitch functions
    //-------------------------------------------------------------
    @Override
    public StitchAuth getAuth() {
        Log.d("whitepeopleshit", "OKOK");
        if(client != null){
            return client.getAuth();
        }
        return null;
    }
    @Override
    public SecureAuthenticator getSecureAuthenticator() {
        return null;
    }
    @Override
    public boolean isLoggedIn(ObjectId userId) {
        return Splash.client.getAuth().isLoggedIn();
    }
    @Override
    public void logout(Consumer<Boolean> consumer) {
        Splash.client.getAuth().logout().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Successfully logged out!");
                    consumer.accept(true);
                } else {
                    Log.e(TAG, "Logout failed!", task.getException());
                    consumer.accept(false);
                }
            }
        });
    }
    @Override
    public boolean autoAuth() {
        if(client.getAuth().getUser().getId() != null){
            return true;
        }
        return false;
    }
    @Override
    protected boolean autoAuthenticate(UUID id, UUID loginKey) {
        return autoAuth();
    }
    @Override
    public StitchUser getStitchUser() {
        if(client.getAuth().getUser() != null){
            return client.getAuth().getUser();
        }
        return null;
    }
    //-------------------------------------------------------------

    //Shared Pref - local user and house manipulation functions --> being moved to a different file || Tests ongoing as code changes
    //-------------------------------------------------------------
    @Override
    public ArrayList<House> getLocalHouseArray(FragmentActivity activity){
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("myHousesList", null);
            if(json == null){
                return null;
            }
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            ArrayList<String> myArray = gson.fromJson(json, type);
            if(myArray == null) return null;
            ArrayList<House> myHouses = new ArrayList<>();
            JsonObject obj;
            for(String s : myArray){
                obj = new JsonParser().parse(s).getAsJsonObject();
                myHouses.add(House.fromJSON(obj));
            }
            return myHouses;
        }
        catch (Error e){
            Log.d("User", "shared pref user not available");
            return null;
        }
    }
    @Override
    public User getCurrentLocalUser(FragmentActivity activity) {
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
            String json = sharedPreferences.getString("myUser", null);
            Log.d("checkingtings",  json.toString());
            if (json != null && !json.equals("null")) {
                JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
                Log.d("MongoDBbaby", obj.toString());
                return User.fromJSON(obj);
            } else return null;
        } catch (Error e) {
            Log.d("User", "shared pref user not available");
            return null;
        }
    }
    @Override
    public House getCurrentLocalHouse(FragmentActivity activity){
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
            String json = sharedPreferences.getString("myHouse", null);
            if(json != null && !json.equals("null")){
                JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
                Log.d("MongoDBbaby", obj.toString());
                return House.fromJSON(obj);
            }
            else
                return null;
        }
        catch (Error e){
            Log.d("User", "shared pref user not available");
            return null;
        }
    }
    @Override
    public void setLocalHouseArray(ArrayList<House> myList, FragmentActivity activity){
        Log.d("checkingHouses ADD", myList.toString());
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ArrayList<String> jsonHolders = new ArrayList<>();
        for(House h : myList){
            jsonHolders.add(h.toBsonDocument().toJson());
        }
        Gson gson = new Gson();
        String json = gson.toJson(jsonHolders);
        editor.putString("myHousesList", json);
        editor.apply();
        Log.d("checkingHouses ADD end", getLocalHouseArray(activity).toString());
    }
    @Override
    public void setLocalUser(User user, FragmentActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = user.toBsonDocument().toJson();
        Log.d("insideSetLocalUSer", json);
        editor.putString("myUser", json);
        editor.apply();
    }
    @Override
    public void setLocalHouse(House house, FragmentActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = house.toBsonDocument().toJson();
        editor.putString("myHouse", json);
        editor.apply();
        ArrayList<House> houses = getLocalHouseArray(activity);

    }
    @Override
    public void saveLocalHouse(House oldHouse, House newHouse, FragmentActivity activity) {
        ArrayList<House> houses = getLocalHouseArray(activity);
        houses.remove(oldHouse);
        houses.add(newHouse);
        User myUser = getCurrentLocalUser(activity);
        myUser.deleteHouse(oldHouse);
        myUser.addHouse(newHouse);
        updateUser(myUser, bool->{

        });
        setLocalHouse(newHouse, activity);
        setLocalHouseArray(houses, activity);
    }
    @Override
    public void clearLocalHouses(FragmentActivity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //IDK IF IT HAS TO BE A CLEAR OR NULL?
//        ArrayList<String> jsonHolders = new ArrayList<>();
        Gson gson = new Gson();
        String json = gson.toJson(null);
        editor.putString("myHousesList", json);
        editor.apply();
    }
    @Override
    public void clearLocalCurHouse(FragmentActivity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //IDK IF IT HAS TO BE A CLEAR OR NULL?
//        ArrayList<String> jsonHolders = new ArrayList<>();
        Gson gson = new Gson();
        String json = gson.toJson(null);
        editor.putString("myHouse", json);
        editor.apply();
    }
    @Override
    public void clearLocalCurUser(FragmentActivity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //IDK IF IT HAS TO BE A CLEAR OR NULL?
//        ArrayList<String> jsonHolders = new ArrayList<>();
        Gson gson = new Gson();
        String json = gson.toJson(null);
        editor.putString("myUser", json);
        editor.apply();
    }
    @Override
    public void clearLocalLoginData(FragmentActivity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //IDK IF IT HAS TO BE A CLEAR OR NULL?
//        ArrayList<String> jsonHolders = new ArrayList<>();
        Gson gson = new Gson();
        String json = gson.toJson(null);
        editor.putString("loginData", json);
        editor.apply();
    }
    //-------------------------------------------------------------

    //Database and Shared Pref - Mainly for me
    //-------
    @Override
    public void addMyUser(User user, FragmentActivity activity) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = userColl.insertOne(user.toBsonDocument());
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted item with id %s",
                            task.getResult().getInsertedId()));
                    Log.d("newUser", user.toString());
                    setLocalUser(user, activity);
                } else {
                    Log.e("app", "failed to insert document with: ", task.getException());
                }
            }
        });

    }
    @Override
    public void addMyHouse(House house, FragmentActivity activity, Consumer<Boolean> boolConsumer){
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = housesColl.insertOne(house.toBsonDocument());
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted item with id %s",
                            task.getResult().getInsertedId()));
                    User myUser = getCurrentLocalUser(activity);
                    myUser.addHouseId(house.getId());
                    myUser.addHouseName(house.getKeyId());
                    setLocalUser(myUser, activity);
                    setLocalHouse(house, activity);
                    ArrayList<House> curHouseList = getLocalHouseArray(activity);
                    curHouseList.add(house);
                    setLocalHouseArray(curHouseList, activity);
                    boolConsumer.accept(true);
                } else {
                    Log.e("app", "failed to insert document with: ", task.getException());
                    boolConsumer.accept(false);
                }
            }
        });

    }
    @Override
    public void findHousesByName(String name, Consumer<ArrayList<House>> consumer) {
        ArrayList<House> houses = new ArrayList<>();
        String pattern = "^"+name;
        BsonRegularExpression nameRE = new BsonRegularExpression(pattern);
        Document filterDoc = new Document()
                .append("key", new Document().append("$regex", nameRE));
        housesColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
                if(task.isSuccessful()){
                    final Long numDocs = task.getResult();
                    RemoteFindIterable findResults = housesColl
                            .find(filterDoc)
                            .limit(20);
                    count = 0L;
                    findResults.forEach(house -> {
                        Document myDoc = (Document) house;
                        House searchHouse = House.fromBsonDocument(myDoc);
                        houses.add(searchHouse);
                        count++;
                        if(count == numDocs)
                            consumer.accept(houses);
                    });
                }
                else{
                    consumer.accept(houses);
                }
            }
        });
    }
    //-------

    //Cloud functions
    //-------------------------------------------------------------
    //Get---
    @Override
    public void getUser(ObjectId id, Consumer<User> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = userColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    consumer.accept(null);
                } else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    consumer.accept(User.fromBsonDocument(task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                } else {
                    consumer.accept(null);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    } //tested
    @Override
    public void getEvent(ObjectId id, Consumer<Event> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = eventColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    consumer.accept(null);
                } else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    Event.FromBsonDocument(task.getResult(), consumer);
                    Log.d("stitch-auth", "Authentication Successful.");
                } else {
                    consumer.accept(null);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    } //tested
    @Override
    public void getTask(ObjectId id, Consumer<Task> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = taskColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    consumer.accept(null);
                } else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    consumer.accept(Task.fromBsonDocument(task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                } else {
                    consumer.accept(null);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    } //tested
    @Override
    public void getFee(ObjectId id, Consumer<Fee> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = feeColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    consumer.accept(null);
                } else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    consumer.accept(Fee.fromBsonDocument(task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                } else {
                    consumer.accept(null);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    } //tested
    @Override
    public void getHouse(ObjectId id, Consumer<House> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = housesColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    consumer.accept(null);
                } else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    consumer.accept(House.fromBsonDocument(task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                } else {
                    consumer.accept(null);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    } //tested
    @Override
    public void getHousesForHouseArray(ArrayList<ObjectId> userHouses, Consumer<ArrayList<House>> consumer){
        ArrayList<House> houses = new ArrayList<>();
        Document filterDoc = new Document()
                .append("_id", new Document().append("$in", userHouses));
        housesColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
                if(task.isSuccessful()){
                    final Long numDocs = task.getResult();
                    RemoteFindIterable findResults = housesColl
                            .find(filterDoc);
                    count = 0L;
                    findResults.forEach(house -> {
                        Document myDoc = (Document) house;
                        House searchHouse = House.fromBsonDocument(myDoc);
                        houses.add(searchHouse);
                        count++;
                        if(count == numDocs)
                            consumer.accept(houses);
                    });
                }
                else{
                    consumer.accept(houses);
                }
            }
        });
    }
    //All returns are in Decending order im tired rn so like if u want it opposite just change the -1 to a 1 in the .sort inside the functions
    @Override
    public void getAllEventsFromHouse(ObjectId houseId, Consumer<ArrayList<Event>> consumer){
        ArrayList<Event> events = new ArrayList<>();
        Document filterDoc = new Document()
                .append("assignedHouse", houseId);
        eventColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
                if(task.isSuccessful()){
                    final Long numDocs = task.getResult();
                    count = 0L;
                    RemoteFindIterable findResults = eventColl
                            .find(filterDoc).sort(new Document("dueDate", -1));
                    findResults.forEach(item -> {
                        Log.d("app", String.format("successfully found Events:  %s", item.toString()));
                        Document event = (Document) item;
                        Event.FromBsonDocument(event, rEvent ->{
                            events.add(rEvent);
                            count++;
                            if(count == numDocs)
                                consumer.accept(events);
                        });
                    });
                }
                else{
                    consumer.accept(events);
                }
            }
        });
    } //tested
    @Override
    public void getAllTasksFromHouse(ObjectId houseId, Consumer<ArrayList<Task>> consumer){
        ArrayList<Task> tasks = new ArrayList<>();
        Document filterDoc = new Document()
                .append("houseId", houseId);
        taskColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
                if(task.isSuccessful()){
                    final Long numDocs = task.getResult();
                    count = 0L;
                    RemoteFindIterable findResults = taskColl
                            .find(filterDoc).sort(new Document("dueDate", -1));
                    findResults.forEach(item -> {
                        Log.d("app", String.format("successfully found Task:  %s", item.toString()));
                        tasks.add(Task.fromBsonDocument((Document) item));
                        if(count == numDocs)
                            consumer.accept(tasks);
                    });
                }
                else{
                    consumer.accept(tasks);
                }
            }
        });
    } //tested
    @Override
    public void getAllFeesFromHouse(ObjectId houseId, Consumer<ArrayList<Fee>> consumer){
        ArrayList<Fee> fees = new ArrayList<>();
        Document filterDoc = new Document()
                .append("houseId", houseId);
        feeColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
                if(task.isSuccessful()){
                    final Long numDocs = task.getResult();
                    count = 0L;
                    RemoteFindIterable findResults = feeColl
                            .find(filterDoc).sort(new Document("dueDate", -1));
                    findResults.forEach(item -> {
                        Log.d("app", String.format("successfully found Fees:  %s", item.toString()));
                        fees.add(Fee.fromBsonDocument((Document) item));
                        if(count.equals(numDocs))
                            consumer.accept(fees);
                    });
                }
                else{
                    consumer.accept(fees);
                }
            }
        });
    } //tested
    @Override
    public void getAllEventsFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Event>> consumer){
        ArrayList<Event> events = new ArrayList<>();
        Document filterDoc = new Document()
                .append("assignedHouse", houseId);
        filterDoc.append("assignedTo", userId);
        eventColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
                if(task.isSuccessful()){
                    final Long numDocs = task.getResult();
                    count = 0L;
                    RemoteFindIterable findResults = eventColl
                            .find(filterDoc).sort(new Document("dueDate", -1));
                    findResults.forEach(item -> {
                        Log.d("app", String.format("successfully found Events:  %s", item.toString()));
                        Document event = (Document) item;
                        Event.FromBsonDocument(event, rEvent ->{
                            events.add(rEvent);
                            count++;
                            if(count == numDocs)
                                consumer.accept(events);
                        });
                    });
                }
                else{
                    consumer.accept(events);
                }
            }
        });
    } //tested
    @Override
    public void getAllTasksFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Task>> consumer){
        ArrayList<Task> tasks = new ArrayList<>();
        Document filterDoc = new Document()
                .append("houseId", houseId);
        filterDoc.append("userId", userId);
        taskColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
                if(task.isSuccessful()){
                    final Long numDocs = task.getResult();
                    count = 0L;
                    RemoteFindIterable findResults = taskColl
                            .find(filterDoc).sort(new Document("dueDate", -1));
                    findResults.forEach(item -> {
                        Log.d("app", String.format("successfully found Task:  %s", item.toString()));
                        tasks.add(Task.fromBsonDocument((Document) item));
                        if(count == numDocs)
                            consumer.accept(tasks);
                    });
                }
                else{
                    consumer.accept(tasks);
                }
            }
        });
    } //tested
    @Override
    public void getAllFeesFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Fee>> consumer){
        ArrayList<Fee> fees = new ArrayList<>();
        Document filterDoc = new Document()
                .append("houseId", houseId);
        filterDoc.append("userId", userId);
        feeColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
                if(task.isSuccessful()){
                    final Long numDocs = task.getResult();
                    count = 0L;
                    RemoteFindIterable findResults = feeColl
                            .find(filterDoc).sort(new Document("dueDate", -1));
                    findResults.forEach(item -> {
                        Log.d("app", String.format("successfully found Fees:  %s", item.toString()));
                        fees.add(Fee.fromBsonDocument((Document) item));
                        if(count.equals(numDocs))
                            consumer.accept(fees);
                    });
                }
                else{
                    consumer.accept(fees);
                }
            }
        });
    } //tested
    //Need to make, Ne
//    public void getAllEventsSince(ObjectId houseId, Date tillDate, Consumer<ArrayList<Task>> consumer){}
//    public void getAllTasksSince(ObjectId houseId, Date tillDate, Consumer<ArrayList<Task>> consumer){}
//    public void getAllFeesSince(ObjectId houseId, Date tillDate, Consumer<ArrayList<Task>> consumer){}
    //House Content Grabs
    //Post---
    @Override
    public void postUser(User user, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = userColl.insertOne(user.toBsonDocument());
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted user with id %s",
                            task.getResult().getInsertedId()));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to insert user with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void postEvent(Event event, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = eventColl.insertOne(event.toBsonDocument());
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted event with id %s",
                            task.getResult().getInsertedId()));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to insert event with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void postTask(Task post_task, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = taskColl.insertOne(post_task.toBsonDocument());
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted task with id %s",
                            task.getResult().getInsertedId()));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to insert task with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void postFee(Fee fee, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = feeColl.insertOne(fee.toBsonDocument());
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted fee with id %s",
                            task.getResult().getInsertedId()));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to insert fee with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void postHouse(House house, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = housesColl.insertOne(house.toBsonDocument());
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted house with id %s",
                            task.getResult().getInsertedId()));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to insert house with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    //Delete---
    @Override
    public void deleteAllEventsFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("assignedHouse", houseId);
        filterDoc.append("assignedTo", userId);
        Log.d("deleteAllEventsFromUserInHouse", "userId: "+ userId + " houseId: " + houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = eventColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteAllTasksFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("houseId", houseId);
        filterDoc.append("userId", userId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteAllFeesFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("houseId", houseId);
        filterDoc.append("userId", userId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = feeColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteAllEventsFromUser(ObjectId userId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("assignedTo", userId);
        Log.d("app", String.format("successfully deleted %s documents", userId.toString()));
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = eventColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteAllTasksFromUser(ObjectId userId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("userId", userId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteAllFeesFromUser(ObjectId userId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("userId", userId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = feeColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteAllEventsFromHouse(ObjectId houseId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("assignedHouse", houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = eventColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteAllTasksFromHouse(ObjectId houseId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("houseId", houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteAllFeesFromHouse(ObjectId houseId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("houseId", houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = feeColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested
    @Override
    public void deleteUser(User user, House userHouse, Consumer<Boolean> consumer){
        deleteAllEventsFromUser(user.getId(), bool->{
            if (!bool) Log.d("deleteAllEventsFromUser", "Failed");
        });
        deleteAllTasksFromUser(user.getId(), bool->{
            if (!bool) Log.d("deleteAllTasksFromUser", "Failed");
        });
        deleteAllFeesFromUser(user.getId(), bool->{
            if (!bool) Log.d("deleteAllFeesFromUser", "Failed");
        });
        deleteUserFromHouse(userHouse, user, bool->{
            if(!bool) Log.d("deleteAllFeesFromUser", "Failed");
        });
        Document filterDoc = new Document().append("_id", user.getId());
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = userColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    }
    @Override
    public void deleteEvent(ObjectId eventId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", eventId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = eventColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                }
            }
        });
    } //tested
    @Override
    public void deleteTask(ObjectId taskId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", taskId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                }
            }
        });
    } //tested
    @Override
    public void deleteFee(ObjectId feeId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", feeId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = feeColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                }
            }
        });
    } //tested
    @Override
    public void deleteHouse(ObjectId houseId, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = housesColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                }
            }
        });
    } //tested
    @Override
    public void deleteOwnerFromHouse(House house, User user, Consumer<Boolean> consumer){
        //Ensure not null
        if(house != null && user != null){
            //if there is nobody or just the owner, you can't delete the owner from delete Occupant, you must use deleteUserFromHouse
            if(house.getOccupants().size() == 1) {
                consumer.accept(false);
            }
            else {
                //Ensure Owner
                if(user.getId() != house.getOwner().getId()){
                    consumer.accept(false);
                    return;
                }
                deleteUserFromHouse(house, user, consumer);
            }
        }
        else {
            consumer.accept(false);
        }
    } //tested
    @Override
    public void deleteUserFromHouse(House house, User user, Consumer<Boolean> consumer){
        if(house == null || user == null) {
            consumer.accept(false);
            return;
        }
        //Log if user update fails
        Consumer<Boolean> booleanConsumer = bool ->{
            if(!bool){
                Log.d("deleteUserFromHouse: ", "Failed");
                //consumer.accept(false)
            }
        };
        deleteAllEventsFromUserInHouse(user.getId(), house.getId(), booleanConsumer);
        deleteAllTasksFromUserInHouse(user.getId(), house.getId(), booleanConsumer);
        deleteAllFeesFromUserInHouse(user.getId(), house.getId(), booleanConsumer);
        //Delete the house if its the only user
        if(house.getOccupants().size() <= 1){
            Log.d("HouseDeleted: ", "Verified");
            deleteHouse(house.getId(), booleanConsumer);
        }
        //Change the owner and remove house from user, update user
        else{
            if(house.getOwner().getId() == user.getId()){
                for (User user1 : house.getOccupants()){
                    if(user.getId() != user1.getId()){
                        house.setOwner(user1);
                        house.removeOccupant(user);
                        updateHouse(house, booleanConsumer);
                        break;
                    }
                }
            }
            else {
                house.removeOccupant(user);
                updateHouse(house, consumer);
            }
            user.removeHouseId(house.getId());
            updateUser(user, booleanConsumer);
        }
    } //tested
    //Update---
    //Update X
    @Override
    public void updateUser(User user, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", user.getId());
        Document updateDoc = user.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = userColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    Log.d("app", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to update document with: ", task.getException());
                    consumer.accept(true);
                }
            }
        });
    } //tested
    @Override
    public void updateFee(Fee fee, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", fee.getId());
        Document updateDoc = fee.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = feeColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    Log.d("app", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to update document with: ", task.getException());
                    consumer.accept(true);
                }
            }
        });
    } //tested
    @Override
    public void updateEvent(House event, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", event.getId());
        Document updateDoc = event.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = eventColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    Log.d("app", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to update document with: ", task.getException());
                    consumer.accept(true);
                }
            }
        });
    } //tested
    @Override
    public void updateHouse(House house, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", house.getId());
        Document updateDoc = house.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = housesColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    Log.d("app", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to update document with: ", task.getException());
                    consumer.accept(true);
                }
            }
        });
    } //tested
    @Override
    public void updateTask(Task task, Consumer<Boolean> consumer){
        Document filterDoc = new Document().append("_id", task.getId());
        Document updateDoc = task.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = taskColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    long numMatched = task.getResult().getMatchedCount();
                    long numModified = task.getResult().getModifiedCount();
                    Log.d("app", String.format("successfully matched %d and modified %d documents",
                            numMatched, numModified));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to update document with: ", task.getException());
                    consumer.accept(true);
                }
            }
        });
    } //tested
    @Override
    public void updateOwner(House house, User user, Consumer<Boolean> consumer){
        house.setOwner(user);
        updateHouse(house, consumer);
    } //tested
    //-------------------------------------------------------------

    //General functions needed for inserting into cloud
    //-------------------------------------------------------------
    @Override
    public void checkKey(String id, Consumer<Boolean> consumer){
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = housesColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    consumer.accept(true);
                } else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    consumer.accept(false);
                    Log.d("stitch-auth", "Authentication Successful.");
                } else {
                    consumer.accept(false);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    }
    @Override
    public Document getQueryForUser() {
        if(client.getAuth().getUser() == null) {
            return null;
        }
        StitchUser user = client.getAuth().getUser();
        Document query = new Document().append("_id", user.getId());
        return query;
    }
    //-------------------------------------------------------------
    //CLEAR ALL DATA
    public void deleteAllTEF(Consumer<Boolean> consumer){
        Document filterDoc = new Document();
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteFee = feeColl.deleteMany(filterDoc);
        deleteFee.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteEvent = eventColl.deleteMany(filterDoc);
        deleteEvent.addOnCompleteListener(new OnCompleteListener <RemoteDeleteResult> () {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteDeleteResult> task) {
                if (task.isSuccessful()) {
                    long numDeleted = task.getResult().getDeletedCount();
                    Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                    consumer.accept(true);
                } else {
                    Log.e("app", "failed to delete document with: ", task.getException());
                    consumer.accept(false);
                }
            }
        });
    } //tested

}
