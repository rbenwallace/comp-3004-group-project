package com.uniques.ourhouse.util;


import java.util.UUID;

import androidx.annotation.NonNull;

import org.bson.types.ObjectId;

public interface Indexable extends Model {

    @NonNull
    ObjectId getId();
}
