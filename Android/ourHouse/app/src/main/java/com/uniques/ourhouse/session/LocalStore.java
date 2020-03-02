package com.uniques.ourhouse.session;

import android.content.Context;

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

import org.bson.types.ObjectId;

import java.io.File;
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
