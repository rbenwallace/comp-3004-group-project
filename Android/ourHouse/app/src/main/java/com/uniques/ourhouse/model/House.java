package com.uniques.ourhouse.model;

import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class House implements Model, Indexable, Observable {

    private UUID houseId = UUID.randomUUID();
    private String name;
    private User[] occupants;
    private Rotation rotation;
    private boolean showTaskDifficulty;
    private boolean penalizeLateTasks;

    @NonNull
    @Override
    public UUID getId() {
        return houseId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public User[] getOccupants() {
        return occupants;
    }

    public void addOccupant(User occupant) {
        User[] occupants = new User[this.occupants.length + 1];
        if (this.occupants.length > 0) {
            System.arraycopy(this.occupants, 0, occupants, 0, this.occupants.length);
        }
        occupants[occupants.length - 1] = occupant;
        this.occupants = occupants;
    }

    public void removeOccupant(User occupant) {
        if (occupants.length == 0) {
            return;
        }
        int index = -1;
        for (int i = 0; i < this.occupants.length; ++i) {
            if (this.occupants[i].equals(occupant)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return;
        }
        User[] occupants = new User[this.occupants.length - 1];
        System.arraycopy(this.occupants, 0, occupants, 0, index);
        System.arraycopy(this.occupants, index + 1, occupants, index, this.occupants.length - index + 1);
        this.occupants = occupants;
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
    public House fromJSON(JSONElement json) {
        houseId = UUID.fromString(json.valueOf("houseId"));
        name = json.valueOf("name");

        List<JSONElement> occupants = json.search("occupants").getChildren();
        int numOccupants = occupants.size();
        this.occupants = new User[numOccupants];
        for (int i = 0; i < numOccupants; ++i) {
            this.occupants[i] =
                    Session.getSession().getDatabase().getUser(occupants.get(i).getValue());
        }

        rotation = new Rotation().fromJSON(json.search("rotation"));
        showTaskDifficulty = json.valueOf("showTaskDifficulty");
        penalizeLateTasks = json.valueOf("penalizeLateTasks");
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof House) {
            return ((House) obj).houseId.equals(houseId);
        }
        return false;
    }

    public static class Rotation implements Model {

        private User[] rotation;

        @Override
        public String consoleFormat(String prefix) {
            return Arrays.toString(rotation);
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
            List<JSONElement> users = json.getChildren();
            int usersLength = users.size();
            rotation = new User[usersLength];
            for (int i = 0; i < usersLength; ++i) {
                rotation[i] = Session.getSession().getDatabase().getUser(UUID.fromString(users.get(i).getValue()));
            }
            return null;
        }
    }
}
