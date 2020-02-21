package com.uniques.ourhouse.util;

import com.uniques.ourhouse.model.User;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class House {
    private String houseName;
    private int userNum;
    private ObjectId houseID;
    private static List<String> Users;
    private static Class<String> clazz;

    public static final String HOUSE_DATABASE = "ourHouseD";
    public static final String HOUSE_COLLECTION = "Houses";

    public House(ObjectId houseID, String houseName,  List<String>  users) {
        this.houseName = houseName;
        this.houseID = houseID;
        this.Users = users;
        userNum = 0;
    }

    static final class Fields {
        static final String House_ID = "_id";
        static final String HouseName = "house_name";
        static final String Users = "Users";
        static final String UsersArray = "Users_";
    }

    public static Document toBsonDocument(final House house) {
        final Document asDoc = new Document();
        asDoc.put(Fields.House_ID, house.getHouseID());
        asDoc.put(Fields.HouseName, house.getHouseName());
        asDoc.put(Fields.Users, Users);
        return asDoc;

    }

    private Document getUsersDoc() {
        final Document asDoc = new Document();
        for(String user : Users){
            String curUser = Fields.UsersArray + userNum;
            asDoc.put(Fields.UsersArray, user);
            userNum++;
        }
        return asDoc;
    }

    public static House fromBsonDocument(final Document doc) {
        return new House(
                doc.getObjectId(Fields.House_ID),
                doc.getString(Fields.HouseName),
                doc.getList(Fields.Users, clazz));
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public ObjectId getHouseID() {
        return houseID;
    }

    public List<String> getUsers() {
        return Users;
    }
    public void addUser(String User) {
        Users.add(User);
    }
    public void deleteUser(String User) {
        Users.remove(User);
    }

    public void setUsers(ArrayList<String> users) {
        Users = users;
    }

    public void setHouseID(ObjectId houseID) {
        this.houseID = houseID;
    }

    public static final Codec<House> codec = new Codec<House>() {

        @Override
        public void encode( final BsonWriter writer, final House value, final EncoderContext encoderContext) {
            new DocumentCodec().encode(writer, toBsonDocument(value), encoderContext);
        }

        @Override
        public Class<House> getEncoderClass() {
            return House.class;
        }

        @Override
        public House decode(
                final BsonReader reader, final DecoderContext decoderContext) {
            final Document document = (new DocumentCodec()).decode(reader, decoderContext);
            return fromBsonDocument(document);
        }
    };

}
