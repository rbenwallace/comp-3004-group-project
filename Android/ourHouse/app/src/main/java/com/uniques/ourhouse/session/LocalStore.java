package com.uniques.ourhouse.session;

import android.content.Context;

import com.mongodb.stitch.android.core.auth.StitchAuth;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

final class LocalStore implements DatabaseLink {
    private static final String USER_FILE = "user.json";
    private static final String HOUSE_FILE = "house.json";
    private static final String TASKS_FILE = "tasks.json";
    private static final String FEES_FILE = "fees.json";
    private static final String EVENTS_FILE = "events.json";

    private Context context;

    LocalStore(Context context) {
        this.context = context;
    }

    @Override
    public StitchAuth getAuth() {
        return null;
    }

    @Override
    public boolean autoAuth() {
        return false;
    }

    @Override
    public boolean isLoggedIn(ObjectId userId) {
        return false;
    }

    @Override
    public void logout(Consumer<Boolean> consumer) {

    }

    @Override
    public StitchUser getStitchUser() {
        return null;
    }

    @Override
    public ArrayList<House> getLocalHouseArray(FragmentActivity activity) {
        return null;
    }

    @Override
    public User getCurrentLocalUser(FragmentActivity activity) {
        return null;
    }

    @Override
    public House getCurrentLocalHouse(FragmentActivity activity) {
        return null;
    }

    @Override
    public void setLocalHouseArray(ArrayList<House> myList, FragmentActivity activity) {

    }

    @Override
    public void setLocalUser(User user, FragmentActivity activity) {

    }

    @Override
    public void setLocalHouse(House house, FragmentActivity activity) {

    }

    @Override
    public void clearLocalHouses(FragmentActivity activity) {

    }

    @Override
    public void clearLocalCurHouse(FragmentActivity activity) {

    }

    @Override
    public void clearLocalCurUser(FragmentActivity activity) {

    }

    @Override
    public void clearLocalLoginData(FragmentActivity activity) {

    }

    @Override
    public void addMyUser(User user, FragmentActivity activity) {

    }

    @Override
    public void addMyHouse(House house, FragmentActivity activity, Consumer<Boolean> boolConsumer) {

    }

    @Override
    public void findHousesByName(String name, Consumer<ArrayList<House>> consumer) {

    }

    @Override
    public void getUser(ObjectId id, Consumer<User> consumer) {
        new User().fromJSON(searchLocal(USER_FILE, id), consumer);
    }

    @Override
    public void getEvent(ObjectId id, Consumer<Event> consumer) {
        new Event().fromJSON(searchLocal(EVENTS_FILE, id), consumer);
    }

    @Override
    public void getTask(ObjectId id, Consumer<Task> consumer) {
        new Task().fromJSON(searchLocal(TASKS_FILE, id), consumer);
    }

    @Override
    public void getFee(ObjectId id, Consumer<Fee> consumer) {
        new Fee().fromJSON(searchLocal(FEES_FILE, id), consumer);
    }

    @Override
    public void getHouse(ObjectId id, Consumer<House> consumer) {
        new House().fromJSON(searchLocal(HOUSE_FILE, id), consumer);
    }

    @Override
    public void getAllEventsFromHouse(ObjectId houseId, Consumer<ArrayList<Event>> consumer) {

    }

    @Override
    public void getAllTasksFromHouse(ObjectId houseId, Consumer<ArrayList<Task>> consumer) {

    }

    @Override
    public void getAllFeesFromHouse(ObjectId houseId, Consumer<ArrayList<Fee>> consumer) {

    }

    @Override
    public void getAllEventsFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Event>> consumer) {

    }

    @Override
    public void getAllTasksFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Task>> consumer) {

    }

    @Override
    public void getAllFeesFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<ArrayList<Fee>> consumer) {

    }

    @Override
    public void postUser(User user, Consumer<Boolean> consumer) {
        saveLocal(USER_FILE, user, consumer);
    }

    @Override
    public void postEvent(Event event, Consumer<Boolean> consumer) {
        saveLocal(EVENTS_FILE, event, consumer);
    }

    @Override
    public void postTask(Task task, Consumer<Boolean> consumer) {
        saveLocal(TASKS_FILE, task, consumer);
    }

    @Override
    public void postFee(Fee fee, Consumer<Boolean> consumer) {
        saveLocal(FEES_FILE, fee, consumer);
    }

    @Override
    public void postHouse(House house, Consumer<Boolean> consumer) {
        saveLocal(HOUSE_FILE, house, consumer);
    }

    @Override
    public void deleteAllEventsFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteAllTasksFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteAllFeesFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteAllEventsFromUser(ObjectId userId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteAllTasksFromUser(ObjectId userId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteAllFeesFromUser(ObjectId userId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteAllEventsFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteAllTasksFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteAllFeesFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteUser(User user, House userHouse, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteEvent(ObjectId eventId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteTask(ObjectId taskId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteFee(ObjectId feeId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteHouse(ObjectId houseId, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteOwnerFromHouse(House house, User user, Consumer<Boolean> consumer) {

    }

    @Override
    public void deleteUserFromHouse(House house, User user, Consumer<Boolean> consumer) {

    }

    @Override
    public void updateUser(User user, Consumer<Boolean> consumer) {

    }

    @Override
    public void updateFee(Fee fee, Consumer<Boolean> consumer) {

    }

    @Override
    public void updateTask(Task task, Consumer<Boolean> consumer) {

    }

    @Override
    public void updateEvent(House event, Consumer<Boolean> consumer) {

    }

    @Override
    public void updateHouse(House house, Consumer<Boolean> consumer) {

    }

    @Override
    public void updateOwner(House house, User user, Consumer<Boolean> consumer) {

    }

    @Override
    public void checkKey(String id, Consumer<Boolean> consumer) {

    }

    @Override
    public Document getQueryForUser() {
        return null;
    }


    private JSONElement searchLocal(String fileName, ObjectId id) {
        return Objects.requireNonNull(Objects.requireNonNull(retrieveLocal(fileName)).search(id.toString()));
    }

    private void saveLocal(String filename, Indexable model, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(filename));
        json.putStructure(model.getId().toString(), model.toJSON());
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            e.printStackTrace();
        }
        consumer.accept(false);
    }

    private EasyJSON retrieveLocal(String fileName) {
        File file = getLocalFile(fileName);
        if (!file.exists()) {
            populateStores();
//            return retrieveLocal(fileName);
            file = getLocalFile(fileName);
        }
        try {
            return EasyJSON.open(file);
        } catch (EasyJSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getLocalFile(String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    private void populateStores() {
        House house = new House();
        house.setName("Test House");
        populateNewStore(HOUSE_FILE, house);
    }

    private void populateNewStore(String fileName, Indexable... models) {
        EasyJSON store = EasyJSON.create(getLocalFile(fileName));
        store.getRootNode().setType(SafeJSONElementType.ARRAY);
        for (Indexable model : models) {
            store.putStructure(model.getId().toString(), model.toJSON());
        }
        try {
            store.save();
        } catch (EasyJSONException e) {
            e.printStackTrace();
        }
    }
}
