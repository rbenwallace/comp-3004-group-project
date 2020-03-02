package com.uniques.ourhouse.session;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;

import org.bson.types.ObjectId;

public interface DatabaseLinkLocal {

    public User getUser(ObjectId id);

    public Event getEvent(ObjectId id);

    public Task getTask(ObjectId id);

    public Fee getFee(ObjectId id);

    public House getHouse(ObjectId id);

    public boolean postUser(User user);

    public boolean postEvent(Event event);

    public boolean postTask(Task task);

    public boolean postFee(Fee fee);

    public boolean postHouse(House house);
}
