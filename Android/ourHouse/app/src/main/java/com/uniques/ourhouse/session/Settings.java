package com.uniques.ourhouse.session;

import android.content.Context;

import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.io.File;
import java.util.UUID;

public enum Settings {

    OPEN_HOUSE(new UUIDHandler() {
        @Override
        String getKey() {
            return "openHouse";
        }

        @Override
        UUID getDefault() {
            return null;
        }
    }),

    FEED_FILTER_USER(new UUIDHandler() {
        @Override
        String getKey() {
            return "feed.filterUser";
        }

        @Override
        UUID getDefault() {
            return null;
        }
    }),

    FEED_SHOW_LATE(new DefaultHandler<Boolean>() {
        @Override
        String getKey() {
            return "feed.showLate";
        }

        @Override
        Boolean getDefault() {
            return true;
        }
    }),

    FEED_SHOW_ON_TIME(new DefaultHandler<Boolean>() {
        @Override
        String getKey() {
            return "feed.showOnTime";
        }

        @Override
        Boolean getDefault() {
            return true;
        }
    });

    private Handler handler;

    Settings(Handler handler) {
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public <T> T get() {
        if (handler.obj == null) {
            handler.readWithDefault(settings.getRootNode());
        }
        return (T) handler.obj;
    }

    public boolean set(Object newValue) {
        handler.obj = newValue;
        handler.write(settings.getRootNode());
        try {
            settings.save();
            return true;
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static EasyJSON settings;

    static void init(Context context) {
        File file = new File(context.getFilesDir(), "settings.json");
        if (file.exists()) {
            try {
                settings = EasyJSON.open(file);
            } catch (EasyJSONException e) {
                e.printStackTrace();
            }
            for (Settings setting : values()) {
                try {
                    setting.handler.readWithDefault(settings.getRootNode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            settings = EasyJSON.create(file);
        }
    }

    private static abstract class Handler<T> {
        T obj;

        abstract String getKey();

        void readWithDefault(JSONElement parent) {
            if (parent.elementExists(getKey())) {
                read(parent);
            } else {
                obj = getDefault();
            }
        }

        abstract T getDefault();

        protected abstract void read(JSONElement parent);

        protected abstract void write(JSONElement parent);
    }

    private static abstract class DefaultHandler<T> extends Handler<T> {

        @Override
        protected void read(JSONElement parent) {
            obj = parent.valueOf(getKey());
        }

        @Override
        protected void write(JSONElement parent) {
            parent.putPrimitive(getKey(), obj.toString());
        }
    }

    private static abstract class UUIDHandler extends Handler<UUID> {

        @Override
        protected void read(JSONElement parent) {
            String uuidString = parent.valueOf(getKey());
            obj = uuidString == null ? null : UUID.fromString(uuidString);
        }

        @Override
        protected void write(JSONElement parent) {
            if (obj == null) {
                parent.removeElement(getKey());
            } else {
                parent.putPrimitive(getKey(), obj.toString());
            }
        }
    }
}
