package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;
import com.uniques.ourhouse.util.Schedule;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.bson.types.ObjectId;

public abstract class ManageItem implements Model, Indexable, Observable {

    protected ObjectId manageItemId = new ObjectId();
    protected String name;
    //private UUID manageItemOwner;
    protected Schedule schedule;

    public ManageItem(){}

    public ManageItem(String name, Schedule schedule){
        this.name = name;
        this.schedule = schedule;
    }

    @NonNull
    @Override
    public ObjectId getId() {
        return manageItemId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    abstract String getType();

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
