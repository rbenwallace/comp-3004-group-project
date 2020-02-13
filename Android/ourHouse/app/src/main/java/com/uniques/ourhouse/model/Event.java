package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.Date;
import java.util.UUID;

import androidx.annotation.NonNull;

public class Event implements Model, Observable, Indexable {

    private UUID eventId = UUID.randomUUID();
    private String title;
    private String assignedTo;
    private Date dueDate;
    private Date dateCompleted;

    public static Event[] testEvents() {
        return new Event[]{new Event("Recycling Bin Day", new Date(), "Ben"),
                new Event("Dishes", new Date(), "Victor"),
                new Event("Buy us a TV", new Date(), "Seb")};
    }

    public Event() {
    }

    public Event(String title, Date dueDate, String assignedTo) {
        this.title = title;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        dateCompleted = new Date();
    }

    @NonNull
    @Override
    public UUID getId() {
        return null;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
        this.title = name;
    }

    @Override
    public int getCompareType() {
        return Comparable.DATE;
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
        json.putPrimitive("assignedTo", assignedTo);
        json.putPrimitive("dueDate", dueDate.getTime());
        json.putPrimitive("dateCompleted", dateCompleted.getTime());
        return json.getRootNode();
    }

    @Override
    public Event fromJSON(JSONElement json) {
        eventId = UUID.fromString(json.valueOf("eventId"));
        title = json.valueOf("title");
        assignedTo = json.valueOf("assignedTo");
        dueDate = new Date((long) json.valueOf("dueDate"));
        dateCompleted = new Date((long) json.valueOf("dateCompleted"));
        return this;
    }
}
