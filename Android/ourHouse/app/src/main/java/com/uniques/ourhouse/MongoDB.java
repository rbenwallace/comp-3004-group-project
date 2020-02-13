package com.uniques.ourhouse;

import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;

public class MongoDB {
    public StitchAppClient client = Stitch.initializeDefaultAppClient("ourhouse-notdj");

    public  RemoteMongoClient mongoClient = client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");

    private RemoteMongoCollection<Document> coll = mongoClient.getDatabase("ourHouseD").getCollection("Users");




}
