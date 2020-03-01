package com.uniques.ourhouse.model;

import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Event implements Model, Observable, Indexable {
    public static final String EVENT_COLLECTION = "Events";
    private ObjectId eventId;
    private String title;
    private User assignedTo;
    private Date dueDate;
    private Date dateCompleted;

    public static Event[] testEvents() {
        long now = new Date().getTime();
        return new Event[]{
                new Event("Dishes", new Date(now + 1000000), new User("Victor", "", "", 0)),
                new Event("Recycling Bin Day", new Date(now), new User("Ben", "", "", 0)),
                new Event("Buy us a TV", new Date(now + 2000000), new User("Seb", "", "", 0))};
    }

    public Event(ObjectId eventId, String title, User assignedTo, Date dueDate, Date dateCompleted) {
        this.eventId = eventId;
        this.title = title;
        this.assignedTo = assignedTo;
        this.dueDate = dueDate;
        this.dateCompleted = dateCompleted;
    }
    public Event(String title, User assignedTo, Date dueDate, Date dateCompleted) {
        this.eventId = new ObjectId();
        this.title = title;
        this.assignedTo = assignedTo;
        this.dueDate = dueDate;
        this.dateCompleted = dateCompleted;
    }

    public Event(String title, Date dueDate, User assignedTo) {
        this.title = title;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        // + (new Random().nextBoolean() ? 1000 : -1000)
        dateCompleted = new Date(dueDate.getTime());
    }

    public Event() {
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

    public User getAssignedTo() {
        return assignedTo;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Date getDateCompleted() {
        return dateCompleted;
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
        return title + ": " + assignedTo;
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        if(eventId != null)
            asDoc.put("_id", eventId);
        asDoc.put("title", title);
        asDoc.put("assignedTo", assignedTo.toBsonDocument());
        asDoc.put("dueDate", dueDate);
        asDoc.put("dateCompleted", dateCompleted);
        return asDoc;
    }

    public static Event fromBsonDocument(final Document doc){
        User assignedTo = User.fromBsonDocument((Document) doc.get("assignedTo"));
        return new Event(
                (ObjectId) doc.get("_id"),
                doc.getString("title"),
                assignedTo,
                doc.getDate("dueDate"),
                doc.getDate("dateCompleted")
        );
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("eventId", eventId);
        json.putPrimitive("title", title);
        json.putPrimitive("assignedTo", assignedTo.getId().toString());
        json.putPrimitive("dueDate", dueDate.getTime());
        json.putPrimitive("dateCompleted", dateCompleted.getTime());
        return json.getRootNode();
    }

    @Override
    public Event fromJSON(JSONElement json) {
        Consumer<User> consumer = returnedObject -> {
            assignedTo = returnedObject;
        };
        eventId = (ObjectId) json.valueOf("eventId");
        title = json.valueOf("title");
        Session.getSession().getDatabase().getUser((ObjectId) json.valueOf("assignedTo"), consumer);
        dueDate = new Date((long) json.valueOf("dueDate"));
        dateCompleted = new Date((long) json.valueOf("dateCompleted"));
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Event && ((Event) obj).eventId.equals(eventId);
    }
}
