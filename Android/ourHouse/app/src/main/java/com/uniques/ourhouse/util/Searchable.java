package com.uniques.ourhouse.util;

import java.util.UUID;

public interface Searchable<T> extends Model {

    T search(UUID query);
}
