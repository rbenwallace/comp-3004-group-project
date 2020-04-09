package com.uniques.ourhouse.session;

import android.content.Context;

import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.types.ObjectId;

import java.io.File;

public enum Settings {

    OPEN_HOUSE(new ObjectIdHandler() {
        @Override
        String getKey() {
            return "openHouse";
        }

        @Override
        ObjectId getDefault() {
            return null;
        }
    }),

    FEED_SHOWING_PAST_EVENTS(new PrimitiveHandler<Boolean>() {
        @Override
        String getKey() {
            return "feed_showingPastEvents";
        }

        @Override
        Boolean getDefault() {
            return true;
        }
    }),

    FEED_FILTER_USER(new ObjectIdHandler() {
        @Override
        String getKey() {
            return "feed_filterUser";
        }

        @Override
        ObjectId getDefault() {
            return null;
        }
    }),

    FEED_SHOW_LATE(new PrimitiveHandler<Boolean>() {
        @Override
        String getKey() {
            return "feed_showLate";
        }

        @Override
        Boolean getDefault() {
            return true;
        }
    }),

    FEED_PENALIZE_TASKS(new PrimitiveHandler<Boolean>() {
        @Override
        String getKey() {
            return "feed_penalizeTasks";
        }

        @Override
        Boolean getDefault() {
            return true;
        }
    }),

    FEED_SHOW_ON_TIME(new PrimitiveHandler<Boolean>() {
        @Override
        String getKey() {
            return "feed_showOnTime";
        }

        @Override
        Boolean getDefault() {
            return true;
        }
    }),

    EVENT_SERVICE_LAST_UPDATE(new PrimitiveHandler<Long>() {
        @Override
        String getKey() {
            return "eventService_lastUpdate";
        }

        @Override
        Long getDefault() {
            return null;
        }
    }),

    EVENT_SERVICE_DUTIES_ARE_PRISTINE(new PrimitiveHandler<Boolean>() {
        @Override
        String getKey() {
            return "eventService_dutiesArePristine";
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

    private static abstract class PrimitiveHandler<U> extends Handler<U> {

        @Override
        protected void read(JSONElement parent) {
            obj = parent.valueOf(getKey());
        }

        @Override
        protected void write(JSONElement parent) {
            if (obj == null) {
                parent.removeElement(getKey());
            } else {
                parent.putPrimitive(getKey(), obj);
            }
        }
    }

    private static abstract class ObjectIdHandler extends Handler<ObjectId> {

        @Override
        protected void read(JSONElement parent) {
            String idString = parent.valueOf(getKey());
            obj = (idString == null ? null : new ObjectId(idString));
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
