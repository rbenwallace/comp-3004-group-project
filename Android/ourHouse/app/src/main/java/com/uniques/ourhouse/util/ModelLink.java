package com.uniques.ourhouse.util;

import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;

import java.util.UUID;

<<<<<<< HEAD
public abstract class ModelLink<E extends IndexableModel> {
=======
public abstract class ModelLink<E extends Indexable> {
>>>>>>> master
    private UUID id;
    private E model;

    protected ModelLink(E model) {
        this.id = model.getId();
        this.model = model;
    }

    ModelLink(UUID id, E model) {
        this.id = id;
        this.model = model;
    }

    protected abstract E getMethod(DatabaseLink link, UUID id);

    protected abstract boolean postMethod(DatabaseLink link, E model);

    public UUID id() {
        return id;
    }

    public E get() {
        return model == null ? update() : model;
    }

    public E update() {
        return model = getMethod(Session.getSession().getDatabase(), id);
    }

    public boolean save() {
        return model != null && postMethod(Session.getSession().getDatabase(), model);
    }
}
