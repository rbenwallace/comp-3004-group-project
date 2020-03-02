package com.uniques.ourhouse.model;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.MongoDB.MongoDB;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import javax.xml.transform.dom.DOMLocator;

public class House implements Model, Indexable, Observable {
    private ObjectId houseId;
    private String houseKey;
    private User owner;
    private String name;
    private ArrayList<User> occupants;
    private Rotation rotation;
    private boolean showTaskDifficulty;
    private boolean penalizeLateTasks;
    private MongoDB myDatabase = new MongoDB();

    @NonNull
    @Override
    public ObjectId getId() {
        return houseId;
    }
    public String getKeyId() {
        return houseKey;
    }

    public static final String HOUSE_COLLECTION = "Houses";

    public House(ObjectId id, String housekey, User owner, String name, ArrayList<User> occupants, House.Rotation rotation, boolean showTaskDifficulty, boolean penalizeLateTasks) {
        this.houseId = id;
        this.houseKey = housekey;
        this.owner = owner;
        this.name = name;
        this.occupants = occupants;
        this.rotation = rotation;
        this.showTaskDifficulty = showTaskDifficulty;
        this.penalizeLateTasks = penalizeLateTasks;
    }

    public House(String name, User owner, ArrayList<User> occupants, House.Rotation rotation, boolean showTaskDifficulty, boolean penalizeLateTasks) {
        this.houseId = new ObjectId();
        this.houseKey = name + myDatabase.keyGen();
        this.owner = owner;
        this.name = name;
        this.occupants = occupants;
        this.rotation = rotation;
        this.showTaskDifficulty = showTaskDifficulty;
        this.penalizeLateTasks = penalizeLateTasks;
    }
    public House(){
        this.houseId = new ObjectId();
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
        if(occupants.contains(occupant)) {
            occupants.remove(occupant);
            return;
        }
        Log.d("House", "No user in this House");
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
        if(houseId != null)
            asDoc.put("_id", houseId);
        asDoc.put("key", houseKey);
        Document occupantsDoc = new Document();
        for(User user : occupants){
            occupantsDoc.put(user.getEmailAddress(), user.toBsonDocument());
        }
        Document rotationDoc = new Document();
        for(User user : rotation.rotation){
            rotationDoc.put(user.getEmailAddress(), user.toBsonDocument());
        }
        asDoc.put("owner", owner.toBsonDocument());
        asDoc.put("name", name);
        asDoc.put("occupants", occupantsDoc);
        asDoc.put("rotation", rotationDoc);
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
        occDoc.forEach((key, value)-> {
            occupants.add(User.fromBsonDocument((Document)value));
        });
        Rotation rotation = new Rotation();
        rotation.rotation.clear();
        Document rotDoc = (Document) (doc.get("rotation"));
        rotDoc.forEach((key, value)-> {
            rotation.rotation.add(User.fromBsonDocument((Document)value));
        });
        Boolean showTaskDifficulty = doc.getBoolean("showTaskDifficulty");
        Boolean penalizeLateTasks = doc.getBoolean("penalizeLateTasks");
        return new House(houseId, houseKey, owner, name, occupants, rotation, showTaskDifficulty, penalizeLateTasks);
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
            rotation = new ArrayList<User>();
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
        public Rotation fromJSON(JSONElement json) {
            Consumer<User> aUser = a -> rotation.add(a);
            List<JSONElement> users = json.getChildren();
            int usersLength = users.size();
            rotation = new ArrayList<User>();
            for (int i = 0; i < usersLength; ++i) {
                Session.getSession().getDatabase().getUser((ObjectId)users.get(i).getValue(), aUser);
            }
            return null;
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
        while(keyIt.hasNext()) {
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
        while(keyIt.hasNext()) {
            String key = keyIt.next();
            if (rotation.get(key) != null) {
                rot.add(User.fromJSON(rotation.get(key).getAsJsonObject()));
            }
        }
        Boolean showTaskDifficulty = obj.get("showTaskDifficulty").getAsBoolean();
        Boolean penalizeLateTasks = obj.get("penalizeLateTasks").getAsBoolean();
        return new House(new ObjectId(myID), myKey, owner, name, occs, actualRotation, showTaskDifficulty, penalizeLateTasks);
    }

    @Override
    public JSONElement toJSON() {
//        EasyJSON json = EasyJSON.create();
//        json.putPrimitive(houseId, houseId.toString());
//        json.putPrimitive(name, name);
//        json.putArray(Fields.Occupants);
//        for (User occupant : occupants) {
//            json.search(Fields.Occupants).putPrimitive(occupant.getId().toString());
//        }
//        json.putElement(Fields.Rotation, rotation.toJSON());
//        json.putPrimitive(Fields.ShowTaskDIfficulty, showTaskDifficulty);
//        json.putPrimitive(Fields.PenalizeLateTasks, penalizeLateTasks);
//        return json.getRootNode();
        return null;
    }

    @Override
    public House fromJSON(JSONElement json) {
        houseId = json.valueOf("houseId");
        Consumer<User> consumer = user -> {
            this.occupants.add(user);
        };
        name = json.valueOf("name");
        List<JSONElement> occupants = json.search("occupants").getChildren();
        this.occupants = new ArrayList<User>();
        for (int i = 0; i < occupants.size(); ++i) {
            Session.getSession().getDatabase().getUser(occupants.get(i).getValue(), consumer);
        }
        rotation = new Rotation().fromJSON(json.search("rotation"));
        showTaskDifficulty = json.valueOf("showTaskDifficulty");
        penalizeLateTasks = json.valueOf("penalizeLateTasks");
        return this;
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
