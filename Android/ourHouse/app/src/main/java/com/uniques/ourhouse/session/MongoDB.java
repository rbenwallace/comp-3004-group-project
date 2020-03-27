package com.uniques.ourhouse.session;

import android.util.Log;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchAuth;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteDeleteResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;
import com.uniques.ourhouse.fragment.FragmentActivity;
import com.uniques.ourhouse.model.Event;
import com.uniques.ourhouse.model.Fee;
import com.uniques.ourhouse.model.House;
import com.uniques.ourhouse.model.Task;
import com.uniques.ourhouse.model.User;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MongoDB extends SecurityLink implements DatabaseLink {
    private static final StitchAppClient CLIENT = Stitch.initializeAppClient("ourhouse-notdj");
    private static final String DATABASE = "ourHouseD";
    public static final String TAG = "MongoDB";

    private static final String USER_COLLECTION = "Users";
    private static final String EVENT_COLLECTION = "Events";
    private static final String FEE_COLLECTION = "Fees";
    private static final String HOUSE_COLLECTION = "Houses";
    private static final String TASK_COLLECTION = "Tasks";

    private RemoteMongoClient mongoClient = CLIENT.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
    private RemoteMongoCollection<Document> userColl = mongoClient.getDatabase(DATABASE).getCollection(USER_COLLECTION);
    private RemoteMongoCollection<Document> housesColl = mongoClient.getDatabase(DATABASE).getCollection(HOUSE_COLLECTION);
    private RemoteMongoCollection<Document> eventColl = mongoClient.getDatabase(DATABASE).getCollection(EVENT_COLLECTION);
    private RemoteMongoCollection<Document> taskColl = mongoClient.getDatabase(DATABASE).getCollection(TASK_COLLECTION);
    private RemoteMongoCollection<Document> feeColl = mongoClient.getDatabase(DATABASE).getCollection(FEE_COLLECTION);
    private Long count;

    private final SecureAuthenticator secureAuthenticator;

    MongoDB() {
        secureAuthenticator = new SecureAuthenticator() {
            @Override
            public void registerUser(String email, String password, Consumer<Exception> callback) {
                CLIENT.getAuth().getProviderClient(UserPasswordAuthProviderClient.factory)
                        .registerWithEmail(email, password)
                        .addOnCompleteListener(task -> callback.accept(task.getException()));
            }

            @Override
            public void authenticateUser(String username, String password, BiConsumer<Exception, ObjectId> callback) {
                try {
                    CLIENT.getAuth()
                            .loginWithCredential(new UserPasswordCredential(username, password))
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    callback.accept(null, new ObjectId(task.getResult().getId()));
                                } else {
                                    callback.accept(task.getException(), null);
                                }
                            });
                } catch (Exception e) {
                    callback.accept(e, null);
                }
            }

            @Override
            public void logout(FragmentActivity activity, Consumer<Boolean> consumer) {
                CLIENT.getAuth().logout().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Session.getSession().setLoggedInUser(null);
                        Log.i(TAG, "Successfully logged out!");
                        consumer.accept(true);
                    } else {
                        Log.e(TAG, "Logout failed!", task.getException());
                        consumer.accept(false);
                    }
                });
            }
        };
    }

    //Stitch functions
//    @Override
    public StitchAuth getAuth() {
        return CLIENT.getAuth();
    }
    //-------------------------------------------------------------

    @Override
    public SecureAuthenticator getSecureAuthenticator() {
        return secureAuthenticator;
    }

    @Override
    public boolean autoAuthenticate() {
        StitchUser loggedInUser = CLIENT.getAuth().getUser();
        return loggedInUser != null && CLIENT.getAuth().getUser().getId() != null;
    }

    @Override
    public ObjectId getLoggedInUserId() {
        StitchUser loggedInUser = CLIENT.getAuth().getUser();
        return loggedInUser == null ? null : new ObjectId(loggedInUser.getId());
    }

//    @Override
//    public StitchUser getStitchUser() {
//        if (client.getAuth().getUser() != null) {
//            return client.getAuth().getUser();
//        }
//        return null;
//    }
    //-------------------------------------------------------------

    //Database and Shared Pref - Mainly for me
    //-------
//    @Override
//    public void addMyUser(User user, FragmentActivity activity) {
//        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = userColl.insertOne(user.toBsonDocument());
//        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
//                if (task.isSuccessful()) {
//                    Log.d("app", String.format("successfully inserted item with id %s",
//                            task.getResult().getInsertedId()));
//                    Log.d("newUser", user.toString());
//                    setLocalUser(user, activity);
//                } else {
//                    Log.e("app", "failed to insert document with: ", task.getException());
//                }
//            }
//        });
//
//    }
//
//    @Override
//    public void addMyHouse(House house, FragmentActivity activity, Consumer<Boolean> boolConsumer) {
//        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = housesColl.insertOne(house.toBsonDocument());
//        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<RemoteInsertOneResult> task) {
//                if (task.isSuccessful()) {
//                    Log.d("app", String.format("successfully inserted item with id %s",
//                            task.getResult().getInsertedId()));
//                    User myUser = getCurrentLocalUser(activity);
//                    myUser.addHouseId(house.getId());
//                    setLocalUser(myUser, activity);
//                    setLocalHouse(house, activity);
//                    ArrayList<House> curHouseList = getLocalHouseArray(activity);
//                    curHouseList.add(house);
//                    setLocalHouseArray(curHouseList, activity);
//                    boolConsumer.accept(true);
//                } else {
//                    Log.e("app", "failed to insert document with: ", task.getException());
//                    boolConsumer.accept(false);
//                }
//            }
//        });
//
//    }

    @Override
    public void findHousesByName(String name, Consumer<List<House>> consumer) {
        ArrayList<House> houses = new ArrayList<>();
        String pattern = "^" + name;
        BsonRegularExpression nameRE = new BsonRegularExpression(pattern);
        Document filterDoc = new Document()
                .append("key", new Document().append("$regex", nameRE));
        housesColl.count(filterDoc).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                final Long numDocs = task.getResult();
                RemoteFindIterable<Document> findResults = housesColl
                        .find(filterDoc)
                        .limit(20);
                count = 0L;
                findResults.forEach(house -> {
                    House searchHouse = House.fromBsonDocument(house);
                    houses.add(searchHouse);
                    count++;
                    if (Objects.equals(count, numDocs))
                        consumer.accept(houses);
                });
            } else {
                consumer.accept(houses);
            }
        });
    }
    //-------

    //Cloud functions
    //-------------------------------------------------------------
    //Get---
    @Override
    public void getUser(ObjectId id, Consumer<User> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = userColl.findOne(query);
        findOne.addOnCompleteListener(task -> {
            if (task.getResult() == null) {
                Log.d("app", "No document matches the provided query");
                Log.d("stitch-auth", "Authentication Successful.");
                consumer.accept(null);
            } else if (task.isSuccessful()) {
                Log.d("app", String.format("Successfully found document: %s",
                        task.getResult()));
                consumer.accept(User.fromBsonDocument(task.getResult()));
                Log.d("stitch-auth", "Authentication Successful.");
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findOne: ", task.getException());
                Log.d("stitch-auth", "Authentication Successful.");
            }
        });
    } //tested

    @Override
    public void getEvent(ObjectId id, Consumer<Event> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = eventColl.findOne(query);
        findOne.addOnCompleteListener(task -> {
            if (task.getResult() == null) {
                Log.d("app", "No document matches the provided query");
                consumer.accept(null);
            } else if (task.isSuccessful()) {
                Log.d("app", String.format("Successfully found document: %s",
                        task.getResult()));
                Event.FromBsonDocument(task.getResult(), consumer);
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findOne: ", task.getException());
            }
        });
    } //tested

    @Override
    public void getHouseEventOnDay(ObjectId houseId, Date day, Consumer<Event> consumer) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date fromDate = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date toDate = cal.getTime();
        Document query = new Document()
                .append("assignedHouse", houseId)
                .append("dueDate", BasicDBObjectBuilder.start("$gte", fromDate).add("$lte", toDate).get());
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = eventColl.findOne(query);
        findOne.addOnCompleteListener(task -> {
            if (task.getResult() == null) {
                Log.d("app", "No document matches the provided query");
                consumer.accept(null);
            } else if (task.isSuccessful()) {
                Log.d("app", String.format("Successfully found document: %s",
                        task.getResult()));
                Event.FromBsonDocument(task.getResult(), consumer);
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findOne: ", task.getException());
            }
        });
    }

    @Override
    public void getTask(ObjectId id, Consumer<Task> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = taskColl.findOne(query);
        findOne.addOnCompleteListener(task -> {
            if (task.getResult() == null) {
                Log.d("app", "No document matches the provided query");
                consumer.accept(null);
            } else if (task.isSuccessful()) {
                Log.d("app", String.format("Successfully found document: %s",
                        task.getResult()));
                consumer.accept(Task.fromBsonDocument(task.getResult()));
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findOne: ", task.getException());
            }
        });
    } //tested

    @Override
    public void getFee(ObjectId id, Consumer<Fee> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = feeColl.findOne(query);
        findOne.addOnCompleteListener(task -> {
            if (task.getResult() == null) {
                Log.d("app", "No document matches the provided query");
                consumer.accept(null);
            } else if (task.isSuccessful()) {
                Log.d("app", String.format("Successfully found document: %s",
                        task.getResult()));
                consumer.accept(Fee.fromBsonDocument(task.getResult()));
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findOne: ", task.getException());
            }
        });
    } //tested

    @Override
    public void getHouse(ObjectId id, Consumer<House> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = housesColl.findOne(query);
        findOne.addOnCompleteListener(task -> {
            if (task.getResult() == null) {
                Log.d("app", "No document matches the provided query");
                consumer.accept(null);
            } else if (task.isSuccessful()) {
                Log.d("app", String.format("Successfully found document: %s",
                        task.getResult()));
                consumer.accept(House.fromBsonDocument(task.getResult()));
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findOne: ", task.getException());
            }
        });
    } //tested

    //All returns are in Decending order im tired rn so like if u want it opposite just change the -1 to a 1 in the .sort inside the functions
    @SuppressWarnings("unchecked")
    @Override
    public void getAllEventsFromHouse(ObjectId houseId, Consumer<List<Event>> consumer) {
//        ArrayList<Event> events = new ArrayList<>();
        Document filterDoc = new Document()
                .append("assignedHouse", houseId);
        eventColl.find(filterDoc).sort(new Document("dueDate", -1)).into(new ArrayList<>()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Document> docs = task.getResult();
                ArrayList<Event> events = new ArrayList<>();
                BiConsumer<Document, BiConsumer> createNextEvent = (doc, next) -> {
                    Event.FromBsonDocument(doc, event -> {
                        //TODO this if statement won't be needed once all document conform to the new Event standard
                        // the addition will still be needed
                        if (event != null) {
                            events.add(event);
                        }
                        if (docs.isEmpty()) {
                            consumer.accept(events);
                        } else {
                            next.accept(docs.remove(0), next);
                        }
                    });
                };
                if (docs.isEmpty()) {
                    consumer.accept(events);
                } else {
                    createNextEvent.accept(docs.remove(0), createNextEvent);
                }
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findTasksFromHouse: ", task.getException());
            }
        });
//        eventColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
//                if (task.isSuccessful()) {
//                    final Long numDocs = task.getResult();
//                    count = 0L;
//                    RemoteFindIterable findResults = eventColl
//                            .find(filterDoc).sort(new Document("dueDate", -1));
//                    findResults.forEach(item -> {
//                        Log.d("app", String.format("successfully found Events:  %s", item.toString()));
//                        Document event = (Document) item;
//                        Event.FromBsonDocument(event, rEvent -> {
//                            events.add(rEvent);
//                            count++;
//                            if (count == numDocs)
//                                consumer.accept(events);
//                        });
//                    });
//                } else {
//                    consumer.accept(events);
//                }
//            }
//        });
    } //tested

    @Override
    public void getAllTasksFromHouse(ObjectId houseId, Consumer<List<Task>> consumer) {
//        ArrayList<Task> tasks = new ArrayList<>();
        Document filterDoc = new Document()
                .append("houseId", houseId);
        taskColl.find(filterDoc).sort(new Document("dueDate", -1)).into(new ArrayList<>()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                consumer.accept(task.getResult().stream().map(Task::fromBsonDocument).collect(Collectors.toList()));
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findTasksFromHouse: ", task.getException());
            }
        });
//        taskColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
//                if (task.isSuccessful()) {
//                    final Long numDocs = task.getResult();
//                    count = 0L;
//                    RemoteFindIterable findResults = taskColl
//                            .find(filterDoc).sort(new Document("dueDate", -1));
//                    findResults.forEach(item -> {
//                        Log.d("app", String.format("successfully found Task:  %s", item.toString()));
//                        tasks.add(Task.fromBsonDocument((Document) item));
//                        if (count == numDocs)
//                            consumer.accept(tasks);
//                    });
//                } else {
//                    consumer.accept(tasks);
//                }
//            }
//        });
    } //tested

    @SuppressWarnings("unchecked")
    @Override
    public void getAllFeesFromHouse(ObjectId houseId, Consumer<List<Fee>> consumer) {
//        ArrayList<Fee> fees = new ArrayList<>();
        Document filterDoc = new Document()
                .append("houseId", houseId);
        feeColl.find(filterDoc).sort(new Document("dueDate", -1)).into(new ArrayList<>()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Document> docs = task.getResult();
                ArrayList<Fee> fees = new ArrayList<>();
                BiConsumer<Document, BiConsumer> createNextEvent = (doc, next) -> {
                    Event.FromBsonDocument(doc, event -> {
                        if (docs.isEmpty()) {
                            consumer.accept(fees);
                        } else {
                            next.accept(docs.remove(0), next);
                        }
                    });
                };
                if (docs.isEmpty()) {
                    consumer.accept(fees);
                } else {
                    createNextEvent.accept(docs.remove(0), createNextEvent);
                }
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findTasksFromHouse: ", task.getException());
            }
        });
//        feeColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
//                if (task.isSuccessful()) {
//                    final Long numDocs = task.getResult();
//                    count = 0L;
//                    RemoteFindIterable findResults = feeColl
//                            .find(filterDoc).sort(new Document("dueDate", -1));
//                    findResults.forEach(item -> {
//                        Log.d("app", String.format("successfully found Fees:  %s", item.toString()));
//                        fees.add(Fee.fromBsonDocument((Document) item));
//                        if (count.equals(numDocs))
//                            consumer.accept(fees);
//                    });
//                } else {
//                    consumer.accept(fees);
//                }
//            }
//        });
    } //tested

    @SuppressWarnings("unchecked")
    @Override
    public void getAllEventsFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Event>> consumer) {
//        ArrayList<Event> events = new ArrayList<>();
        Document filterDoc = new Document()
                .append("assignedHouse", houseId);
        filterDoc.append("assignedTo", userId);
        eventColl.find(filterDoc).sort(new Document("dueDate", -1)).into(new ArrayList<>()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Document> docs = task.getResult();
                ArrayList<Event> events = new ArrayList<>();
                BiConsumer<Document, BiConsumer> createNextEvent = (doc, next) -> {
                    Event.FromBsonDocument(doc, event -> {
                        if (docs.isEmpty()) {
                            consumer.accept(events);
                        } else {
                            next.accept(docs.remove(0), next);
                        }
                    });
                };
                if (docs.isEmpty()) {
                    consumer.accept(events);
                } else {
                    createNextEvent.accept(docs.remove(0), createNextEvent);
                }
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findTasksFromHouse: ", task.getException());
            }
        });
//        eventColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
//                if (task.isSuccessful()) {
//                    final Long numDocs = task.getResult();
//                    count = 0L;
//                    RemoteFindIterable findResults = eventColl
//                            .find(filterDoc).sort(new Document("dueDate", -1));
//                    findResults.forEach(item -> {
//                        Log.d("app", String.format("successfully found Events:  %s", item.toString()));
//                        Document event = (Document) item;
//                        Event.FromBsonDocument(event, rEvent -> {
//                            events.add(rEvent);
//                            count++;
//                            if (count == numDocs)
//                                consumer.accept(events);
//                        });
//                    });
//                } else {
//                    consumer.accept(events);
//                }
//            }
//        });
    } //tested

    @SuppressWarnings("unchecked")
    @Override
    public void getAllTasksFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Task>> consumer) {
//        ArrayList<Task> tasks = new ArrayList<>();
        Document filterDoc = new Document()
                .append("houseId", houseId);
        filterDoc.append("userId", userId);
        taskColl.find(filterDoc).sort(new Document("dueDate", -1)).into(new ArrayList<>()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Document> docs = task.getResult();
                ArrayList<Task> parsedTasks = new ArrayList<>();
                BiConsumer<Document, BiConsumer> createNextEvent = (doc, next) -> {
                    Event.FromBsonDocument(doc, event -> {
                        if (docs.isEmpty()) {
                            consumer.accept(parsedTasks);
                        } else {
                            next.accept(docs.remove(0), next);
                        }
                    });
                };
                if (docs.isEmpty()) {
                    consumer.accept(parsedTasks);
                } else {
                    createNextEvent.accept(docs.remove(0), createNextEvent);
                }
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findTasksFromHouse: ", task.getException());
            }
        });
//        taskColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
//                if (task.isSuccessful()) {
//                    final Long numDocs = task.getResult();
//                    count = 0L;
//                    RemoteFindIterable findResults = taskColl
//                            .find(filterDoc).sort(new Document("dueDate", -1));
//                    findResults.forEach(item -> {
//                        Log.d("app", String.format("successfully found Task:  %s", item.toString()));
//                        tasks.add(Task.fromBsonDocument((Document) item));
//                        if (count == numDocs)
//                            consumer.accept(tasks);
//                    });
//                } else {
//                    consumer.accept(tasks);
//                }
//            }
//        });
    } //tested

    @SuppressWarnings("unchecked")
    @Override
    public void getAllFeesFromUserInHouse(ObjectId houseId, ObjectId userId, Consumer<List<Fee>> consumer) {
//        ArrayList<Fee> fees = new ArrayList<>();
        Document filterDoc = new Document()
                .append("houseId", houseId);
        filterDoc.append("userId", userId);
        eventColl.find(filterDoc).sort(new Document("dueDate", -1)).into(new ArrayList<>()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Document> docs = task.getResult();
                ArrayList<Fee> fees = new ArrayList<>();
                BiConsumer<Document, BiConsumer> createNextEvent = (doc, next) -> {
                    Event.FromBsonDocument(doc, event -> {
                        if (docs.isEmpty()) {
                            consumer.accept(fees);
                        } else {
                            next.accept(docs.remove(0), next);
                        }
                    });
                };
                if (docs.isEmpty()) {
                    consumer.accept(fees);
                } else {
                    createNextEvent.accept(docs.remove(0), createNextEvent);
                }
            } else {
                consumer.accept(null);
                Log.e("app", "Failed to findTasksFromHouse: ", task.getException());
            }
        });
//        feeColl.count(filterDoc).addOnCompleteListener(new OnCompleteListener<Long>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Long> task) {
//                if (task.isSuccessful()) {
//                    final Long numDocs = task.getResult();
//                    count = 0L;
//                    RemoteFindIterable findResults = feeColl
//                            .find(filterDoc).sort(new Document("dueDate", -1));
//                    findResults.forEach(item -> {
//                        Log.d("app", String.format("successfully found Fees:  %s", item.toString()));
//                        fees.add(Fee.fromBsonDocument((Document) item));
//                        if (count.equals(numDocs))
//                            consumer.accept(fees);
//                    });
//                } else {
//                    consumer.accept(fees);
//                }
//            }
//        });
    } //tested

    //Need to make, Ne
//    public void getAllEventsSince(ObjectId houseId, Date tillDate, Consumer<ArrayList<Task>> consumer){}
//    public void getAllTasksSince(ObjectId houseId, Date tillDate, Consumer<ArrayList<Task>> consumer){}
//    public void getAllFeesSince(ObjectId houseId, Date tillDate, Consumer<ArrayList<Task>> consumer){}
    //House Content Grabs
    //Post---
    @Override
    public void postUser(User user, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = userColl.insertOne(user.toBsonDocument());
        insertTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("app", String.format("successfully inserted user with id %s",
                        task.getResult().getInsertedId()));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to insert user with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void postEvent(Event event, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = eventColl.insertOne(event.toBsonDocument());
        insertTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("app", String.format("successfully inserted event with id %s",
                        task.getResult().getInsertedId()));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to insert event with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void postTask(Task post_task, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = taskColl.insertOne(post_task.toBsonDocument());
        insertTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("app", String.format("successfully inserted task with id %s",
                        task.getResult().getInsertedId()));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to insert task with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void postFee(Fee fee, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = feeColl.insertOne(fee.toBsonDocument());
        insertTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("app", String.format("successfully inserted fee with id %s",
                        task.getResult().getInsertedId()));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to insert fee with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void postHouse(House house, Consumer<Boolean> consumer) {
        final com.google.android.gms.tasks.Task<RemoteInsertOneResult> insertTask = housesColl.insertOne(house.toBsonDocument());
        insertTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("app", String.format("successfully inserted house with id %s",
                        task.getResult().getInsertedId()));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to insert house with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    //Delete---
    @Override
    public void deleteAllEventsFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("assignedHouse", houseId);
        filterDoc.append("assignedTo", userId);
        Log.d("deleteAllEventsFromUserInHouse", "userId: " + userId + " houseId: " + houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = eventColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteAllTasksFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("houseId", houseId);
        filterDoc.append("userId", userId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteAllFeesFromUserInHouse(ObjectId userId, ObjectId houseId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("houseId", houseId);
        filterDoc.append("userId", userId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = feeColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteAllEventsFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("assignedTo", userId);
        Log.d("app", String.format("successfully deleted %s documents", userId.toString()));
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = eventColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteAllTasksFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("userId", userId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteAllFeesFromUser(ObjectId userId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("userId", userId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = feeColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteAllEventsFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("assignedHouse", houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = eventColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteAllTasksFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("houseId", houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteAllFeesFromHouse(ObjectId houseId, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("houseId", houseId);
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = feeColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteUser(User user, Consumer<Boolean> consumer) {
        Consumer<String> failConsumer = name -> {
            Log.e(TAG, name + " failed");
            consumer.accept(false);
        };
        deleteAllEventsFromUser(user.getId(), successful1 -> {
            if (!successful1) {
                failConsumer.accept("");
                return;
            }
            deleteAllTasksFromUser(user.getId(), successful2 -> {
                if (!successful2) {
                    failConsumer.accept("");
                    return;
                }
                deleteAllFeesFromUser(user.getId(), successful3 -> {
                    if (!successful3) {
                        failConsumer.accept("");
                        return;
                    }
//                  deleteUserFromHouse(userHouse, user, bool -> {
//                     if (!bool) Log.d("deleteAllFeesFromUser", "Failed");
//                   });
                    Document filterDoc = new Document().append("_id", user.getId());
                    final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = userColl.deleteOne(filterDoc);
                    deleteTask.addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            long numDeleted = task.getResult().getDeletedCount();
                            Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                            consumer.accept(true);
                        } else {
                            Log.e("app", "failed to delete document with: ", task.getException());
                            consumer.accept(false);
                        }
                    });
                });
            });
        });
    }

    @Override
    public void deleteAllHouses(Consumer<Boolean> consumer) {
        Document filterDoc = new Document();
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = userColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

    @Override
    public void deleteEvent(Event event, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", event.getId());
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = eventColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
            }
            consumer.accept(task.isSuccessful());
        });
    } //tested

    @Override
    public void deleteTask(Task task, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", task.getId());
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                long numDeleted = task1.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
            } else {
                Log.e("app", "failed to delete document with: ", task1.getException());
            }
            consumer.accept(task1.isSuccessful());
        });
    } //tested

    @Override
    public void deleteFee(Fee fee, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", fee.getId());
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = feeColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
            }
            consumer.accept(task.isSuccessful());
        });
    } //tested

    @Override
    public void deleteHouse(House house, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", house.getId());
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = housesColl.deleteOne(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
            } else {
                consumer.accept(false);
                Log.e("app", "failed to delete document with: ", task.getException());
            }
            consumer.accept(task.isSuccessful());
        });
    } //tested

//    @Override
//    public void deleteOwnerFromHouse(House house, User user, Consumer<Boolean> consumer) {
//        //Ensure not null
//        if (house != null && user != null) {
//            //if there is nobody or just the owner, you can't delete the owner from delete Occupant, you must use deleteUserFromHouse
//            if (house.getOccupants().size() == 1) {
//                consumer.accept(false);
//            } else {
//                //Ensure Owner
//                if (user.getId() != house.getOwner().getId()) {
//                    consumer.accept(false);
//                    return;
//                }
//                deleteUserFromHouse(house, user, consumer);
//            }
//        } else {
//            consumer.accept(false);
//        }
//    } //tested

    @Override
    public void deleteUserFromHouse(House house, User user, Consumer<Boolean> consumer) {
        if (house == null || user == null) {
            consumer.accept(false);
            return;
        }
        //Log if user update fails
        Consumer<String> failConsumer = name -> {
            Log.e(TAG, name + " failed");
            consumer.accept(false);
        };
        deleteAllEventsFromUserInHouse(user.getId(), house.getId(), successful1 -> {
            if (!successful1) {
                failConsumer.accept("deleteAllEventsFromUserInHouse");
                return;
            }
            deleteAllTasksFromUserInHouse(user.getId(), house.getId(), successful2 -> {
                if (!successful2) {
                    failConsumer.accept("deleteAllTasksFromUserInHouse");
                    return;
                }
                deleteAllFeesFromUserInHouse(user.getId(), house.getId(), successful3 -> {
                    if (!successful3) {
                        failConsumer.accept("deleteAllFeesFromUserInHouse");
                        return;
                    }
                    //Delete the house if it's the only user
                    if (house.getOccupants().size() <= 1) {
                        Log.d("HouseDeleted: ", "Verified");
                        deleteHouse(house, successful4 -> {
                            if (!successful4) {
                                failConsumer.accept("deleteHouse");
                                return;
                            }
                            consumer.accept(true);
                        });
                    }
                    //Change the owner and remove house from user, update user
                    else {
                        Consumer<Boolean> afterUpdateOwner = successful4 -> {
                            if (!successful4) {
                                failConsumer.accept("updateHouse");
                                return;
                            }
                            user.removeHouseId(house.getId());
                            updateUser(user, successful5 -> {
                                if (!successful5) {
                                    failConsumer.accept("updateUser");
                                    return;
                                }
                                consumer.accept(true);
                            });
                        };
                        if (house.getOwner().getId() == user.getId()) {
                            for (User user1 : house.getOccupants()) {
                                if (user.getId() != user1.getId()) {
                                    house.setOwner(user1);
                                    house.removeOccupant(user);
                                    updateHouse(house, afterUpdateOwner);
                                    break;
                                }
                            }
                            return;
                        }
                        house.removeOccupant(user);
                        updateHouse(house, afterUpdateOwner);
                    }
                });
            });
        });
    } //tested

    //Update---
    //Update X
    @Override
    public void updateUser(User user, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", user.getId());
        Document updateDoc = user.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = userColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numMatched = task.getResult().getMatchedCount();
                long numModified = task.getResult().getModifiedCount();
                Log.d("app", String.format("successfully matched %d and modified %d documents",
                        numMatched, numModified));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to update document with: ", task.getException());
                consumer.accept(true);
            }
        });
    } //tested

    @Override
    public void updateFee(Fee fee, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", fee.getId());
        Document updateDoc = fee.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = feeColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numMatched = task.getResult().getMatchedCount();
                long numModified = task.getResult().getModifiedCount();
                Log.d("app", String.format("successfully matched %d and modified %d documents",
                        numMatched, numModified));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to update document with: ", task.getException());
                consumer.accept(true);
            }
        });
    } //tested

    @Override
    public void updateEvent(Event event, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", event.getId());
        Document updateDoc = event.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = eventColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numMatched = task.getResult().getMatchedCount();
                long numModified = task.getResult().getModifiedCount();
                Log.d("app", String.format("successfully matched %d and modified %d documents",
                        numMatched, numModified));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to update document with: ", task.getException());
                consumer.accept(true);
            }
        });
    } //tested

    @Override
    public void updateHouse(House house, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", house.getId());
        Document updateDoc = house.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = housesColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numMatched = task.getResult().getMatchedCount();
                long numModified = task.getResult().getModifiedCount();
                Log.d("app", String.format("successfully matched %d and modified %d documents",
                        numMatched, numModified));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to update document with: ", task.getException());
                consumer.accept(true);
            }
        });
    } //tested

    @Override
    public void updateTask(Task task, Consumer<Boolean> consumer) {
        Document filterDoc = new Document().append("_id", task.getId());
        Document updateDoc = task.toBsonDocument();
        final com.google.android.gms.tasks.Task<RemoteUpdateResult> updateTask = taskColl.updateOne(filterDoc, updateDoc);
        updateTask.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                long numMatched = task1.getResult().getMatchedCount();
                long numModified = task1.getResult().getModifiedCount();
                Log.d("app", String.format("successfully matched %d and modified %d documents",
                        numMatched, numModified));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to update document with: ", task1.getException());
                consumer.accept(true);
            }
        });
    } //tested

    @Override
    public void updateOwner(House house, User user, Consumer<Boolean> consumer) {
        house.setOwner(user);
        updateHouse(house, consumer);
    } //tested
    //-------------------------------------------------------------

    //General functions needed for inserting into cloud
    //-------------------------------------------------------------
    @Override
    public void checkIfHouseKeyExists(String id, Consumer<Boolean> consumer) {
        Document query = new Document().append("_id", id);
        Log.d("MongoDB", query.toString());
        final com.google.android.gms.tasks.Task<Document> findOne = housesColl.findOne(query);
        findOne.addOnCompleteListener(task -> {
            if (task.getResult() == null) {
                Log.d("app", String.format("No document matches the provided query"));
                consumer.accept(true);
            } else if (task.isSuccessful()) {
                Log.d("app", String.format("Successfully found document: %s",
                        task.getResult()));
                consumer.accept(false);
            } else {
                consumer.accept(false);
                Log.e("app", "Failed to findOne: ", task.getException());
            }
        });
    }

    private Document getQueryForUser() {
        if (CLIENT.getAuth().getUser() == null) {
            return null;
        }
        StitchUser user = CLIENT.getAuth().getUser();
        Document query = new Document().append("_id", user.getId());
        return query;
    }

    //-------------------------------------------------------------
    //CLEAR ALL DATA
    public void deleteAllCollectionData(Consumer<Boolean> consumer) {
        Document filterDoc = new Document();
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteTask = taskColl.deleteMany(filterDoc);
        deleteTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteFee = feeColl.deleteMany(filterDoc);
        deleteFee.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
        final com.google.android.gms.tasks.Task<RemoteDeleteResult> deleteEvent = eventColl.deleteMany(filterDoc);
        deleteEvent.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numDeleted = task.getResult().getDeletedCount();
                Log.d("app", String.format("successfully deleted %d documents", numDeleted));
                consumer.accept(true);
            } else {
                Log.e("app", "failed to delete document with: ", task.getException());
                consumer.accept(false);
            }
        });
    } //tested

}
