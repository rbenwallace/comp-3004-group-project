package com.uniques.ourhouse.util;

public interface ReadOnlyNameable extends Nameable {

    String getName();

    default void setName(String name) {
        throw new RuntimeException("nameable object is read-only");
    }
}
