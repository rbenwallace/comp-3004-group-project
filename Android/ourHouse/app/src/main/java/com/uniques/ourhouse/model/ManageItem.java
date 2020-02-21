package com.uniques.ourhouse.model;

import com.uniques.ourhouse.util.BetterSchedule;
import com.uniques.ourhouse.util.Indexable;
import com.uniques.ourhouse.util.Model;
import com.uniques.ourhouse.util.Observable;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ManageItem implements Model, Indexable, Observable {

    protected UUID manageItemId = UUID.randomUUID();
    protected String name;
    //private UUID manageItemOwner;
    protected BetterSchedule schedule;

    public ManageItem(){}

    public ManageItem(String name, BetterSchedule schedule){
        this.name = name;
        this.schedule = schedule;
    }

    @NonNull
    @Override
    public UUID getId() {
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
    public UUID getCompareObject() {
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
