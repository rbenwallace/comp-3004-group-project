package com.uniques.ourhouse.util.easyjson;

import com.uniques.ourhouse.util.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class JSONElement implements Iterable<JSONElement> {
    private EasyJSON easyJSONStructure;
    private JSONElement parent;
    private JSONElementType type;
    private ArrayList<JSONElement> children = new ArrayList<>();
    private String key;
    private Object value;

    JSONElement(EasyJSON easyJSONStructure, JSONElement parent, JSONElementType type, String key, Object value) {
        this.easyJSONStructure = easyJSONStructure;
        this.parent = parent;
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public EasyJSON getEasyJSONStructure() {
        return easyJSONStructure;
    }

    public JSONElement getParent() {
        return parent;
    }

    public JSONElementType getType() {
        return type;
    }

    public void setType(SafeJSONElementType type) {
        this.type = type.getRealType();
    }

    public ArrayList<JSONElement> getChildren() {
        return children;
    }

    public String getKey() {
        return key;
    }

    public <T> T getValue() {
        return (T) value;
    }

    public JSONElement putElement(JSONElement jsonElement) {
        return putElement(jsonElement.key, jsonElement);
    }

    public void putElement(JSONElement... elements) {
        for (JSONElement element : elements) {
            putElement(element.key, element);
        }
    }

    public JSONElement putElement(String key, JSONElement jsonElement) {
        Object[] deconstructedKey = deconstructKey(key);
        if (deconstructedKey[0] != this) {
            return ((JSONElement) deconstructedKey[0]).putElement((String) deconstructedKey[1], jsonElement);
        }
        switch (jsonElement.type) {
            case PRIMITIVE:
                return putPrimitive(key, jsonElement);
            case ARRAY:
                return putArray(key, jsonElement);
            case STRUCTURE:
                return putStructure(key, jsonElement);
            case ROOT:
                return putStructure(null, jsonElement);
        }
        return null;
    }

    public JSONElement putPrimitive(Object value) {
        Object[] deconstructedKey = deconstructKey(key);
        if (deconstructedKey[0] != this) {
            return ((JSONElement) deconstructedKey[0]).putPrimitive((String) deconstructedKey[1], value);
        }
        JSONElement element;
        if (value instanceof JSONElement) {
            element = (JSONElement) value;
            element.parent = this;
        } else {
            element = new JSONElement(easyJSONStructure, this, JSONElementType.PRIMITIVE, null, value);
        }
        children.add(element);
        return element;
    }

    @NonNull
    public JSONElement putPrimitive(String key, Object value) {
        Object[] deconstructedKey = deconstructKey(key);
        if (deconstructedKey[0] != this) {
            return ((JSONElement) deconstructedKey[0]).putPrimitive((String) deconstructedKey[1], value);
        }
        JSONElement search = search(key);
        if (search == null) {
            JSONElement element;
            if (value instanceof JSONElement) {
                element = (JSONElement) value;
                claimElement(element);
            } else {
                element = new JSONElement(easyJSONStructure, this, JSONElementType.PRIMITIVE, key, value);
                children.add(element);
            }
            return element;
        } else {
            if (value instanceof JSONElement) {
                search.overwriteWith((JSONElement) value);
            } else {
                search.value = value;
            }
            return search;
        }
    }

    public JSONElement putStructure(String key) {
        Object[] deconstructedKey = deconstructKey(key);
        if (deconstructedKey[0] != this) {
            ((JSONElement) deconstructedKey[0]).putStructure((String) deconstructedKey[1], items);
        }
        JSONElement element = search(key);
        if (element == null) {
            element = new JSONElement(easyJSONStructure, this, JSONElementType.STRUCTURE, key, null);
            children.add(element);
        } else {
            throw new RuntimeException("EasyJSON: An element already exists with that key!");
        }
        return element;
    }

    public JSONElement putStructure(String key, EasyJSON easyJSON) {
        Object[] deconstructedKey = deconstructKey(key);
        if (deconstructedKey[0] != this) {
            return ((JSONElement) deconstructedKey[0]).putStructure((String) deconstructedKey[1], easyJSON);
        }
        easyJSON.getRootNode().easyJSONStructure = easyJSONStructure;
        easyJSON.getRootNode().parent = this;
        return putStructure(key, easyJSON.getRootNode());
    }

    public JSONElement putStructure(String key, JSONElement structure) {
        Object[] deconstructedKey = deconstructKey(key);
        if (deconstructedKey[0] != this) {
            return ((JSONElement) deconstructedKey[0]).putStructure((String) deconstructedKey[1], structure);
        }
        JSONElement searchResult = search(key);
        if (searchResult == null) {
            structure.type = JSONElementType.STRUCTURE;
            structure.key = key;
            claimElement(structure);
            return structure;
        } else {
            return searchResult.overwriteWith(structure);
        }
    }

    public JSONElement putArray(String key, Object... items) {
        Object[] deconstructedKey = deconstructKey(key);
        if (deconstructedKey[0] != this) {
            return ((JSONElement) deconstructedKey[0]).putArray((String) deconstructedKey[1], items);
        }
        JSONElement search = search(key);
        if (search == null || search.type != JSONElementType.ARRAY) {
            JSONElement element = new JSONElement(easyJSONStructure, this, JSONElementType.ARRAY, key, null);
            for (Object item : items) {
                if (item instanceof JSONElement) {
                    JSONElement itemElement = (JSONElement) item;
                    element.putElement(itemElement.getKey(), itemElement);
                } else {
                    element.putPrimitive(item);
                }
            }
            children.add(element);
            return element;
        } else {
            for (Object item : items) {
                if (item instanceof JSONElement) {
                    JSONElement itemElement = (JSONElement) item;
                    search.putElement(itemElement.getKey(), itemElement);
                } else {
                    search.putPrimitive(item);
                }
            }
            return search;
        }
    }

    public void removeElement(String... location) {
        JSONElement element = search(location);
        if (element != null) {
            element.getParent().children.remove(element);
        }
    }

    public boolean elementExists(String... location) {
        return search(location) != null;
    }

    /**
     * Will deconstruct simple & complex location keys (e.g "a.b.c") to return both the
     * suitable parent element and the final key that should be used to add the child element.
     * <br/><br/>
     * In the case of a simple key, the suitable parent is 'this' and the final key === key (param)
     * <br/><br/>
     * In the case of a complex key, the suitable parent will never be 'this' and the final key !== key (param)
     * <br/><br/>
     * Please be mindful of this when using this method.
     * @param key simple/complex key you want to deconstruct
     * @return Object[] {suitable_parent: JSONElement, final_key: String}
     * @throws RuntimeException if any part (before/after '.') of a complex key has < 1 character
     */
    public Object[] deconstructKey(String key) {
        JSONElement result = this;
        String finalKey = key;
        if (key.contains(Pattern.quote("."))) {
            String[] parts = key.split(Pattern.quote("."));
            for (int i = 0; i < parts.length; ++i) {
                String part = parts[i];
                if (i == parts.length - 1) {
                    finalKey = part;
                    break;
                }
                if (part.length() == 0) {
                    throw new RuntimeException("EasyJSON: '" + key + "' is an invalid complex location key. "
                            + "When using complex location keys (e.g 'a.b.c'), all parts before and after a '.' must contain >= 1 character!");
                }
                JSONElement innerSearch = result.search(part);
                if (innerSearch == null) {
                    result = result.putStructure(part);
                } else {
                    result = innerSearch;
                }
            }
        }
        return new Object[] {result, finalKey};
    }

    public JSONElement search(String... location) {
        return deepSearch(this, location, 0);
    }

    private JSONElement deepSearch(JSONElement element, String[] location, int locPosition) {
        for (int i = 0; locPosition < location.length && i < element.children.size(); ++i) {
            JSONElement child = (JSONElement) element.children.get(i);
            if (child.key != null) {
                if (child.key.equals(location[locPosition])) {
                    if (locPosition == location.length - 1) {
                        return child;
                    } else {
                        return deepSearch(child, location, locPosition + 1);
                    }
                }
            }
        }
        return null;
    }

    public <T> T valueOf(String... location) {
        JSONElement result = search(location);
        if (result != null) return (T) result.getValue();
        else return null;
    }

    public String stringValueOf(String defaultString, String... location) {
        String value;
        try {
            value = valueOf(location);
            if (value != null) {
                return value;
            }
        } catch (Exception ignored) {
        }
        return defaultString;
    }

    public <T> T valueOf(T defaultValue, String... location) {
        T value;
        try {
            value = valueOf(location);
            if (value != null) {
                return value;
            }
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    public void combine(EasyJSON easyJSONStructure) {
        combine(easyJSONStructure.getRootNode());
    }

    /**
     * overwrites any existing elements with keys
     *
     * @param jsonElement
     */
    public void combine(JSONElement jsonElement) {
        for (int i = 0; i < jsonElement.children.size(); i++) {
            putElement((JSONElement) jsonElement.children.get(i));
        }
    }

    /**
     * may cause duplicate keys, use carefully!
     *
     * @param jsonElement
     */
    private void claimElement(JSONElement jsonElement) {
        jsonElement.easyJSONStructure = easyJSONStructure;
        jsonElement.parent = this;
        children.add(jsonElement);
    }

    JSONElement overwriteWith(JSONElement newElement) {
        type = newElement.type;
        children = newElement.children;
        value = newElement.value;
        return this;
    }

    @NonNull
    @Override
    public Iterator<JSONElement> iterator() {
        return children.iterator();
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj = easyJSONStructure.deepSave(obj, this);
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return super.toString();
        }
        return obj.toJSONString();
    }
}
