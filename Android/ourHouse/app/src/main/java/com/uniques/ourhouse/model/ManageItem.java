package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.Schedule;

import org.bson.types.ObjectId;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ManageItem implements Model, Indexable, Observable {

    static final int DIFFICULTY_EASY = 1;
    static final int DIFFICULTY_MEDIUM = 2;
    static final int DIFFICULTY_HARD = 3;

    protected ObjectId manageItemId;
    protected ObjectId manageItemOwner;
    protected ObjectId manageItemHouse;
    protected String name;
    protected Schedule schedule;
    protected Date deletedDate;

    ManageItem(){}

    ManageItem(ObjectId idItem, ObjectId owner, ObjectId house, String name, Schedule schedule) {
        manageItemId = idItem;
        manageItemOwner = owner;
        manageItemHouse = house;
        this.name = name;
        this.schedule = schedule;
    }

    @NonNull
    @Override
    public ObjectId getId() {
        return manageItemId;
    }

    public ObjectId getOwnerId() {
        return manageItemOwner;
    }

    public ObjectId getHouseId() {
        return manageItemOwner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    abstract String getType();

    public abstract int getDifficulty();

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public ObjectId getCompareObject() {
        return manageItemId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Task) {
            return ((Task) obj).getId().equals(manageItemId);
        }
        return false;
    }
}
