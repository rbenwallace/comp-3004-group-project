package com.uniques.ourhouse.model;

import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Event implements Model, Observable, Indexable {

    public static final String EVENT_COLLECTION = "Events";
    private ObjectId eventId;
    private String title;
    private User assignedTo;
    private Date dueDate;
    private Date dateCompleted;

    public static Event[] testEvents() {
        long now = new Date().getTime() - 80000000;
        Event r = new Event("Recycling Bin Day", new Date(now), new User("Ben", "", ""));
        r.setDateCompleted(new Date(now + 76543210));
        return new Event[]{
                new Event("Dishes", new Date(now + 1000000), new User("Victor", "", "")),
                r,
                new Event("Buy us a TV", new Date(now + 2000000), new User("Seb", "", ""))};
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

    public boolean isLate() {
        return dateCompleted.after(dueDate);
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
        return title + ": " + assignedTo;
    }

    public Document toBsonDocument() {
        final Document asDoc = new Document();
        if (eventId != null)
            asDoc.put("_id", eventId);
        asDoc.put("title", title);
        asDoc.put("assignedTo", assignedTo.toBsonDocument());
        asDoc.put("dueDate", dueDate);
        asDoc.put("dateCompleted", dateCompleted);
        return asDoc;
    }

    public static Event fromBsonDocument(final Document doc) {
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
        json.putPrimitive("_id", eventId.toString());
        json.putPrimitive("title", title);
        json.putPrimitive("assignedTo", assignedTo.getId().toString());
        json.putPrimitive("dueDate", dueDate.getTime());
        json.putPrimitive("dateCompleted", dateCompleted.getTime());
        return json.getRootNode();
    }

    @Override
    public void fromJSON(JSONElement json, Consumer consumer) {
        Event that = this;
        Session.getSession().getDatabase().getUser(new ObjectId(json.<String>valueOf("assignedTo")), user -> {
            eventId = new ObjectId(json.<String>valueOf("eventId"));
            title = json.valueOf("title");
            assignedTo = user;
            dueDate = new Date((long) json.valueOf("dueDate"));
            dateCompleted = new Date((long) json.valueOf("dateCompleted"));
            consumer.accept(that);
        });
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Event && ((Event) obj).eventId.equals(eventId);
    }
}
