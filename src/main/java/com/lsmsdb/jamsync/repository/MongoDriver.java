package com.lsmsdb.jamsync.repository;

import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDriver {

    private static final MongoDriver driver = new MongoDriver();
    private static final String MONGO_URI = "mongodb://%s:%d";
    private final MongoClient client;
    private final MongoDatabase defaultDB;

    private MongoDriver(){
        String host = MongoConfig.getMongoHost();
        int port = MongoConfig.getMongoPort();
        String database = MongoConfig.getMongoDatabase();

        String connectionString = String.format(MONGO_URI, host, port);

        client = MongoClients.create(connectionString);
        defaultDB = client.getDatabase(database);
    }

    public MongoCollection getCollection(MongoCollectionsEnum c){
        return defaultDB.getCollection(c.getName());
    }

    public static MongoDriver getInstance(){return driver;}

    public void closeConnection(){
        client.close();
    }
}
