package com.uniques.ourhouse.util;

import com.uniques.ourhouse.util.easyjson.JSONElement;

public interface Model {

    String consoleFormat(String prefix);

    JSONElement toJSON();

    Object fromJSON(JSONElement json);
}
