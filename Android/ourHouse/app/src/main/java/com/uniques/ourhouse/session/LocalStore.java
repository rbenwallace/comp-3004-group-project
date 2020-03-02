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

    private JSONElement searchLocal(EasyJSON json, ObjectId id) {
        return json.search(id.toString());
    }

    private void saveLocal(EasyJSON json, Indexable model, Consumer<Boolean> consumer) {
        json.putStructure(model.getId().toString(), model.toJSON());
        try {
            json.save();
            consumer.accept(true);
        } catch (EasyJSONException e) {
            e.printStackTrace();
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
                e.printStackTrace();
                return null;
            }
//            populateStores();
//            return retrieveLocal(fileName);
        } else {
            try {
                return EasyJSON.open(file);
            } catch (EasyJSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private File getLocalFile(String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    private void populateStores() {
        House house = new House();
        house.setName("Test House");
        populateNewStore(HOUSES_FILE, house);
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
