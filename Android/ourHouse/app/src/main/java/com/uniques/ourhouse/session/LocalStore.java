package com.uniques.ourhouse.session;

import android.content.Context;

import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

final class LocalStore implements DatabaseLink {
    private static final String USERS_FILE = "users.json";

    private Context context;

    LocalStore(Context context) {
        this.context = context;
    }

    @Override
    public User getUser(UUID id) {
        return new User().fromJSON(searchLocal(USERS_FILE, id));
    }

    @Override
    public boolean postUser(User user) {
        return saveLocal(USERS_FILE, user);
    }

    private JSONElement searchLocal(String fileName, UUID id) {
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
            //TODO this creates an infinite loop
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
        populateNewStore(USERS_FILE);
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
