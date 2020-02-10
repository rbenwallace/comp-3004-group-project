package com.uniques.ourhouse.util;


import java.util.UUID;

import androidx.annotation.NonNull;

public interface IndexableModel extends Model {

    @NonNull
    UUID getId();
}
