package com.uniques.ourhouse.session;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;

import java.util.UUID;

public interface DatabaseLink {

    User getUser(UUID id);

    Event getEvent(UUID id);

    Task getTask(UUID id);

    Fee getFee(UUID id);

    House getHouse(UUID id);

    boolean postUser(User user);

    boolean postEvent(Event event);

    boolean postTask(Task task);

    boolean postFee(Fee fee);

    boolean postHouse(House house);
}
