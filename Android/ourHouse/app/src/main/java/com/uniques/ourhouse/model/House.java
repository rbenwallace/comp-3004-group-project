package com.uniques.ourhouse.model;

import android.util.Log;

import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class House implements Indexable, Observable {
    @NonNull
    private ObjectId houseId;
    @NonNull
    private String houseKey;
    @NonNull
    private ObjectId owner;
    @NonNull
    private String name;
    @NonNull
    private List<ObjectId> occupants;
    @NonNull
    private Rotation rotation;
    @NonNull
    private String password;

    private boolean showTaskDifficulty;
    private boolean penalizeLateTasks;

    private HashMap<ObjectId, Float> userPoints;
    private HashMap<ObjectId, Float> userAmountPaid;

    private HashMap<ObjectId, Integer> tasksCompleted;
    private ArrayList<String> userFees;

    private DatabaseLink myDatabase = Session.getSession().getDatabase();

    public House(@NonNull ObjectId id, @NonNull String houseKey, @NonNull ObjectId owner, @NonNull String name, @NonNull List<ObjectId> occupants, @NonNull House.Rotation rotation, @NonNull String password, boolean showTaskDifficulty, boolean penalizeLateTasks) {
        this.houseId = id;
        this.houseKey = houseKey;
        this.owner = owner;
        this.name = name;
        this.occupants = occupants;
        this.rotation = rotation;
        this.password = password;
        this.showTaskDifficulty = showTaskDifficulty;
        this.penalizeLateTasks = penalizeLateTasks;
    }

    public House(@NonNull String name, @NonNull ObjectId owner, @NonNull List<ObjectId> occupants, @NonNull House.Rotation rotation, @NonNull String password, boolean showTaskDifficulty, boolean penalizeLateTasks) {
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
        houseKey = "";
        owner = new ObjectId();
        name = "<untitled>";
        occupants = new ArrayList<>();
        rotation = new Rotation();
        password = "";
    }

//    public static House testHouse() {
//        Rotation rotation = new Rotation();
//        rotation.addUserToRotation(Session.getSession().getLoggedInUser());
//        rotation.addUserToRotation(new User("User", "A", "email1"));
//        rotation.addUserToRotation(new User("User", "B", "email2"));
//        rotation.addUserToRotation(new User("User", "C", "email3"));
//        House house = new House("Test House", Session.getSession().getLoggedInUser(),
//                new ArrayList<>(), rotation, "password", true, true);
//        return house;
//    }

    @NonNull
    @Override
    public ObjectId getId() {
        return houseId;
    }

    public String getKeyId() {
        return houseKey;
    }

    public HashMap<ObjectId, Float> getUserPoints() {
        return userPoints;
    }

    public HashMap<ObjectId, Float> getUserAmountPaid() {
        return userAmountPaid;
    }

    public HashMap<ObjectId, Integer> getTasksCompleted() {
        return tasksCompleted;
    }

    public ArrayList<String> getUserFees() {
        return userFees;
    }

    public boolean getShowTaskDifficulty() {
        return showTaskDifficulty;
    }

    public void setShowTaskDifficulty(boolean showTaskDifficulty) {
        this.showTaskDifficulty = showTaskDifficulty;
    }

    public boolean getPenalizeLateTasks() {
        return penalizeLateTasks;
    }

    public void setPenalizeLateTasks(boolean penalizeLateTasks) {
        this.penalizeLateTasks = penalizeLateTasks;
    }

    public void initHouseEvents() {
        userPoints = new HashMap<>();
        userAmountPaid = new HashMap<>();
        tasksCompleted = new HashMap<>();
        userFees = new ArrayList<>();
        for (ObjectId userId : occupants) {
            userPoints.put(userId, Float.valueOf("0.0"));
            userAmountPaid.put(userId, Float.valueOf("0.0"));
            tasksCompleted.put(userId, Integer.parseInt("0"));
        }
    }

    public void populateStats(int year, int month, ObjectId taskUser) {
        initHouseEvents();
        myDatabase.getAllEventsFromHouse(houseId, events -> {
            for (Event event : events) {
                if (event.getDateCompleted() != null) {
                    ObjectId eventUser = event.getAssignedTo();
                    int tempYear = event.getDateCompleted().getYear();
                    int tempMonth = event.getDateCompleted().getMonth();
                    if ((event.getType() == 0) && (tempMonth == month) && (tempYear == year)) {
                        myDatabase.getTask(event.getAssociatedTask(), task -> {
                            int completed = tasksCompleted.get(eventUser) + 1;
                            tasksCompleted.put(eventUser, completed);
                            if (showTaskDifficulty && penalizeLateTasks) {
                                if (event.getDueDate().after(event.getDateCompleted())) {
                                    float num = (float) (userPoints.get(eventUser) + task.getDifficulty());
                                    userPoints.put(eventUser, num);
                                } else {
                                    float num = (float) (userPoints.get(eventUser) + task.getDifficulty() * 0.5);
                                    userPoints.put(eventUser, num);
                                }
                            } else if (!showTaskDifficulty && penalizeLateTasks) {
                                if (event.getDueDate().after(event.getDateCompleted())) {
                                    float num = (float) (userPoints.get(eventUser) + 1.0);
                                    userPoints.put(eventUser, num);
                                } else {
                                    float num = (float) (userPoints.get(eventUser) + 0.5);
                                    userPoints.put(eventUser, num);
                                }
                            } else {
                                float num = (float) (userPoints.get(eventUser) + 1.0);
                                userPoints.put(eventUser, num);
                            }
                        });
                    } else if (event.getType() == 1 && (tempMonth == month) && (tempYear == year)) {
                        myDatabase.getFee(event.getAssociatedTask(), fee -> {
                            String userFee = "Amt: " + String.valueOf(fee.getAmount()) + " - " + fee.getName();
                            if (eventUser.equals(taskUser)) {
                                userFees.add(userFee);
                            }
                            float num = (float) (userAmountPaid.get(eventUser) + fee.getAmount());
                            userAmountPaid.put(eventUser, num);
                        });
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public List<ObjectId> getOccupants() {
        return occupants;
    }

    public void addOccupant(User occupant) {
        occupants.add(occupant.getId());
        rotation.getRotation().add(occupant.getId());
    }

    public void removeOccupant(User occupant) {
        if (occupants.contains(occupant.getId())) {
            occupants.remove(occupant.getId());
            rotation.rotation.remove(occupant.getId());
        } else {
            Log.d("House", "No user in this House");
        }
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @NonNull
    public ObjectId getOwner() {
        return owner;
    }

    public void setOwner(@NonNull ObjectId owner) {
        this.owner = owner;
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
        return "(" + houseId.toString() + ")[" + name + "] owner=" + owner +
                " difficultyOn=" + showTaskDifficulty + " penalize=" + penalizeLateTasks +
                " occupants=" + occupants + " rotation " + rotation.rotation;
    }

    @NonNull
    public House.Rotation getRotation() {
        return rotation;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof House) {
            return ((House) obj).houseId.equals(houseId);
        }
        return false;
    }

    public static class Rotation implements Model, Iterable<ObjectId> {
        private ArrayList<ObjectId> rotation;

        public Rotation() {
            rotation = new ArrayList<>();
        }

        public ArrayList<ObjectId> getRotation() {
            return rotation;
        }

        public void setRotation(ArrayList<ObjectId> rotation) {
            this.rotation = rotation;
        }

        @Override
        public String consoleFormat(String prefix) {
            return rotation.toString();
        }

        public void addUserToRotation(User user) {
            rotation.add(user.getId());
        }

        @Override
        public JSONElement toJSON() {
            EasyJSON json = EasyJSON.create();
            json.getRootNode().setType(SafeJSONElementType.ARRAY);
            for (ObjectId userId : rotation) json.putPrimitive(userId.toString());
            return json.getRootNode();
        }

        @Override
        public void fromJSON(JSONElement json, Consumer consumer) {
            rotation = new ArrayList<>();
            for (JSONElement element : json) rotation.add(new ObjectId(element.<String>getValue()));
            consumer.accept(this);
        }

        @NonNull
        @Override
        public Iterator<ObjectId> iterator() {
            return new PriorityQueue<>(rotation).iterator();
        }
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("_id", houseId.toString());
        json.putPrimitive("houseId", houseId.toString());
        json.putPrimitive("houseKey", houseKey);
        json.putPrimitive("owner", owner.toString());
        json.putPrimitive("name", name);
        json.putArray("occupants");
        for (ObjectId occupantId : occupants) {
            json.search("occupants").putPrimitive(occupantId.toString());
        }
        json.putElement("rotation", rotation.toJSON());
        json.putPrimitive("password", password);
        json.putPrimitive("showTaskDifficulty", showTaskDifficulty);
        json.putPrimitive("penalizeLateTasks", penalizeLateTasks);
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        House that = this;
        houseId = new ObjectId(json.<String>valueOf("houseId"));
        houseKey = json.valueOf("houseKey");
        owner = new ObjectId(json.<String>valueOf("owner"));
        name = json.valueOf("name");
        occupants = new ArrayList<>();
        for (JSONElement element : json.search("occupants")) {
            occupants.add(new ObjectId(element.<String>getValue()));
        }
        password = json.valueOf("password");
        showTaskDifficulty = json.valueOf("showTaskDifficulty");
        penalizeLateTasks = json.valueOf("penalizeLateTasks");
        new Rotation().fromJSON(json.search("rotation"), rotation -> {
            that.rotation = (Rotation) rotation;
            consumer.accept(that);
        });
    }

    @NonNull
    @Override
    public String toString() {
        return consoleFormat("House ");
    }
}
