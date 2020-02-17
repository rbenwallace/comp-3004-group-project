package com.uniques.ourhouse.util.MongoDB;

import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.Tag;
import com.mongodb.client.FindIterable;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.uniques.ourhouse.util.House;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class MongoDB extends SecurityLink{
    public static final String TAG = "MongoDB";
    public StitchAppClient client = Stitch.initializeDefaultAppClient("ourhouse-notdj");
    public  RemoteMongoClient mongoClient = client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
    private RemoteMongoCollection<Document> userColl = mongoClient.getDatabase("ourHouseD").getCollection("Users");
    private RemoteMongoCollection<Document> housesColl = mongoClient.getDatabase(House.HOUSE_DATABASE).getCollection(House.HOUSE_COLLECTION);

//    private boolean addHouse(House house){
//        try{
//            housesColl.insertOne(house.toBsonDocument(house));
//            return true;
//        }
//        catch (MongoException | ClassCastException e) {
//            Log.d(TAG, "Exception occurred while insert Value using **Document** : " + e);
//            return false;
//        }
//    }
//
//    private void addUserToHouse(String HouseID, String UserID){
//
//    }
//
//    private void getHouseID(String HouseID, String UserID){
//
//    }
//
//    private void getHouse(){
//
//    }
//    private void getUsers(House house){
//        RemoteFindIterable<Document> findIterable = housesColl.find(eq("_id", house.getHouseID()));
//
//    }
    @Override
    SecureAuthenticator getSecureAuthenticator() {

        return null;
    }
    @Override
    protected boolean autoAuthenticate(UUID id, UUID loginKey) {
        if(client.getAuth().getUser() != null){
            return true;
        }
        return false;
    }
}
