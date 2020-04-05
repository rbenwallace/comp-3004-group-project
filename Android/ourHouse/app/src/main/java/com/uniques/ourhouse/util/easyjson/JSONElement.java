package com.uniques.ourhouse.util.easyjson;

import com.uniques.ourhouse.util.exception.ElementNotFoundException;
import com.uniques.ourhouse.util.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

@SuppressWarnings({"unused", "WeakerAccess"})
public class JSONElement implements Iterable<JSONElement> {
    private EasyJSON easyJSONStructure;
    private JSONElement parent;
    @NonNull
    private JSONElementType type;
    @NonNull
    private List<JSONElement> children;
    private String key;
    private Object value;

    JSONElement(EasyJSON easyJSONStructure, JSONElement parent, @NonNull JSONElementType type, String key, Object value) {
        this.easyJSONStructure = easyJSONStructure;
        this.parent = parent;
        this.type = type;
        this.children = new ArrayList<>();
        this.key = key;
        this.value = value;
    }

    public EasyJSON getEasyJSONStructure() {
        return easyJSONStructure;
    }

    public JSONElement getParent() {
        return parent;
    }

    @NonNull
    public JSONElementType getType() {
        return type;
    }

    public void setType(SafeJSONElementType type) {
        this.type = type.getRealType();
    }

    @NonNull
    public List<JSONElement> getChildren() {
        return children;
    }

    public String getKey() {
        return key;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
//        if (value instanceof JSONObject) {
//            JSONObject obj = (JSONObject) value;
//            if (obj.size() == 1) {
//                Set keys = obj.keySet();
//                if (keys.size() == 1) {
//                    for (Object o : keys) {
//                        if (o instanceof String) {
//                            return (T) obj.get(o);
//                        }
//                    }
//                }
//            }
//        }
        return (T) value;
    }

    void setValue(Object value) {
        this.value = value;
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
        switch (jsonElement.type) {
            case PRIMITIVE:
                return putPrimitive(key, jsonElement);
            case ARRAY:
                return putArray(key, jsonElement.children.toArray());
            case STRUCTURE:
            case ROOT:
                return putStructure(key, jsonElement);
        }
        return null;
    }

    public JSONElement putPrimitive(Object value) {
        JSONElement element;
        if (value instanceof JSONElement) {
            element = (JSONElement) value;
            claimElement(element);
        } else {
            element = new JSONElement(
                    easyJSONStructure,
                    this,
                    JSONElementType.PRIMITIVE,
                    null,
                    value == null ? null : value.toString());
            children.add(element);
        }
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
            ((JSONElement) deconstructedKey[0]).putStructure((String) deconstructedKey[1]);
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
        if (key != null) {
            Object[] deconstructedKey = deconstructKey(key);
            if (deconstructedKey[0] != this) {
                return ((JSONElement) deconstructedKey[0]).putStructure((String) deconstructedKey[1], structure);
            }
        }
        JSONElement searchResult = key == null ? null : search(key);
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
            JSONElement arrayElement = new JSONElement(easyJSONStructure, this, JSONElementType.ARRAY, key, null);
            for (Object item : items) {
                if (item instanceof JSONElement) {
                    JSONElement itemElement = (JSONElement) item;
                    arrayElement.putElement(null, itemElement);
                } else {
                    arrayElement.putPrimitive(item);
                }
            }
            children.add(arrayElement);
            return arrayElement;
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

    public boolean removeElement(String... location) {
//        System.out.println(Arrays.toString(location.clone()));
//        System.out.println(toString());
//        System.out.println(children.get(0).getKey());
        JSONElement element = search(location);
        if (element != null) {
//            System.out.println(element);
            //TODO figure out why this is needed
            if (element.getParent() == null && element.getType() != JSONElementType.ROOT) {
                element.parent = easyJSONStructure.getRootNode();
            }
//            if (element.getParent() != null) {
//                System.out.println("par: " + element.getParent());
//                System.out.println("parchld: " + element.getParent().children);
//            }
        }
        return element == null || element.getParent().children.remove(element);
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
     * This method will also call {@link #putStructure(String) putStructure(String)} for every missing part of a complex key.
     *
     * @param key simple/complex key you want to deconstruct
     * @return Object[] {suitable_parent: JSONElement, final_key: String}
     * @throws RuntimeException if any part (before/after '.') of a complex key has < 1 character
     */
    public Object[] deconstructKey(String key) {
        return deconstructKey(key, true);
    }


    /**
     * @param generateStructures set to true to call {@link #putStructure(String) putStructure(String)} for every missing part of a complex key.
     *                           If set to false, this method will throw an ElementNotFoundException
     * @see #deconstructKey(String) deconstructKey(String) for fuller docs
     */
    private Object[] deconstructKey(String key, boolean generateStructures) {
        if (key == null) {
            return new Object[]{this, null};
        }
        JSONElement result = this;
        String finalKey = key;
        if (key.contains(".")) {
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
                    if (generateStructures) {
                        result = result.putStructure(part);
                    } else {
                        throw new ElementNotFoundException();
                    }
                } else {
                    result = innerSearch;
                }
            }
        }
        return new Object[]{result, finalKey};
    }

    /**
     * Search for the JSONElement at the specified location
     */
    public JSONElement search(String... location) {
//        for (String loc : location) {
//            if (loc.contains("."))
//                return searchNested(this, location);
//        }
        return deepSearch(this, location, 0);
    }

//    private JSONElement searchNested(JSONElement at, int locIndex, String... location) {
//        List<String> expandedPath = new ArrayList<>();
//        if (location.length == 0) return null;
//        if (location[locIndex].contains(".")) {
//            try {
//                Object[] res = this.deconstructKey(location[locIndex], false);
//                return ((JSONElement) res[0]).searchNested(null, locIndex + 1, location);
//            } catch (ElementNotFoundException e) {
//                return null;
//            }
//        } else {
//            JSONElement next = this.search(location[locIndex]);
//            at.searchNested(at, locIndex + 1, location);
//        }
//    }

    private JSONElement deepSearch(JSONElement element, String[] location, int locPosition) {
        for (int i = 0; locPosition < location.length && i < element.children.size(); ++i) {
            JSONElement child = element.children.get(i);
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

    /**
     * Attempts to retrieve and cast the value of the element at the specific location. If the value
     * cannot be found, null is returned. <b>This method does not check types, a Runtime
     * exception my occur</b>
     */
    public <T> T valueOf(String... location) {
        JSONElement result = search(location);
        if (result != null) {
            return (T) result.getValue();
        } else return null;
    }

    /**
     * Attempts to retrieve and cast the value of the element at the specific location. If the value
     * cannot be found, casted or is null, defaultValue is returned
     *
     * @param location path to the element whose value you want
     * @param <T>      expected type of the value
     */
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

    /**
     * Wrapper method for {@link #combine(JSONElement)} using {@link EasyJSON#getRootNode()}
     */
    public void combine(EasyJSON easyJSONStructure) {
        combine(easyJSONStructure.getRootNode());
    }

    /**
     * Children of the supplied element that have keys matching children in this element are used to
     * overwrite the values of the corresponding child in this element.
     * <br/>
     * <code>
     * {"foo": "bar", "abc": 123}.combine({"abc": "xyz"}) --> {"foo": "bar", "abc": "xyz"}
     * </code>
     */
    public void combine(JSONElement jsonElement) {
        for (JSONElement element : jsonElement) {
            JSONElement match = search(element.key);
            if (match != null) {
                match.overwriteWith(element);
            }
        }
    }

    /**
     * Adds the supplied element to this element's children, updating it's easyJSONStructure and
     * parent in the process to the corresponding values in (this).
     */
    private void claimElement(JSONElement jsonElement) {
        jsonElement.easyJSONStructure = easyJSONStructure;
        jsonElement.parent = this;
        children.add(jsonElement);
    }

    /**
     * Overwrites this element with data in newElement. Specifically, it will adopt newElement's
     * type, children, and value.
     *
     * @param newElement element whose data you want to adopt
     * @return this element
     */
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
