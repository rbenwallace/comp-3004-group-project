package com.uniques.ourhouse.session;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;

import org.bson.types.ObjectId;

import java.util.function.Consumer;

public interface DatabaseLink {

    void getUser(ObjectId id, Consumer<User> consumer);

    void getEvent(ObjectId id, Consumer<Event> consumer);

    void getTask(ObjectId id, Consumer<Task> consumer);

    void getFee(ObjectId id, Consumer<Fee> consumer);

    void getHouse(ObjectId id, Consumer<House> consumer);

    void postUser(User user, Consumer<Boolean> consumer);

    void postEvent(Event event, Consumer<Boolean> consumer);

    void postTask(Task post_task, Consumer<Boolean> consumer);

    void postFee(Fee fee, Consumer<Boolean> consumer);

    void postHouse(House house, Consumer<Boolean> consumer);
}
