package com.uniques.ourhouse.session;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.stitch.android.core.auth.StitchAuth;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.function.Consumer;

import static android.content.Context.MODE_PRIVATE;

public interface DatabaseLink {
    //Stitch Functions
    //-------------------------------------------------------------
    StitchAuth getAuth();
    boolean autoAuth();
    boolean isLoggedIn(ObjectId userId);
    void logout(Consumer<Boolean> consumer);
    StitchUser getStitchUser();
    //-------------------------------------------------------------

    //SharedPref --> being moved to a different file
    //-------------------------------------------------------------
    ArrayList<House> getLocalHouseArray(FragmentActivity activity);
    User getCurrentLocalUser(FragmentActivity activity);
    House getCurrentLocalHouse(FragmentActivity activity);
    void setLocalHouseArray(ArrayList<House> myList, FragmentActivity activity);
    void setLocalUser(User user, FragmentActivity activity);
    void setLocalHouse(House house, FragmentActivity activity);
    void saveLocalHouse(House newHouse, FragmentActivity activity);
    void clearLocalHouses(FragmentActivity activity);
    void clearLocalCurHouse(FragmentActivity activity);
    void clearLocalCurUser(FragmentActivity activity);
    void clearLocalLoginData(FragmentActivity activity);
    //-------------------------------------------------------------

    //Database and Shared Pref
    //-------
    void addMyUser(User user, FragmentActivity activity);
    void addMyHouse(House house, FragmentActivity activity, Consumer<Boolean> boolConsumer);
    void findHousesByName(String name, Consumer<ArrayList<House>> consumer);
    //-------

    //Get
    //-------------------------------------------------------------
    void getUser(ObjectId id, Consumer<User> consumer);
    void getEvent(ObjectId id, Consumer<Event> consumer);
    void getTask(ObjectId id, Consumer<Task> consumer);
    void getFee(ObjectId id, Consumer<Fee> consumer);
    void getHouse(ObjectId id, Consumer<House> consumer);
    void getHousesForHouseArray(ArrayList<ObjectId> userHouses, Consumer<ArrayList<House>> consumer);
    void getAllEventsFromHouse(ObjectId houseId, Consumer<ArrayList<Event>> consumer);
    void getAllTasksFromHouse(ObjectId houseId, Consumer<ArrayList<Task>> consumer);
    void getAllFeesFromHouse(ObjectId houseId, Consumer<ArrayList<Fee>> consumer);
    void getAllEventsFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Event>> consumer);
    void getAllTasksFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Task>> consumer);
    void getAllFeesFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Fee>> consumer);
    //-------------------------------------------------------------

    //Post
    //-------------------------------------------------------------
    void postUser(User user, Consumer<Boolean> consumer);
    void postEvent(Event event, Consumer<Boolean> consumer);
    void postTask(Task post_task, Consumer<Boolean> consumer);
    void postFee(Fee fee, Consumer<Boolean> consumer);
    void postHouse(House house, Consumer<Boolean> consumer);
    //-------------------------------------------------------------

    //Delete
    //-------------------------------------------------------------
    void deleteAllEventsFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer);
    void deleteAllTasksFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer);
    void deleteAllFeesFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer);
    void deleteAllEventsFromUser(ObjectId userId, Consumer<Boolean> consumer);
    void deleteAllTasksFromUser(ObjectId userId, Consumer<Boolean> consumer);
    void deleteAllFeesFromUser(ObjectId userId, Consumer<Boolean> consumer);
    void deleteAllEventsFromHouse(ObjectId houseId, Consumer<Boolean> consumer);
    void deleteAllTasksFromHouse(ObjectId houseId, Consumer<Boolean> consumer);
    void deleteAllFeesFromHouse(ObjectId houseId, Consumer<Boolean> consumer);
    void deleteUser(User user, House userHouse, Consumer<Boolean> consumer);
    void deleteEvent(ObjectId eventId, Consumer<Boolean> consumer);
    void deleteTask(ObjectId taskId, Consumer<Boolean> consumer);
    void deleteFee(ObjectId feeId, Consumer<Boolean> consumer);
    void deleteHouse(ObjectId houseId, Consumer<Boolean> consumer);
    void deleteOwnerFromHouse(House house, User user, Consumer<Boolean> consumer);
    void deleteUserFromHouse(House house, User user, Consumer<Boolean> consumer);
    //-------------------------------------------------------------

    //Update
    //-------------------------------------------------------------
    void updateUser(User user, Consumer<Boolean> consumer);
    void updateFee(Fee fee, Consumer<Boolean> consumer);
    void updateTask(Task task, Consumer<Boolean> consumer);
    void updateEvent(House event, Consumer<Boolean> consumer);
    void updateHouse(House house, Consumer<Boolean> consumer);
    void updateOwner(House house, User user, Consumer<Boolean> consumer);
    //-------------------------------------------------------------

    //General functions needed for inserting into cloud
    //-------------------------------------------------------------
    void checkKey(String id, Consumer<Boolean> consumer);
    Document getQueryForUser();
    //-------------------------------------------------------------


}



