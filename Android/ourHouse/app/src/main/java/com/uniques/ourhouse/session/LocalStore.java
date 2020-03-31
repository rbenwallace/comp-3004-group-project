package com.uniques.ourhouse.session;

import android.content.Context;
import android.util.Log;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.types.ObjectId;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("CodeBlock2Expr")
final class LocalStore implements DatabaseLink {

    private static final String TAG = "LocalStore";
    private static final String USERS_FILE = "users.json";
    private static final String HOUSES_FILE = "houses.json";
    private static final String TASKS_FILE = "tasks.json";
    private static final String FEES_FILE = "fees.json";
    private static final String EVENTS_FILE = "events.json";

    private final EasyJSON USERS_JSON;
    private final EasyJSON HOUSES_JSON;
    private final EasyJSON TASKS_JSON;
    private final EasyJSON FEES_JSON;
    private final EasyJSON EVENTS_JSON;

    private Context context;

    LocalStore(Context context) {
        this.context = context;
        USERS_JSON = Objects.requireNonNull(retrieveLocal(USERS_FILE));
        HOUSES_JSON = Objects.requireNonNull(retrieveLocal(HOUSES_FILE));
        TASKS_JSON = Objects.requireNonNull(retrieveLocal(TASKS_FILE));
        FEES_JSON = Objects.requireNonNull(retrieveLocal(FEES_FILE));
        EVENTS_JSON = Objects.requireNonNull(retrieveLocal(EVENTS_FILE));
    }

    @Override
    public void findHousesByName(String name, Consumer<List<House>> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(HOUSES_FILE));
        List<JSONElement> houses = json.getRootNode().getChildren();
        List<House> results = new ArrayList<>();
        for (int i = houses.size() - 1; i >= 0; --i) {
            JSONElement element = houses.get(i);
            if (!element.getKey().startsWith(name)) {
                houses.remove(i);
            }
        }
        if (houses.isEmpty()) {
            consumer.accept(results);
            return;
        }
        houses.forEach(element -> {
            new House().fromJSON(element, house -> {
                results.add((House) house);
                houses.remove(element);
                if (houses.isEmpty()) {
                    consumer.accept(results);
                }
            });
        });
    }

    @Override
    public void getUser(ObjectId id, Consumer<User> consumer) {
        JSONElement json = searchLocal(USERS_JSON, id);
        if (json == null) {
            consumer.accept(null);
        } else {
            new User().fromJSON(json, consumer);
        }
    }

    @Override
    public void getEvent(ObjectId id, Consumer<Event> consumer) {
        JSONElement json = searchLocal(EVENTS_JSON, id);
        if (json == null) {
            consumer.accept(null);
        } else {
            new Event().fromJSON(json, consumer);
        }
    }

    @Override
    public void getHouseEventOnDay(ObjectId houseId, Date day, Consumer<Event> consumer) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long fromDate = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long toDate = cal.getTimeInMillis();
        for (JSONElement eventJSON : EVENTS_JSON) {
            try {
                if (eventJSON.valueOf("assignedHouse").equals(houseId.toString())) {
                    long dueDate = eventJSON.valueOf("dueDate");
                    if (dueDate >= fromDate && dueDate <= toDate) {
                        new Event().fromJSON(eventJSON, consumer);
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        consumer.accept(null);
    }

    @Override
    public void getTask(ObjectId id, Consumer<Task> consumer) {
        JSONElement json = searchLocal(TASKS_JSON, id);
        if (json == null) {
            consumer.accept(null);
        } else {
            new Task().fromJSON(json, consumer);
        }
    }

    @Override
    public void getFee(ObjectId id, Consumer<Fee> consumer) {
        JSONElement json = searchLocal(FEES_JSON, id);
        if (json == null) {
            consumer.accept(null);
        } else {
            new Fee().fromJSON(json, consumer);
        }
    }

    @Override
    public void getHouse(ObjectId id, Consumer<House> consumer) {
        JSONElement json = searchLocal(HOUSES_JSON, id);
        if (json == null) {
            consumer.accept(null);
        } else {
            new House().fromJSON(json, consumer);
        }
    }

    @Override
    public void getAllEventsFromHouse(ObjectId houseId, Consumer<List<Event>> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(EVENTS_FILE));
        List<JSONElement> events = json.getRootNode().getChildren();
        for (int i = events.size() - 1; i >= 0; --i) {
            JSONElement element = events.get(i);
            if (!element.valueOf("assignedHouse").equals(houseId.toString())) {
                events.remove(i);
            }
        }
        List<Event> results = new ArrayList<>();
        if (events.isEmpty()) {
            consumer.accept(results);
            return;
        }
        events.forEach(element -> {
            new Event().fromJSON(element, event -> {
                results.add((Event) event);
                events.remove(element);
                if (events.isEmpty()) {
                    consumer.accept(results);
                }
            });
        });
    }

    @Override
    public void getAllTasksFromHouse(ObjectId houseId, Consumer<List<Task>> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(TASKS_FILE));
        List<JSONElement> tasks = json.getRootNode().getChildren();
        for (int i = tasks.size() - 1; i >= 0; --i) {
            JSONElement element = tasks.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())) {
                tasks.remove(i);
            }
        }
        List<Task> results = new ArrayList<>();
        if (tasks.isEmpty()) {
            consumer.accept(results);
            return;
        }
        tasks.forEach(element -> {
            new Task().fromJSON(element, task -> {
                results.add((Task) task);
                tasks.remove(element);
                if (tasks.isEmpty()) {
                    consumer.accept(results);
                }
            });
        });
    }

    @Override
    public void getAllFeesFromHouse(ObjectId houseId, Consumer<List<Fee>> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(FEES_FILE));
        List<JSONElement> fees = json.getRootNode().getChildren();
        for (int i = fees.size() - 1; i >= 0; --i) {
            JSONElement element = fees.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())) {
                fees.remove(i);
            }
        }
        List<Fee> results = new ArrayList<>();
        if (fees.isEmpty()) {
            consumer.accept(results);
            return;
        }
        fees.forEach(element -> {
            new Fee().fromJSON(element, fee -> {
                results.add((Fee) fee);
                fees.remove(element);
                if (fees.isEmpty()) {
                    consumer.accept(results);
                }
            });
        });
    }

    @Override
    public void getAllEventsFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Event>> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(EVENTS_FILE));
        List<JSONElement> events = json.getRootNode().getChildren();
        for (int i = events.size() - 1; i >= 0; --i) {
            JSONElement element = events.get(i);
            if (!element.valueOf("assignedHouse").equals(houseId.toString())
                    || !element.valueOf("assignedTo").equals(userId.toString())) {
                events.remove(i);
            }
        }
        List<Event> results = new ArrayList<>();
        if (events.isEmpty()) {
            consumer.accept(results);
            return;
        }
        events.forEach(element -> {
            new Event().fromJSON(element, event -> {
                results.add((Event) event);
                events.remove(element);
                if (events.isEmpty()) {
                    consumer.accept(results);
                }
            });
        });
    }

    @Override
    public void getAllTasksFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Task>> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(TASKS_FILE));
        List<JSONElement> tasks = json.getRootNode().getChildren();
        for (int i = tasks.size() - 1; i >= 0; --i) {
            JSONElement element = tasks.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())
                    || !element.valueOf("userId").equals(userId.toString())) {
                tasks.remove(i);
            }
        }
        List<Task> results = new ArrayList<>();
        if (tasks.isEmpty()) {
            consumer.accept(results);
            return;
        }
        tasks.forEach(element -> {
            new Task().fromJSON(element, task -> {
                results.add((Task) task);
                tasks.remove(element);
                if (tasks.isEmpty()) {
                    consumer.accept(results);
                }
            });
        });
    }

    @Override
    public void getAllFeesFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Fee>> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(FEES_FILE));
        List<JSONElement> fees = json.getRootNode().getChildren();
        for (int i = fees.size() - 1; i >= 0; --i) {
            JSONElement element = fees.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())
                    || !element.valueOf("userId").equals(userId.toString())) {
                fees.remove(i);
            }
        }
        List<Fee> results = new ArrayList<>();
        if (fees.isEmpty()) {
            consumer.accept(results);
            return;
        }
        fees.forEach(element -> {
            new Fee().fromJSON(element, fee -> {
                results.add((Fee) fee);
                fees.remove(element);
                if (fees.isEmpty()) {
                    consumer.accept(results);
                }
            });
        });
    }

    @Override
    public void postUser(User user, Consumer<Boolean> consumer) {
        saveLocal(USERS_JSON, user, consumer);
    }

    @Override
    public void postEvent(Event event, Consumer<Boolean> consumer) {
        saveLocal(EVENTS_JSON, event, consumer);
    }

    @Override
    public void postTask(Task task, Consumer<Boolean> consumer) {
        saveLocal(TASKS_JSON, task, consumer);
    }

    @Override
    public void postFee(Fee fee, Consumer<Boolean> consumer) {
        saveLocal(FEES_JSON, fee, consumer);
    }

    @Override
    public void postHouse(House house, Consumer<Boolean> consumer) {
        saveLocal(HOUSES_JSON, house, consumer);
    }

    @Override
    public void deleteAllEventsFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(EVENTS_FILE));
        List<JSONElement> events = json.getRootNode().getChildren();
        for (int i = events.size() - 1; i >= 0; --i) {
            JSONElement element = events.get(i);
            if (!element.valueOf("assignedHouse").equals(houseId.toString())
                    || !element.valueOf("assignedTo").equals(userId.toString())) {
                events.remove(i);
            }
        }
        events.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllEventsFromUserInHouse", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteAllTasksFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(TASKS_FILE));
        List<JSONElement> tasks = json.getRootNode().getChildren();
        for (int i = tasks.size() - 1; i >= 0; --i) {
            JSONElement element = tasks.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())
                    || !element.valueOf("userId").equals(userId.toString())) {
                tasks.remove(i);
            }
        }
        tasks.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllTasksFromUserInHouse", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteAllFeesFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(FEES_FILE));
        List<JSONElement> fees = json.getRootNode().getChildren();
        for (int i = fees.size() - 1; i >= 0; --i) {
            JSONElement element = fees.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())
                    || !element.valueOf("userId").equals(userId.toString())) {
                fees.remove(i);
            }
        }
        fees.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllFeesFromUserInHouse", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteAllEventsFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(EVENTS_FILE));
        List<JSONElement> events = json.getRootNode().getChildren();
        for (int i = events.size() - 1; i >= 0; --i) {
            JSONElement element = events.get(i);
            if (!element.valueOf("assignedTo").equals(userId.toString())) {
                events.remove(i);
            }
        }
        events.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllEventsFromUser", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteAllTasksFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(TASKS_FILE));
        List<JSONElement> tasks = json.getRootNode().getChildren();
        for (int i = tasks.size() - 1; i >= 0; --i) {
            JSONElement element = tasks.get(i);
            if (!element.valueOf("userId").equals(userId.toString())) {
                tasks.remove(i);
            }
        }
        tasks.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllTasksFromUser", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteAllFeesFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(FEES_FILE));
        List<JSONElement> fees = json.getRootNode().getChildren();
        for (int i = fees.size() - 1; i >= 0; --i) {
            JSONElement element = fees.get(i);
            if (!element.valueOf("userId").equals(userId.toString())) {
                fees.remove(i);
            }
        }
        fees.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllFeesFromUser", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteAllEventsFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(FEES_FILE));
        List<JSONElement> fees = json.getRootNode().getChildren();
        for (int i = fees.size() - 1; i >= 0; --i) {
            JSONElement element = fees.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())) {
                fees.remove(i);
            }
        }
        fees.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllEventsFromHouse", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteAllTasksFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(TASKS_FILE));
        List<JSONElement> tasks = json.getRootNode().getChildren();
        for (int i = tasks.size() - 1; i >= 0; --i) {
            JSONElement element = tasks.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())) {
                tasks.remove(i);
            }
        }
        tasks.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllTasksFromHouse", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteAllFeesFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(FEES_FILE));
        List<JSONElement> fees = json.getRootNode().getChildren();
        for (int i = fees.size() - 1; i >= 0; --i) {
            JSONElement element = fees.get(i);
            if (!element.valueOf("houseId").equals(houseId.toString())) {
                fees.remove(i);
            }
        }
        fees.forEach(element -> json.getRootNode().removeElement(element.getKey()));
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteAllFeesFromHouse", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteUser(User user, Consumer<Boolean> consumer) {
        deleteAllEventsFromUser(user.getId(), s1 -> {
            if (!s1) {
                consumer.accept(false);
                return;
            }
            deleteAllTasksFromUser(user.getId(), s2 -> {
                if (!s2) {
                    consumer.accept(false);
                    return;
                }
                deleteAllFeesFromUser(user.getId(), s3 -> {
                    if (!s3) {
                        consumer.accept(false);
                        return;
                    }
//                    deleteUserFromHouse(userHouse, user, s4 -> {
//                        if (!s4) {
//                            consumer.accept(false);
//                            return;
//                        }
                    EasyJSON json = Objects.requireNonNull(retrieveLocal(USERS_FILE));
                    json.removeElement(user.getId().toString());
                    try {
                        json.save();
                        consumer.accept(true);
                    } catch (EasyJSONException e) {
                        Log.e(TAG, "Failed to deleteUser", e);
                        consumer.accept(false);
                    }
//                    });
                });
            });
        });
    }

    @Override
    public void deleteEvent(Event event, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(EVENTS_FILE));
        json.removeElement(event.getId().toString());
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteEvent", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteTask(Task task, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(TASKS_FILE));
        json.removeElement(task.getId().toString());
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteTask", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteFee(Fee fee, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(FEES_FILE));
        json.removeElement(fee.getId().toString());
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteFee", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteHouse(House house, Consumer<Boolean> consumer) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(HOUSES_FILE));
        json.removeElement(house.getId().toString());
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to deleteHouse", e);
            consumer.accept(false);
        }
    }

    @Override
    public void deleteUserFromHouse(House house, User user, Consumer<Boolean> consumer) {
        house.removeOccupant(user);
        updateHouse(house, consumer);
    }

    @Override
    public void deleteAllCollectionData(Consumer<Boolean> consumer) {
        clearLocalState(consumer);
    }

    @Override
    public void updateUser(User user, Consumer<Boolean> consumer) {
        postUser(user, consumer);
    }

    @Override
    public void updateFee(Fee fee, Consumer<Boolean> consumer) {
        postFee(fee, consumer);
    }

    @Override
    public void updateTask(Task task, Consumer<Boolean> consumer) {
        postTask(task, consumer);
    }

    @Override
    public void deleteAllHouses(Consumer<Boolean> consumer) {

    }

    @Override
    public void updateEvent(Event event, Consumer<Boolean> consumer) {
        postEvent(event, consumer);
    }

    @Override
    public void updateHouse(House house, Consumer<Boolean> consumer) {
        postHouse(house, consumer);
    }

    @Override
    public void updateOwner(House house, User user, Consumer<Boolean> consumer) {
        house.setOwner(user.getId());
        updateHouse(house, consumer);
    }

    @Override
    public void checkIfHouseKeyExists(String id, Consumer<Boolean> consumer) {
    }

    @Override
    public void clearLocalState(Consumer<Boolean> consumer) {
        if (getLocalFile(USERS_FILE).delete())
            if (getLocalFile(HOUSES_FILE).delete())
                if (getLocalFile(TASKS_FILE).delete())
                    if (getLocalFile(FEES_FILE).delete())
                        if (getLocalFile(EVENTS_FILE).delete()) {
                            consumer.accept(true);
                            return;
                        }

        consumer.accept(false);
    }

    private JSONElement searchLocal(EasyJSON json, ObjectId id) {
        return json.search(id.toString());
    }

    private void saveLocal(EasyJSON json, Indexable model, Consumer<Boolean> consumer) {
        json.putStructure(model.getId().toString(), model.toJSON());
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            Log.e(TAG, "Failed to saveLocal", e);
            consumer.accept(false);
        }
    }

    private EasyJSON retrieveLocal(String fileName) {
        File file = getLocalFile(fileName);
        if (!file.exists()) {
            EasyJSON json = EasyJSON.create(file);
            try {
                json.save();
                return json;
            } catch (EasyJSONException e) {
                Log.e(TAG, "Failed to save new json", e);
                return null;
            }
//            populateStores();
//            return retrieveLocal(fileName);
        } else {
            try {
//                Log.d("myNewUser", "before");
//                Log.d("myNewUser", "hello" + EasyJSON.open(file).toString());
//                Log.d("myNewUser", "after");
                return EasyJSON.open(file);
            } catch (EasyJSONException e) {
                Log.e(TAG, "Failed to open json file", e);
                return null;
            }
        }
    }

    @Override
    public void emailFriends(String email, String firstName, String friend, ObjectId houseId, Consumer<Boolean> bool) {

    }

    private File getLocalFile(String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

//    private void populateStores() {
//        populateNewStore(HOUSES_FILE, House.testHouse());
//    }
//
//    private void populateNewStore(String fileName, Indexable... models) {
//        EasyJSON store = EasyJSON.create(getLocalFile(fileName));
//        store.getRootNode().setType(SafeJSONElementType.ARRAY);
//        for (Indexable model : models) {
//            store.putStructure(model.getId().toString(), model.toJSON());
//        }
//        try {
//            store.save();
//        } catch (EasyJSONException e) {
//            Log.e(TAG, "Failed to populate store " + fileName, e);
//        }
//    }
}
