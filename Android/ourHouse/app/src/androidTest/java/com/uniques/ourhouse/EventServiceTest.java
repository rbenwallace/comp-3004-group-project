package com.uniques.ourhouse;

import android.content.Context;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.session.DatabaseLink;
import com.uniques.ourhouse.session.Session;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.function.Consumer;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

@RunWith(AndroidJUnit4.class)
public class EventServiceTest {

    @Test
    public void testService() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Session.newSession(appContext);
        Session session = Session.getSession();
        session.setDatabase(new FakeDatabase());

        EventService service = new EventService();
        service.onStartJob(null);
    }

    static class FakeDatabase implements DatabaseLink {

        @Override
        public void postEvent(Event event, Consumer<Boolean> consumer) {
            System.out.println("FAKE-DATABASE: posting Event (" + event + ")");
            consumer.accept(true);
        }

        @Override
        public void findHousesByName(String name, Consumer<List<House>> consumer) {

        }

        @Override
        public void getUser(ObjectId id, Consumer<User> consumer) {

        }

        @Override
        public void getEvent(ObjectId id, Consumer<Event> consumer) {

        }

        @Override
        public void getTask(ObjectId id, Consumer<Task> consumer) {

        }

        @Override
        public void getFee(ObjectId id, Consumer<Fee> consumer) {

        }

        @Override
        public void getHouse(ObjectId id, Consumer<House> consumer) {

        }

        @Override
        public void getAllEventsFromHouse(ObjectId houseId, Consumer<List<Event>> consumer) {

        }

        @Override
        public void getAllTasksFromHouse(ObjectId houseId, Consumer<List<Task>> consumer) {

        }

        @Override
        public void getAllFeesFromHouse(ObjectId houseId, Consumer<List<Fee>> consumer) {

        }

        @Override
        public void getAllEventsFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Event>> consumer) {

        }

        @Override
        public void getAllTasksFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Task>> consumer) {

        }

        @Override
        public void getAllFeesFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Fee>> consumer) {

        }

        @Override
        public void postUser(User user, Consumer<Boolean> consumer) {

        }

        @Override
        public void postTask(Task task, Consumer<Boolean> consumer) {

        }

        @Override
        public void postFee(Fee fee, Consumer<Boolean> consumer) {

        }

        @Override
        public void postHouse(House house, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllEventsFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllTasksFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllFeesFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllEventsFromUser(ObjectId userId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllTasksFromUser(ObjectId userId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllFeesFromUser(ObjectId userId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllEventsFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllTasksFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteAllFeesFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteUser(User user, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteEvent(Event event, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteTask(Task task, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteFee(Fee fee, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteHouse(House house, Consumer<Boolean> consumer) {

        }

        @Override
        public void deleteUserFromHouse(House house, User user, Consumer<Boolean> consumer) {

        }

        @Override
        public void updateUser(User user, Consumer<Boolean> consumer) {

        }

        @Override
        public void updateFee(Fee fee, Consumer<Boolean> consumer) {

        }

        @Override
        public void updateTask(Task task, Consumer<Boolean> consumer) {

        }

        @Override
        public void updateEvent(Event event, Consumer<Boolean> consumer) {

        }

        @Override
        public void updateHouse(House house, Consumer<Boolean> consumer) {

        }

        @Override
        public void updateOwner(House house, User user, Consumer<Boolean> consumer) {

        }

        @Override
        public void checkIfHouseKeyExists(String id, Consumer<Boolean> consumer) {

        }
    }
}
