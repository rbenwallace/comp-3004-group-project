package com.uniques.ourhouse.util;

import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.JSONElement;

public class Location implements Model {
    private String text;
    private android.location.Location actualLocation;

    public Location() {
        text = "";
    }

    public Location(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String consoleFormat(String prefix) {
        return text;
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("text");
        //TODO save/load actual location
        return json.getRootNode();
    }

    @Override
    public Location fromJSON(JSONElement json) {
        text = json.stringValueOf("","text");
        return this;
    }
}
