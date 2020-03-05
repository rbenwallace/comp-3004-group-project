package com.uniques.ourhouse.util;

import java.util.Iterator;

public class OneItemIterator<T> implements Iterator<T> {
    private T value;

    public OneItemIterator(T value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return value != null;
    }

    @Override
    public T next() {
        T val = value;
        value = null;
        return val;
    }
}
