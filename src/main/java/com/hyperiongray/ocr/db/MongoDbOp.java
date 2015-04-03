package com.hyperiongray.ocr.db;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Set;

public class MongoDbOp {
    private DB db;
    private DBCollection collection;

    public static void main(String[] args) throws Exception {
        // test the connnection
        MongoDbOp instance = new MongoDbOp();
        instance.openConnection();
        instance.listCollections();
    }

    public void openConnection() throws java.net.UnknownHostException {

        MongoClient mongoClient = new MongoClient();

        db = mongoClient.getDB("MemexHack");
        System.out.println("Connection to MongoDB established");
        collection = db.getCollection("urlinfo");
    }

    private void listCollections() {
        System.out.println("List collections:");
        // get a list of the collections in this database and print them out
        Set<String> collectionNames = db.getCollectionNames();
        for (final String s : collectionNames) {
            System.out.println(s);
        }
    }


    public void addScore(String mongoId, String keyPhrase, float score) {
//        System.out.println("Updating MongoDB for mongoId=" + mongoId + ", keyPhrase=" + keyPhrase + ", score=" + score);
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append(keyPhrase, score);
        BasicDBObject searchQuery = new BasicDBObject();
        try {
            searchQuery.put("_id", new ObjectId(mongoId));
            DBObject dbObj = collection.findOne(searchQuery);
//            System.out.println("before update");
//            System.out.println(dbObj.toString());
            dbObj.put("keyPhrase " + keyPhrase, score);
            collection.update(searchQuery, dbObj);
            // debugging - verify
            dbObj = collection.findOne(searchQuery);
//            System.out.println("after update");
//            System.out.println(dbObj.toString());
        } catch (Exception e) {
            // TODO better error handling
            // but this error should not normally happen
            e.printStackTrace();
        }
    }
}