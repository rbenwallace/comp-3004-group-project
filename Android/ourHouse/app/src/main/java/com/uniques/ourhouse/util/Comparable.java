package com.uniques.ourhouse.util;

import androidx.annotation.NonNull;

@SuppressWarnings("unchecked")
public interface Comparable extends java.lang.Comparable {

    int DATE = 0;
    int STRING = 1;
    int COMPLEX = 2;

    int getCompareType();

    java.lang.Comparable getCompareObject();

    default int compareTo(@NonNull Object o) {
        if (!(o instanceof Comparable))
            return 0;

        Comparable c = (Comparable) o;

        if (getCompareObject() == null || c.getCompareObject() == null) {
            return 0;
        } else if (getCompareType() == COMPLEX) {
            return getCompareObject().compareTo(c);
        } else if (c.getCompareType() == COMPLEX) {
            return compareTo(c.getCompareObject());
        } else if (getCompareType() != c.getCompareType()) {
            return 0;
        } else {
//            System.out.println("[Compare] " + getCompareObject().toString() + " vs " + c.getCompareObject().toString());
            return getCompareObject().compareTo(c.getCompareObject());
        }
    }
}
