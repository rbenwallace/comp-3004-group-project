package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Event implements Observable, Indexable {
    private static final int TYPE_UNDEFINED = -1;
    public static final int TYPE_TASK = 0;
    public static final int TYPE_FEE = 1;

    @NonNull
    private ObjectId eventId;
    @NonNull
    private Integer type;
    @NonNull
    private ObjectId assignedHouse;
    @NonNull
    private ObjectId associatedTask;
    @NonNull
    private String title;
    @NonNull
    private ObjectId assignedTo;
    @NonNull
    private Date dueDate;
    private Date dateCompleted;

//    public static Event[] testEvents() {
//        long now = new Date().getTime() - 80000000;
//        Event r = new Event("Recycling Bin Day", new Date(now), new User("Ben", "", ""), new ObjectId(), new ObjectId());
//        r.setDateCompleted(new Date(now + 76543210));
//        return new Event[]{
//                new Event("Dishes", new Date(now + 1000000), new User("Victor", "", ""), new ObjectId(), new ObjectId()),
//                r,
//                new Event("Buy us a TV", new Date(now + 2000000), new User("Seb", "", ""), new ObjectId(), new ObjectId())};
//    }
//
//    public static Event[] testEventsWithId() {
//        long now = new Date().getTime() - 80000000;
//        Event r = new Event(new ObjectId(), "Recycling Bin Day", new User("Ben", "", ""), new ObjectId(), new ObjectId(), new Date(now), null);
//        r.setDateCompleted(new Date(now + 76543210));
//        return new Event[]{
//                new Event("Dishes", new User("Victor", "", ""), new ObjectId(), new ObjectId(), new Date(now + 1000000), null),
//                r,
//                new Event("Buy us a TV", new User("Seb", "", ""), new ObjectId(), new ObjectId(), new Date(now + 2000000), null)};
//    }

    public Event(@NonNull Integer type, @NonNull String title, @NonNull Date dueDate, @NonNull ObjectId assignedTo, @NonNull ObjectId assignedHouse, @NonNull ObjectId associatedTask) {
        this.eventId = new ObjectId();
        this.type = type;
        this.title = title;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        this.assignedHouse = assignedHouse;
        this.associatedTask = associatedTask;
        // + (new Random().nextBoolean() ? 1000 : -1000)
//        dateCompleted = new Date(dueDate.getTime());
    }

    public Event() {
        eventId = new ObjectId();
        type = TYPE_UNDEFINED;
        assignedHouse = new ObjectId();
        associatedTask = new ObjectId();
        title = "< untitled event >";
        assignedTo = new ObjectId();
        dueDate = new Date();
    }

    @NonNull
    public Integer getType(){
        return type;
    }

    @NonNull
    @Override
    public ObjectId getId() {
        return eventId;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
        this.title = name;
    }

    @NonNull
    public ObjectId getAssignedTo() {
        return assignedTo;
    }

    @NonNull
    public ObjectId getAssociatedTask() {
        return associatedTask;
    }

    @NonNull
    public Date getDueDate() {
        return dueDate;
    }

    public Date getDateCompleted() {
        return dateCompleted;
    }

    public boolean isLate() {
        return dateCompleted != null ?
                dateCompleted.after(dueDate) : System.currentTimeMillis() > dueDate.getTime();
    }

    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    @Override
    public int getCompareType() {
        return DATE;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return dateCompleted;
    }

    public String consoleFormat(String prefix) {
        return title + " (> usr " + assignedTo + "), due (" + dueDate + "), completed (" + dateCompleted + ")";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("_id", eventId.toString());
        json.putPrimitive("type", type);
        json.putPrimitive("title", title);
        json.putPrimitive("assignedTo", assignedTo.toString());
        json.putPrimitive("assignedHouse", assignedHouse.toString());
        json.putPrimitive("associatedTask", associatedTask.toString());
        json.putPrimitive("dueDate", dueDate.getTime());
        if (dateCompleted != null) {
            json.putPrimitive("dateCompleted", dateCompleted.getTime());
        }
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        eventId = new ObjectId(json.<String>valueOf("_id"));
        type = json.valueOf("type");
        title = json.valueOf("title");
        assignedTo = new ObjectId(json.<String>valueOf("assignedTo"));
        assignedHouse = new ObjectId(json.<String>valueOf("assignedHouse"));
        associatedTask = new ObjectId(json.<String>valueOf("associatedTask"));
        dueDate = new Date((long) json.valueOf("dueDate"));
        if (json.elementExists("dateCompleted")) {
            dateCompleted = new Date((long) json.valueOf("dateCompleted"));
        }
        consumer.accept(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Event && ((Event) obj).eventId.equals(eventId);
    }

    @NonNull
    @Override
    public String toString() {
        return consoleFormat("[Event] ");
    }
}
