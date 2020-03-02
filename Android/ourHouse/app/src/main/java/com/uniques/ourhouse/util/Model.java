package com.uniques.ourhouse.util;

import com.uniques.ourhouse.util.easyjson.JSONElement;

import java.util.function.Consumer;

public interface Model {

    String consoleFormat(String prefix);

    JSONElement toJSON();

    void fromJSON(JSONElement json, Consumer consumer);
}
