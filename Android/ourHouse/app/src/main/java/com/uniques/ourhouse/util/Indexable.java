package com.uniques.ourhouse.util;


import java.util.UUID;

import androidx.annotation.NonNull;

public interface Indexable extends Model {

    @NonNull
    UUID getId();
}
