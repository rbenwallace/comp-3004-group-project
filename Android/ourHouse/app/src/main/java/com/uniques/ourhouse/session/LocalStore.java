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
import java.util.UUID;

final class LocalStore implements DatabaseLinkLocal {
    private static final String USER_FILE = "user.json";
    private static final String HOUSE_FILE = "house.json";
    private static final String TASKS_FILE = "tasks.json";
    private static final String FEES_FILE = "tasks.json";
    private static final String EVENTS_FILE = "events.json";

    private Context context;

    LocalStore(Context context) {
        this.context = context;
    }

    @Override
    public User getUser(ObjectId id) {
        return new User().fromJSON(searchLocal(USER_FILE, id));
    }

    @Override
    public Event getEvent(ObjectId id) {
        return new Event().fromJSON(searchLocal(EVENTS_FILE, id));
    }

    @Override
    public Task getTask(ObjectId id) {
        return new Task().fromJSON(searchLocal(TASKS_FILE, id));
    }

    @Override
    public Fee getFee(ObjectId id) {
        return new Fee().fromJSON(searchLocal(FEES_FILE, id));
    }

    @Override
    public House getHouse(ObjectId id) {
        return new House().fromJSON(searchLocal(HOUSE_FILE, id));
    }

    @Override
    public boolean postUser(User user) {
        return saveLocal(USER_FILE, user);
    }

    @Override
    public boolean postEvent(Event event) {
        return saveLocal(EVENTS_FILE, event);
    }

    @Override
    public boolean postTask(Task task) {
        return saveLocal(TASKS_FILE, task);
    }

    @Override
    public boolean postFee(Fee fee) {
        return saveLocal(FEES_FILE, fee);
    }

    @Override
    public boolean postHouse(House house) {
        return saveLocal(HOUSE_FILE, house);
    }

    private JSONElement searchLocal(String fileName, ObjectId id) {
        return Objects.requireNonNull(Objects.requireNonNull(retrieveLocal(fileName)).search(id.toString()));
    }

    private boolean saveLocal(String filename, Indexable model) {
        EasyJSON json = Objects.requireNonNull(retrieveLocal(filename));
        json.putStructure(model.getId().toString(), model.toJSON());
        try {
            json.save();
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private EasyJSON retrieveLocal(String fileName) {
        File file = getLocalFile(fileName);
        if (!file.exists()) {
            populateStores();
            return retrieveLocal(fileName);
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
