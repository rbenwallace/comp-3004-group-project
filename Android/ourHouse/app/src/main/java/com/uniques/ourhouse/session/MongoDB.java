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
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.uniques.ourhouse.Splash;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

public class MongoDB implements DatabaseLink {
    private static final String DATABASE = "ourHouseD";
    public static final String TAG = "MongoDB";
    private StitchAppClient client = Splash.client;
    private RemoteMongoClient mongoClient = client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
    private RemoteMongoCollection<Document> userColl = mongoClient.getDatabase(DATABASE).getCollection(User.USER_COLLECTION);
    private RemoteMongoCollection<Document> housesColl = mongoClient.getDatabase(DATABASE).getCollection(House.HOUSE_COLLECTION);
    private RemoteMongoCollection<Document> eventColl = mongoClient.getDatabase(DATABASE).getCollection(Event.EVENT_COLLECTION);
    private RemoteMongoCollection<Document> taskColl = mongoClient.getDatabase(DATABASE).getCollection(Task.TASK_COLLECTION);
    private RemoteMongoCollection<Document> feeColl = mongoClient.getDatabase(DATABASE).getCollection(Fee.FEE_COLLECTION);


    public StitchAuth getAuth() {
        Log.d("whitepeopleshit", "OKOK");
        if(client.getAuth() != null){
            return client.getAuth();
        }
        return null;
    }

    public boolean autoAuth() {
        Log.d("whitepeopleshit", "OKOK");
        if(client.getAuth().getUser() != null){
            Log.d("whitepeopleshit", client.getAuth().getUser().toString());
            return true;
        }
        return false;
    }
    public StitchUser getStitchUser() {
        Log.d("whitepeopleshit", "OKOK");
        if(client.getAuth().getUser() != null){
            Log.d("whitepeopleshit", client.getAuth().getUser().toString());
            return client.getAuth().getUser();
        }
        return null;
    }
    public Document getQueryForUser() {
        Log.d("brownpeopleshit", "top");
        if(client.getAuth().getUser() == null) {
            return null;
        }
        Log.d("whitepeopleshit", client.getAuth().getUser().toString());
        Log.d("brownpeopleshit", "middle");
        StitchUser user = client.getAuth().getUser();
        Document query = new Document().append("_id", user.getId());
        Log.d("brownpeopleshit", "Bottom");
        return query;
    }
    public Document getCurrentUserCustomData() {
        Log.d("whitepeopleshit", "OKOK");
        if(client.getAuth().getUser().getCustomData() != null){
            Log.d("ls", client.getAuth().getUser().toString());
            return client.getAuth().getUser().getCustomData();
        }
        return null;
    }

    public ArrayList<House> getLocalHouseArray(FragmentActivity activity){
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("myHousesList", null);
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            ArrayList<String> myArray = gson.fromJson(json, type);
            Log.d("checkingHouses GET", myArray.toString());
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

    public void updateLocalHouseArray(ArrayList<House> myList, FragmentActivity activity){
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
        Log.d("checkingHouses ADD end", myList.toString());
    }

    //Local user and house manipulation through shared preferences
    public static User getCurrentLocalUser(FragmentActivity activity){
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
            String json = sharedPreferences.getString("myUser", null);
            if(json != null) {
                JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
                Log.d("MongoDBbaby", obj.toString());
                return User.fromJSON(obj);
            }
            else return null;
        }
        catch (Error e){
            Log.d("User", "shared pref user not available");
            return null;
        }
    }

    public static House getCurrentLocalHouse(FragmentActivity activity){
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
            String json = sharedPreferences.getString("myHouse", null);
            if(json != null) {
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

    public void addMyUser(User user, FragmentActivity activity) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = userColl.insertOne(user.toBsonDocument());
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted item with id %s",
                            task.getResult().getInsertedId()));
                    setLocalUser(user, activity);
                } else {
                    Log.e("app", "failed to insert document with: ", task.getException());
                }
            }
        });

    }

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
                    setLocalUser(myUser, activity);
                    setLocalHouse(house, activity);
                    boolConsumer.accept(true);
                } else {
                    Log.e("app", "failed to insert document with: ", task.getException());
                    boolConsumer.accept(false);
                }
            }
        });

    }

    public void setLocalUser(User user, FragmentActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = user.toBsonDocument().toJson();
        editor.putString("myUser", json);
        editor.apply();
    }

    public void setLocalHouse(House house, FragmentActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = house.toBsonDocument().toJson();
        editor.putString("myHouse", json);
        editor.apply();
    }

    public void getMultipleHouses(String name, Consumer<ArrayList<House>> consumer) {
        ArrayList<House> houses = new ArrayList<>();
        String pattern = "^"+name;
        BsonRegularExpression nameRE = new BsonRegularExpression(pattern);
        Document filterDoc = new Document()
                .append("key", new Document().append("$regex", nameRE));
        RemoteFindIterable findResults = housesColl
                .find(filterDoc)
                .limit(20)
                .sort(new Document().append("name", 1));
        Log.d("MongoDB", filterDoc.toString());
        findResults.forEach(house -> {
            Log.d("app", String.format("successfully found:  %s", house.toString()));
            houses.add(House.fromBsonDocument((Document)house));
        });
        consumer.accept(houses);
    }

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
    }

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
                    consumer.accept(Event.fromBsonDocument(task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                } else {
                    consumer.accept(null);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    }

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
    }

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
    }

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
    }

    @Override
    public void postUser(User user, Consumer<Boolean> consumer) {
        Document toUploadDoc = user.toBsonDocument();
        Document query = new Document().append("_id", user.getId());
        //Check to see if user already inside
        final com.google.android.gms.tasks.Task<Document> findOne = userColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    //
                    final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = userColl.insertOne(user.toBsonDocument());
                    insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("app", String.format("successfully inserted item with id %s",
                                        task.getResult().getInsertedId()));
                                consumer.accept(true);
                            } else {
                                Log.e("app", "failed to insert document with: ", task.getException());
                                consumer.accept(false);
                            }
                        }
                    });
                }
                else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                    final com.google.android.gms.tasks.Task<Document> findOneAndReplace = userColl.findOneAndReplace(query, toUploadDoc);
                    findOneAndReplace.addOnCompleteListener(new OnCompleteListener<Document>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                            if (task.getResult() == null) {
                                Log.d("app", String.format("No document matches the provided query"));
                                Log.d("stitch-auth", "Authentication Successful.");
                                consumer.accept(false);
                            } else if (task.isSuccessful()) {
                                Log.d("app", String.format("Successfully found document: %s",
                                        task.getResult()));
                                consumer.accept(true);
                                Log.d("stitch-auth", "Authentication Successful.");
                            } else {
                                consumer.accept(false);
                                Log.e("app", "Failed to findOne: ", task.getException());
                                Log.d("stitch-auth", "Authentication Successful.");
                            }
                        }
                    });

                } else {
                    consumer.accept(false);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    }
    @Override
    public void postEvent(Event event, Consumer<Boolean> consumer) {
        Document toUploadDoc = event.toBsonDocument();
        Document query = new Document().append("_id", event.getId());
        //Check to see if user already inside
        final com.google.android.gms.tasks.Task<Document> findOne = eventColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    //
                    final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = eventColl.insertOne(event.toBsonDocument());
                    insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("app", String.format("successfully inserted item with id %s",
                                        task.getResult().getInsertedId()));
                                consumer.accept(true);
                            } else {
                                Log.e("app", "failed to insert document with: ", task.getException());
                                consumer.accept(false);
                            }
                        }
                    });
                }
                else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                    final com.google.android.gms.tasks.Task<Document> findOneAndReplace = eventColl.findOneAndReplace(query, toUploadDoc);
                    findOneAndReplace.addOnCompleteListener(new OnCompleteListener<Document>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                            if (task.getResult() == null) {
                                Log.d("app", String.format("No document matches the provided query"));
                                Log.d("stitch-auth", "Authentication Successful.");
                                consumer.accept(false);
                            } else if (task.isSuccessful()) {
                                Log.d("app", String.format("Successfully found document: %s",
                                        task.getResult()));
                                consumer.accept(true);
                                Log.d("stitch-auth", "Authentication Successful.");
                            } else {
                                consumer.accept(false);
                                Log.e("app", "Failed to findOne: ", task.getException());
                                Log.d("stitch-auth", "Authentication Successful.");
                            }
                        }
                    });

                } else {
                    consumer.accept(false);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    }

    @Override
    public void postTask(Task post_task, Consumer<Boolean> consumer) {
        Document toUploadDoc = post_task.toBsonDocument();
        Document query = new Document().append("_id", post_task.getId());
        //Check to see if user already inside
        final com.google.android.gms.tasks.Task<Document> findOne = taskColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    //
                    final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = taskColl.insertOne(post_task.toBsonDocument());
                    insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("app", String.format("successfully inserted item with id %s",
                                        task.getResult().getInsertedId()));
                                consumer.accept(true);
                            } else {
                                Log.e("app", "failed to insert document with: ", task.getException());
                                consumer.accept(false);
                            }
                        }
                    });
                }
                else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                    final com.google.android.gms.tasks.Task<Document> findOneAndReplace = taskColl.findOneAndReplace(query, toUploadDoc);
                    findOneAndReplace.addOnCompleteListener(new OnCompleteListener<Document>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                            if (task.getResult() == null) {
                                Log.d("app", String.format("No document matches the provided query"));
                                Log.d("stitch-auth", "Authentication Successful.");
                                consumer.accept(false);
                            } else if (task.isSuccessful()) {
                                Log.d("app", String.format("Successfully found document: %s",
                                        task.getResult()));
                                consumer.accept(true);
                                Log.d("stitch-auth", "Authentication Successful.");
                            } else {
                                consumer.accept(false);
                                Log.e("app", "Failed to findOne: ", task.getException());
                                Log.d("stitch-auth", "Authentication Successful.");
                            }
                        }
                    });

                } else {
                    consumer.accept(false);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    }

    @Override
    public void postFee(Fee fee, Consumer<Boolean> consumer) {
        Document toUploadDoc = fee.toBsonDocument();
        Document query = new Document().append("_id", fee.getId());
        //Check to see if user already inside
        final com.google.android.gms.tasks.Task<Document> findOne = feeColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    //
                    final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = feeColl.insertOne(fee.toBsonDocument());
                    insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("app", String.format("successfully inserted item with id %s",
                                        task.getResult().getInsertedId()));
                                consumer.accept(true);
                            } else {
                                Log.e("app", "failed to insert document with: ", task.getException());
                                consumer.accept(false);
                            }
                        }
                    });
                }
                else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                    final com.google.android.gms.tasks.Task<Document> findOneAndReplace = feeColl.findOneAndReplace(query, toUploadDoc);
                    findOneAndReplace.addOnCompleteListener(new OnCompleteListener<Document>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                            if (task.getResult() == null) {
                                Log.d("app", String.format("No document matches the provided query"));
                                Log.d("stitch-auth", "Authentication Successful.");
                                consumer.accept(false);
                            } else if (task.isSuccessful()) {
                                Log.d("app", String.format("Successfully found document: %s",
                                        task.getResult()));
                                consumer.accept(true);
                                Log.d("stitch-auth", "Authentication Successful.");
                            } else {
                                consumer.accept(false);
                                Log.e("app", "Failed to findOne: ", task.getException());
                                Log.d("stitch-auth", "Authentication Successful.");
                            }
                        }
                    });

                } else {
                    consumer.accept(false);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    }
    //If there is no house add it and if there is a house update it
    @Override
    public void postHouse(House house, Consumer<Boolean> consumer) {
        Document toUploadDoc = house.toBsonDocument();
        Document query = new Document().append("_id", house.getId());
        //Check to see if user already inside
        final com.google.android.gms.tasks.Task<Document> findOne = housesColl.findOne(query);
        findOne.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                if (task.getResult() == null) {
                    Log.d("app", String.format("No document matches the provided query"));
                    Log.d("stitch-auth", "Authentication Successful.");
                    //
                    final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = housesColl.insertOne(house.toBsonDocument());
                    insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("app", String.format("successfully inserted item with id %s",
                                        task.getResult().getInsertedId()));
                                consumer.accept(true);
                            } else {
                                Log.e("app", "failed to insert document with: ", task.getException());
                                consumer.accept(false);
                            }
                        }
                    });
                }
                else if (task.isSuccessful()) {
                    Log.d("app", String.format("Successfully found document: %s",
                            task.getResult()));
                    Log.d("stitch-auth", "Authentication Successful.");
                    final com.google.android.gms.tasks.Task<Document> findOneAndReplace = housesColl.findOneAndReplace(query, toUploadDoc);
                    findOneAndReplace.addOnCompleteListener(new OnCompleteListener<Document>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Document> task) {
                            if (task.getResult() == null) {
                                Log.d("app", String.format("No document matches the provided query"));
                                Log.d("stitch-auth", "Authentication Successful.");
                                consumer.accept(false);
                            } else if (task.isSuccessful()) {
                                Log.d("app", String.format("Successfully found document: %s",
                                        task.getResult()));
                                consumer.accept(true);
                                Log.d("stitch-auth", "Authentication Successful.");
                            } else {
                                consumer.accept(false);
                                Log.e("app", "Failed to findOne: ", task.getException());
                                Log.d("stitch-auth", "Authentication Successful.");
                            }
                        }
                    });

                } else {
                    consumer.accept(false);
                    Log.e("app", "Failed to findOne: ", task.getException());
                    Log.d("stitch-auth", "Authentication Successful.");
                }
            }
        });
    }

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

//     add the delete stuff leave deletetion of user till end because its confusing

}
