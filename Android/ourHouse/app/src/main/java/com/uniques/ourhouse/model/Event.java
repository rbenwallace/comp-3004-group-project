package com.uniques.ourhouse.model;

import com.uniques.ourhouse.session.Session;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.Date;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Event implements Model, Observable, Indexable {

    private UUID eventId = UUID.randomUUID();
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

    public Event() {
    }

    public Event(String title, Date dueDate, User assignedTo) {
        this.title = title;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        // + (new Random().nextBoolean() ? 1000 : -1000)
        dateCompleted = new Date(dueDate.getTime());
    }

    @NonNull
    @Override
    public UUID getId() {
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
        eventId = UUID.fromString(json.valueOf("eventId"));
        title = json.valueOf("title");
        assignedTo = Session.getSession().getDatabase().getUser(UUID.fromString(json.valueOf("assignedTo")));
        dueDate = new Date((long) json.valueOf("dueDate"));
        dateCompleted = new Date((long) json.valueOf("dateCompleted"));
        return this;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Event && ((Event) obj).eventId.equals(eventId);
    }
}
