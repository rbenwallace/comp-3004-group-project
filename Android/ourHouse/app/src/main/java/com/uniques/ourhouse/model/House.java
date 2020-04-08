package com.uniques.ourhouse.model;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;
import android.util.Log;

import com.uniques.ourhouse.controller.FeedCard;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;
import com.uniques.ourhouse.util.easyjson.SafeJSONElementType;

import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class House implements Indexable, Observable {
    public static final String TAG = "HouseModel";
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
    private Consumer<Task> taskFiller;
    private Consumer<Fee> feeFiller;
    ObjectId tempId;

    private HashMap<ObjectId, Float> userPoints;
    private HashMap<ObjectId, Float> userAmountPaid;

    private HashMap<ObjectId, Integer> tasksCompleted;
    private ArrayList<String> userFees;
    private ArrayList<Task> gatheredTasks;
    private ArrayList<Fee> gatheredFees;
    ArrayList<Event> taskEvents, feeEvents;
    private int count;

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
        taskEvents = new ArrayList<>();
        feeEvents = new ArrayList<>();
        for (ObjectId userId : occupants) {
            userPoints.put(userId, Float.valueOf("0.0"));
            userAmountPaid.put(userId, Float.valueOf("0.0"));
            tasksCompleted.put(userId, Integer.parseInt("0"));
        }
    }

    public void populateStats(int year, int month, ObjectId taskUser, Consumer<Boolean> returnStats){
        Log.d("TestingSTuff", "Inside");
        initHouseEvents();
        gatheredFees = new ArrayList<>();
        gatheredTasks = new ArrayList<>();
        myDatabase.getAllEventsFromHouse(houseId, events -> {
            Log.d("TestingSTuff", "Leaving to gather tasks");
            gatheringTasks(events, year, month, taskUser, returnStats);
        });
    }

    public void gatheringTasks(List<Event> events, int year, int month, ObjectId taskUser, Consumer<Boolean> returnStats) {
        if (events.isEmpty()) gatheringFees(new ArrayList<Event>(), year, month, taskUser,returnStats);
        if (taskFiller != null) {
            return;
        }
        ArrayList<Event> eventsToSendTemp = new ArrayList<>();
        ArrayList<Event> eventsToKeepTemp = new ArrayList<>();
        for(Event event : events){
            if(event.getDateCompleted() != null){
                String strYear = (String) DateFormat.format("yyyy", event.getDateCompleted());
                int tempYear = Integer.parseInt(strYear);
                int tempMonth = event.getDateCompleted().getMonth();
                if((tempMonth == month) && (tempYear == year)) {
                    if(event.getType() != 0){
                        feeEvents.add(event);
                        eventsToSendTemp.add(event);
                    }
                    else {
                        taskEvents.add(event);
                        eventsToKeepTemp.add(event);
                    }
                }
            }
        }
        if (eventsToKeepTemp.isEmpty()) {
            gatheringFees(eventsToSendTemp, year, month, taskUser, returnStats);
            return;
        }
        taskFiller = task -> {
            if (task != null) {
                gatheredTasks.add(task);
            }
            if (eventsToKeepTemp.isEmpty()) {
                taskFiller = null;
                gatheringFees(eventsToSendTemp,  year, month, taskUser,returnStats);
            } else {
                myDatabase.getTask(eventsToKeepTemp.remove(0).getAssociatedTask(), taskFiller);
            }
        };
        myDatabase.getTask(eventsToKeepTemp.remove(0).getAssociatedTask(), taskFiller);
    }
    public void gatheringFees(ArrayList<Event> events, int year, int month, ObjectId taskUser, Consumer<Boolean> returnStats) {
        if (events.isEmpty()) doneCalculating(new ArrayList<Event>(), year, month, taskUser,returnStats);
        if (feeFiller != null) {
            return;
        }
        if (events.isEmpty()) {
            doneCalculating(events, year, month, taskUser, returnStats);
            return;
        }
        feeFiller = fee -> {
            if (fee != null) {
                gatheredFees.add(fee);
            }
            if (events.isEmpty()) {
                feeFiller = null;
                tempId = null;
                doneCalculating(events, year, month, taskUser, returnStats);
            } else {
                myDatabase.getFee(events.remove(0).getAssociatedTask(), feeFiller);
            }
        };
        myDatabase.getFee(events.remove(0).getAssociatedTask(), feeFiller);
    }

    private void doneCalculating(ArrayList<Event> events, int year, int month, ObjectId taskUser, Consumer<Boolean> returnStats) {
        Log.d("all gathered fees", gatheredFees.toString());
        Log.d("all gathered tasks" , gatheredTasks.toString());
        for(int i = 0; i < gatheredTasks.size(); i++) {
            int completed =  tasksCompleted.get(taskEvents.get(i).getAssignedTo()) + 1;
            tasksCompleted.put(taskEvents.get(i).getAssignedTo(), completed);
            if(showTaskDifficulty && penalizeLateTasks){
                if(taskEvents.get(i).getDueDate().after(taskEvents.get(i).getDateCompleted())){
                    float num = (float) (userPoints.get(taskEvents.get(i).getAssignedTo()) + gatheredTasks.get(i).getDifficulty());
                    userPoints.put(taskEvents.get(i).getAssignedTo(), num);
                }
                else {
                    float num = (float) (userPoints.get(taskEvents.get(i).getAssignedTo()) + (gatheredTasks.get(i).getDifficulty() * 0.5));
                    userPoints.put(taskEvents.get(i).getAssignedTo(), num);
                }
            }
            else if(!showTaskDifficulty && penalizeLateTasks){
                if(taskEvents.get(i).getDueDate().after(taskEvents.get(i).getDateCompleted())){
                    float num = (float) (userPoints.get(taskEvents.get(i).getAssignedTo()) + 1.0);
                    userPoints.put(taskEvents.get(i).getAssignedTo(), num);
                }
                else {
                    float num = (float) (userPoints.get(taskEvents.get(i).getAssignedTo()) + 0.5);
                    userPoints.put(taskEvents.get(i).getAssignedTo(), num);
                }
            }
            else{
                if (showTaskDifficulty) {
                    float num = (float) (userPoints.get(taskEvents.get(i).getAssignedTo()) + gatheredTasks.get(i).getDifficulty());
                    userPoints.put(taskEvents.get(i).getAssignedTo(), num);
                }
                else{
                    float num = (float) (userPoints.get(taskEvents.get(i).getAssignedTo()) + 1.0);
                    userPoints.put(taskEvents.get(i).getAssignedTo(), num);
                }
            }
        }
        for(int i = 0; i < gatheredFees.size(); i++) {
            String userFee = "Amt: " + String.valueOf(gatheredFees.get(i).getAmount()) + " - " + gatheredFees.get(i).getName();
            if(feeEvents.get(i).getAssignedTo().equals(taskUser)){
                userFees.add(userFee);
            }
            float num = (float) (userAmountPaid.get(feeEvents.get(i).getAssignedTo()) + gatheredFees.get(i).getAmount());
            userAmountPaid.put(feeEvents.get(i).getAssignedTo(), num);
        }
        returnStats.accept(true);
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

    public void setHouseKey(@NonNull String houseKey) {
        this.houseKey = houseKey;
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
