package com.uniques.ourhouse.util;

public interface Nameable {

    String getName();

    void setName(String name);

    default boolean isReadOnly() {
        return false;
    }

}
