package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Comparable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.Date;

public class Event implements Model, Observable, Comparable {
    private String title;
    private String assignedTo;
    private Date dueDate;
    private Date dateCompleted;

    public static Event[] testEvents() {
        return new Event[]{new Event("Recycling Bin Day", new Date(), "Ben"),
                new Event("Dishes", new Date(), "Victor"),
                new Event("Buy us a TV", new Date(), "Seb")};
    }

    public Event(String title, Date dueDate, String assignedTo) {
        this.title = title;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
        dateCompleted = new Date();
    }

    @Override
    public int getCompareType() {
        return Comparable.DATE;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return dateCompleted;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
        this.title = name;
    }

    public String consoleFormat(String prefix) {
        return title + ": " + assignedTo;
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("title", title);
        json.putPrimitive("assignedTo", assignedTo);
        json.putPrimitive("dueDate", dueDate.getTime());
        json.putPrimitive("dateCompleted", dateCompleted.getTime());
        return json.getRootNode();
    }

    @Override
    public Object fromJSON(JSONElement json) {
        title = json.valueOf("title");
        assignedTo = json.valueOf("assignedTo");
        dueDate = new Date((long) json.valueOf("dueDate"));
        dateCompleted = new Date((long) json.valueOf("dateCompleted"));
        return this;
    }
}
