package com.uniques.ourhouse.model;

import android.util.Log;

import com.google.gson.JsonObject;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class House implements Model, Indexable, Observable {
    private ObjectId houseId;
    private String houseKey;
    private User owner;
    private String name;
    private ArrayList<User> occupants;
    private Rotation rotation;
    private String password;
    private boolean showTaskDifficulty;
    private boolean penalizeLateTasks;

    @NonNull
    @Override
    public ObjectId getId() {
        return houseId;
    }

    public String getKeyId() {
        return houseKey;
    }

    public static final String HOUSE_COLLECTION = "Houses";

    public House(ObjectId id, String housekey, User owner, String name, ArrayList<User> occupants, House.Rotation rotation, String password, boolean showTaskDifficulty, boolean penalizeLateTasks) {
        this.houseId = id;
        this.houseKey = housekey;
        this.owner = owner;
        this.name = name;
        this.occupants = occupants;
        this.rotation = rotation;
        this.password = password;
        this.showTaskDifficulty = showTaskDifficulty;
        this.penalizeLateTasks = penalizeLateTasks;
    }

    public House(String name, User owner, ArrayList<User> occupants, House.Rotation rotation, String password, boolean showTaskDifficulty, boolean penalizeLateTasks) {
        this.houseId = new ObjectId();
        this.houseKey = name + Session.keyGen();
        this.owner = owner;
        this.name = name;
        this.occupants = occupants;
        this.rotation = rotation;
        this.password = password;
        this.showTaskDifficulty = showTaskDifficulty;
        this.penalizeLateTasks = penalizeLateTasks;
    }

    public House() {
        houseId = new ObjectId();
        occupants = new ArrayList<>();
        rotation = new Rotation();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<User> getOccupants() {
        return occupants;
    }

    public void addOccupant(User occupant) {
        occupants.add(occupant);
    }

    public void removeOccupant(User occupant) {
        if (occupants.contains(occupant)) {
            occupants.remove(occupant);
            return;
        }
        Log.d("House", "No user in this House");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getCompareType() {
        return STRING;
    }

    @Override
    public Comparable getCompareObject() {
        return name;
    }

    @Override
    public String consoleFormat(String prefix) {
        return "House (" + houseId.toString() + ") [" + name + "]";
    }

    public House.Rotation getRotation() {
        return rotation;
    }

    public void setRotation(House.Rotation rotation) {
        this.rotation = rotation;
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        if (houseId != null)
            asDoc.put("_id", houseId);
        asDoc.put("key", houseKey);
        Document occupantsDoc = new Document();
        for (User user : occupants) {
            occupantsDoc.put(user.getEmailAddress(), user.toBsonDocument());
        }
        Document rotationDoc = new Document();
        for (User user : rotation.rotation) {
            rotationDoc.put(user.getEmailAddress(), user.toBsonDocument());
        }
        asDoc.put("owner", owner.toBsonDocument());
        asDoc.put("name", name);
        asDoc.put("occupants", occupantsDoc);
        asDoc.put("rotation", rotationDoc);
        asDoc.put("password", password);
        asDoc.put("showTaskDifficulty", showTaskDifficulty);
        asDoc.put("penalizeLateTasks", penalizeLateTasks);
        return asDoc;
    }

    public static House fromBsonDocument(final Document doc) {
        ObjectId houseId = (ObjectId) doc.get("_id");
        String houseKey = doc.getString("key");
        User owner = User.fromBsonDocument((Document) doc.get("owner"));
        String name = doc.getString("name");
        Document occDoc = (Document) (doc.get("occupants"));
        ArrayList<User> occupants = new ArrayList<>();
        occDoc.forEach((key, value) -> {
            occupants.add(User.fromBsonDocument((Document) value));
        });
        Rotation rotation = new Rotation();
        rotation.rotation.clear();
        Document rotDoc = (Document) (doc.get("rotation"));
        rotDoc.forEach((key, value) -> {
            rotation.rotation.add(User.fromBsonDocument((Document) value));
        });
        String password = doc.getString("password");
        Boolean showTaskDifficulty = doc.getBoolean("showTaskDifficulty");
        Boolean penalizeLateTasks = doc.getBoolean("penalizeLateTasks");
        return new House(houseId, houseKey, owner, name, occupants, rotation, password, showTaskDifficulty, penalizeLateTasks);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof House) {
            return ((House) obj).houseId.equals(houseId);
        }
        return false;
    }

    public static class Rotation implements Model {

        private ArrayList<User> rotation;

        public Rotation(ArrayList<User> rotation) {
            this.rotation = rotation;
        }

        public Rotation() {
            rotation = new ArrayList<>();
        }

        public ArrayList<User> getRotation() {
            return rotation;
        }

        public void setRotation(ArrayList<User> rotation) {
            this.rotation = rotation;
        }

        @Override
        public String consoleFormat(String prefix) {
            return rotation.toString();
        }

        public void addUserToRotation(User user) {
            rotation.add(user);
        }


        @Override
        public JSONElement toJSON() {
            EasyJSON json = EasyJSON.create();
            json.getRootNode().setType(SafeJSONElementType.ARRAY);
            for (User user : rotation) {
                json.putPrimitive(user.getId());
            }
            return json.getRootNode();
        }

        @Override
        public void fromJSON(JSONElement json, Consumer consumer) {
            Consumer<User> aUser = a -> rotation.add(a);
            List<JSONElement> users = json.getChildren();
            int usersLength = users.size();
            rotation = new ArrayList<>();
            for (int i = 0; i < usersLength; ++i) {
                Session.getSession().getDatabase().getUser(new ObjectId(users.get(i).<String>getValue()), aUser);
            }
            consumer.accept(this);
        }
    }

    public static House fromJSON(JsonObject obj) {
        Log.d("tryingtogetit", obj.toString());
        JsonObject id = obj.get("_id").getAsJsonObject();
        String myID = id.get("$oid").getAsString();
        String myKey = obj.get("key").getAsString();
        User owner = User.fromJSON(obj.get("owner").getAsJsonObject());
        String name = obj.get("name").getAsString();
        JsonObject occupants = obj.get("occupants").getAsJsonObject();
        Set<String> keys = occupants.keySet();
        Iterator<String> keyIt = keys.iterator();
        ArrayList<User> occs = new ArrayList<User>();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            if (occupants.get(key) != null) {
                occs.add(User.fromJSON(occupants.get(key).getAsJsonObject()));
            }
        }
        JsonObject rotation = obj.get("rotation").getAsJsonObject();
        keys = rotation.keySet();
        keyIt = keys.iterator();
        ArrayList<User> rot = new ArrayList<User>();
        Rotation actualRotation = new Rotation();
        actualRotation.setRotation(rot);
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            if (rotation.get(key) != null) {
                rot.add(User.fromJSON(rotation.get(key).getAsJsonObject()));
            }
        }
        String password = obj.get("password").getAsString();
        Boolean showTaskDifficulty = obj.get("showTaskDifficulty").getAsBoolean();
        Boolean penalizeLateTasks = obj.get("penalizeLateTasks").getAsBoolean();
        return new House(new ObjectId(myID), myKey, owner, name, occs, actualRotation, password, showTaskDifficulty, penalizeLateTasks);
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("houseId", houseId.toString());
        json.putPrimitive("name", name);
        json.putArray("occupants");
        for (User occupant : occupants) {
            json.search("occupants").putPrimitive(occupant.getId().toString());
        }
        json.putElement("rotation", rotation.toJSON());
        json.putPrimitive("showTaskDifficulty", showTaskDifficulty);
        json.putPrimitive("penalizeLateTasks", penalizeLateTasks);
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        House that = this;
        houseId = new ObjectId(json.<String>valueOf("houseId"));
        Consumer<User> c = user -> {
            this.occupants.add(user);
        };
        name = json.valueOf("name");
        List<JSONElement> occupants = json.search("occupants").getChildren();
        this.occupants = new ArrayList<>();
        for (int i = 0; i < occupants.size(); ++i) {
            Session.getSession().getDatabase().getUser(occupants.get(i).getValue(), c);
        }
        password = json.valueOf("password");
        showTaskDifficulty = json.valueOf("showTaskDifficulty");
        penalizeLateTasks = json.valueOf("penalizeLateTasks");
        new Rotation().fromJSON(json.search("rotation"), rotation -> {
            that.rotation = (Rotation) rotation;
            consumer.accept(that);
        });
    }

    @Override
    public String toString() {
        return "House{" +
                "houseId=" + houseId +
                ", houseKey='" + houseKey + '\'' +
                ", owner=" + owner +
                ", name='" + name + '\'' +
                ", occupants=" + occupants +
                ", rotation=" + rotation +
                ", showTaskDifficulty=" + showTaskDifficulty +
                ", penalizeLateTasks=" + penalizeLateTasks +
                '}';
    }
}
