package com.uniques.ourhouse.session;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;
import com.uniques.ourhouse.util.easyjson.EasyJSON;
import com.uniques.ourhouse.util.easyjson.EasyJSONException;

import org.bson.types.ObjectId;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

//TODO delete user deletes user from all houses
class DatabaseCoordinator implements DatabaseLink {
    private static final int INTERNET_CHECK_FREQUENCY = 3000;

    private final DatabaseLink localDatabase;
    private final DatabaseLink remoteDatabase;
    private InternetChecker internetChecker;
    private EasyJSON cache;


    DatabaseCoordinator(DatabaseLink localDatabase, DatabaseLink remoteDatabase) {
        this.localDatabase = localDatabase;
        this.remoteDatabase = remoteDatabase;
    }

    DatabaseLink getRemoteDatabase() {
        return remoteDatabase;
    }

    void beginCoordinating(Context context) {
        File file = new File(context.getFilesDir(), "cache-info.json");
        EasyJSON json;
        if (!file.exists()) {
            json = EasyJSON.create(file);
            try {
                json.save();
            } catch (EasyJSONException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to save new cache-info file");
            }
        } else {
            try {
                json = EasyJSON.open(file);
            } catch (EasyJSONException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to open cache-info file");
            }
        }

        cache = json;

        if (internetChecker == null) {
            internetChecker = new InternetChecker(context);
//            new Thread(internetChecker).start();
        }
    }

    boolean networkAvailable() {
        if (internetChecker == null)
            throw new RuntimeException("Cannot check for network if beginCoordinating() hasn't been called");

        long curTime = System.currentTimeMillis();

        if (internetChecker.lastChecked + INTERNET_CHECK_FREQUENCY < curTime) {
            internetChecker.networkAvailable = internetChecker.check();
            internetChecker.lastChecked = curTime;
        }

        return internetChecker.networkAvailable;
    }

    private void notifyModelCached(ObjectId id) {
        long fiveMins = 5 * 60 * 1000;
        cache.putPrimitive(id.toString(), System.currentTimeMillis() + fiveMins);
        try {
            cache.save();
        } catch (EasyJSONException e) {
            e.printStackTrace();
        }
    }

    private boolean modelIsCached(ObjectId id) {
        return cache.elementExists(id.toString())
                && System.currentTimeMillis() >= cache.<Long>valueOf(id.toString());
    }

    @Override
    public void findHousesByName(String name, Consumer<List<House>> consumer) {
        Log.d("CheckingHouses", "Inside DB Cord");
        remoteDatabase.findHousesByName(name, consumer);
    }

    @Override
    public void getUser(ObjectId id, Consumer<User> consumer) {
        Consumer<User> networkConsumer = user -> {
            if (user != null) {
                localDatabase.postUser(user, success -> {
                    if (success) {
                        notifyModelCached(user.getId());
                        if (user.getId().equals(Session.getSession().getLoggedInUserId())) {
                            Session.getSession().setLoggedInUser(user);
                        }
                    }
                    consumer.accept(user);
                });
            } else {
                consumer.accept(null);
            }
        };
        if (modelIsCached(id)) {
            localDatabase.getUser(id, user -> {
                if (user != null) consumer.accept(user);
                else {
                    if (networkAvailable()) remoteDatabase.getUser(id, networkConsumer);
                    else consumer.accept(null);
                }
            });
        } else if (networkAvailable()) {
            remoteDatabase.getUser(id, networkConsumer);
        } else {
            consumer.accept(null);
        }
    }

    @Override
    public void getEvent(ObjectId id, Consumer<Event> consumer) {
        Consumer<Event> networkConsumer = event -> {
            if (event != null) {
                localDatabase.postEvent(event, success -> {
                    if (success) notifyModelCached(event.getId());
                    consumer.accept(event);
                });
            } else {
                consumer.accept(null);
            }
        };
        if (modelIsCached(id)) {
            localDatabase.getEvent(id, event -> {
                if (event != null) consumer.accept(event);
                else {
                    if (networkAvailable()) remoteDatabase.getEvent(id, networkConsumer);
                    else consumer.accept(null);
                }
            });
        } else if (networkAvailable()) {
            remoteDatabase.getEvent(id, networkConsumer);
        } else {
            consumer.accept(null);
        }
    }

    @Override
    public void getHouseEventOnDay(ObjectId houseId, ObjectId taskId, Date day, Consumer<Event> consumer) {
        remoteDatabase.getHouseEventOnDay(houseId, taskId, day, consumer);
    }

    @Override
    public void getTask(ObjectId id, Consumer<Task> consumer) {
        Consumer<Task> networkConsumer = task -> {
            if (task != null) {
                Log.d("checkingTask", task.toString());
                localDatabase.postTask(task, success -> {
                    if (success) notifyModelCached(task.getId());
                    consumer.accept(task);
                });
            } else {
                consumer.accept(null);
            }
        };
        if (modelIsCached(id)) {
            localDatabase.getTask(id, task -> {
                if (task != null) consumer.accept(task);
                else {
                    if (networkAvailable()) remoteDatabase.getTask(id, networkConsumer);
                    else consumer.accept(null);
                }
            });
        } else if (networkAvailable()) {
            remoteDatabase.getTask(id, networkConsumer);
        } else {
            consumer.accept(null);
        }
    }

    @Override
    public void getFee(ObjectId id, Consumer<Fee> consumer) {
        Consumer<Fee> networkConsumer = fee -> {
            if (fee != null) {
                localDatabase.postFee(fee, success -> {
                    if (success) notifyModelCached(fee.getId());
                    consumer.accept(fee);
                });
            } else {
                consumer.accept(null);
            }
        };
        if (modelIsCached(id)) {
            localDatabase.getFee(id, user -> {
                if (user != null) consumer.accept(user);
                else {
                    if (networkAvailable()) remoteDatabase.getFee(id, networkConsumer);
                    else consumer.accept(null);
                }
            });
        } else if (networkAvailable()) {
            remoteDatabase.getFee(id, networkConsumer);
        } else {
            consumer.accept(null);
        }
    }


    @Override
    public void getHouse(ObjectId id, Consumer<House> consumer) {
        Consumer<House> networkConsumer = house -> {
            if (house != null) {
                localDatabase.postHouse(house, success -> {
                    if (success) notifyModelCached(house.getId());
                    consumer.accept(house);
                });
            } else {
                consumer.accept(null);
            }
        };
        if (modelIsCached(id)) {
            localDatabase.getHouse(id, user -> {
                if (user != null) consumer.accept(user);
                else {
                    if (networkAvailable()) remoteDatabase.getHouse(id, networkConsumer);
                    else consumer.accept(null);
                }
            });
        } else if (networkAvailable()) {
            remoteDatabase.getHouse(id, networkConsumer);
        } else {
            consumer.accept(null);
        }
    }

    @Override
    public void getAllEventsFromHouse(ObjectId houseId, Consumer<List<Event>> consumer) {
        remoteDatabase.getAllEventsFromHouse(houseId, consumer);
    }

    @Override
    public void getAllTasksFromHouse(ObjectId houseId, Consumer<List<Task>> consumer) {
        remoteDatabase.getAllTasksFromHouse(houseId, consumer);
    }

    @Override
    public void getAllFeesFromHouse(ObjectId houseId, Consumer<List<Fee>> consumer) {
        remoteDatabase.getAllFeesFromHouse(houseId, consumer);
    }

    @Override
    public void getAllEventsFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Event>> consumer) {
        remoteDatabase.getAllEventsFromUserInHouse(houseId, userId, consumer);
    }

    @Override
    public void getAllTasksFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Task>> consumer) {
        remoteDatabase.getAllTasksFromUserInHouse(houseId, userId, consumer);
    }

    @Override
    public void getAllFeesFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Fee>> consumer) {
        remoteDatabase.getAllFeesFromUserInHouse(houseId, userId, consumer);
    }

    @Override
    public void postUser(User user, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.postUser(user, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.postUser(user, localSuccess -> {
                    if (localSuccess) notifyModelCached(user.getId());
                    consumer.accept(true);
                });
            });
        } else consumer.accept(false);
    }

    @Override
    public void postEvent(Event event, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.postEvent(event, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.postEvent(event, localSuccess -> {
                    if (localSuccess) notifyModelCached(event.getId());
                    consumer.accept(true);
                });
            });
        } else consumer.accept(false);
    }

    @Override
    public void postTask(Task task, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.postTask(task, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.postTask(task, localSuccess -> {
                    if (localSuccess) notifyModelCached(task.getId());
                    consumer.accept(true);
                });
            });
        } else consumer.accept(false);
    }

    @Override
    public void postFee(Fee fee, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.postFee(fee, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.postFee(fee, localSuccess -> {
                    if (localSuccess) notifyModelCached(fee.getId());
                    consumer.accept(true);
                });
            });
        } else consumer.accept(false);
    }

    @Override
    public void postHouse(House house, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.postHouse(house, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.postHouse(house, localSuccess -> {
                    if (localSuccess) notifyModelCached(house.getId());
                    consumer.accept(true);
                });
            });
        } else consumer.accept(false);
    }

    @Override
    public void deleteAllEventsFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllEventsFromUserInHouse(userId, houseId, consumer);
    }

    @Override
    public void deleteAllTasksFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllTasksFromUserInHouse(userId, houseId, consumer);
    }

    @Override
    public void deleteAllFeesFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllFeesFromUserInHouse(userId, houseId, consumer);
    }

    @Override
    public void deleteAllEventsFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllEventsFromUser(userId, consumer);
    }

    @Override
    public void deleteAllTasksFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllTasksFromUser(userId, consumer);
    }

    @Override
    public void deleteAllFeesFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllFeesFromUser(userId, consumer);
    }

    @Override
    public void deleteAllEventsFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllEventsFromHouse(houseId, consumer);
    }

    @Override
    public void deleteAllTasksFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllTasksFromHouse(houseId, consumer);
    }

    @Override
    public void deleteAllFeesFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllFeesFromHouse(houseId, consumer);
    }

    @Override
    public void deleteUser(User user, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.deleteUser(user, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.deleteUser(user, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void deleteEvent(Event event, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.deleteEvent(event, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.deleteEvent(event, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void deleteTask(Task task, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.deleteTask(task, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.deleteTask(task, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void deleteFee(Fee fee, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.deleteFee(fee, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.deleteFee(fee, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void deleteHouse(House house, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.deleteHouse(house, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.deleteHouse(house, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void deleteUserFromHouse(House house, User user, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.deleteUserFromHouse(house, user, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.deleteUserFromHouse(house, user, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void deleteAllCollectionData(Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.deleteAllCollectionData(remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.deleteAllCollectionData(bool -> {
                });
            });
        } else consumer.accept(false);
    }

    @Override
    public void updateUser(User user, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.updateUser(user, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.updateUser(user, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void updateFee(Fee fee, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.updateFee(fee, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.updateFee(fee, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void updateTask(Task task, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.updateTask(task, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.updateTask(task, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void updateEvent(Event event, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.updateEvent(event, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.updateEvent(event, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void updateHouse(House house, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.updateHouse(house, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.updateHouse(house, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void updateOwner(House house, User user, Consumer<Boolean> consumer) {
        if (networkAvailable()) {
            remoteDatabase.updateOwner(house, user, remoteSuccess -> {
                if (!remoteSuccess) {
                    consumer.accept(false);
                    return;
                }
                localDatabase.updateOwner(house, user, consumer);
            });
        } else consumer.accept(false);
    }

    @Override
    public void deleteAllHouses(Consumer<Boolean> consumer) {
        remoteDatabase.deleteAllHouses(consumer);
    }

    @Override
    public void checkIfHouseKeyExists(String id, Consumer<Boolean> consumer) {
        remoteDatabase.checkIfHouseKeyExists(id, consumer);
    }

    @Override
    public void clearLocalState(Consumer<Boolean> consumer) {
        remoteDatabase.clearLocalState(remoteSuccess -> {
            if (remoteSuccess) {
                localDatabase.clearLocalState(consumer);
            } else consumer.accept(false);
        });
    }

    @Override
    public void emailFriends(String email, String firstName, String friend, ObjectId houseId, Consumer<Boolean> bool){
        if (networkAvailable()) {
            remoteDatabase.emailFriends(email, firstName, friend, houseId, bool);
        }
        else {
            Log.d("CheckingEmailSending", "DB Cord: " + bool.toString());
            bool.accept(false);
        }
    }

    private static class InternetChecker implements Runnable {
        private final Context context;
        private boolean networkAvailable;
        private boolean checkForNetworkAvailability;
        private long lastChecked = -1;

        private InternetChecker(Context context) {
            this.context = context;
            checkForNetworkAvailability = true;
        }

        private boolean check() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = null;
            if (connectivityManager != null) {
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            }

            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        @Override
        public void run() {
            while (checkForNetworkAvailability) {

                networkAvailable = check();
                lastChecked = System.currentTimeMillis();

                try {
                    Thread.sleep(INTERNET_CHECK_FREQUENCY);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
