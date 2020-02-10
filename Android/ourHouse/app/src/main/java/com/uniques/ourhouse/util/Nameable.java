package com.uniques.ourhouse.util;

public interface Nameable extends Model {

    String getName();

    void setName(String name);

    String getFancyName();

    String getShortName();
}
