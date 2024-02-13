package com.lsmsdb.jamsync.repository;

import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.LoggerSettings;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;

public class MongoDriver {

    private static final MongoDriver driver = new MongoDriver();
    private static final String MONGO_URI = "mongodb://%s:%d,%s:%d,%s:%d/?w=W1&readPreference=secondaryPreferred&wtimeoutms=5000&retryWrites=true";
    private final MongoClient client;
    private final MongoDatabase defaultDB;

    private MongoDriver(){
        String host = MongoConfig.getMongoHost();
        int portOne = MongoConfig.getMongoPortOne();
        int portTwo = MongoConfig.getMongoPortTwo();
        int portThree = MongoConfig.getMongoPortThree();
        String database = MongoConfig.getMongoDatabase();

        String connectionString = String.format(MONGO_URI, host, portOne, host, portTwo, host, portThree);

        client = MongoClients.create(connectionString);
        defaultDB = client.getDatabase(database)
                .withWriteConcern(WriteConcern.W1)
                .withReadPreference(ReadPreference.secondaryPreferred());
    }

    public MongoCollection getCollection(MongoCollectionsEnum c){
        return defaultDB.getCollection(c.getName())
                .withWriteConcern(WriteConcern.W1)
                .withReadPreference(ReadPreference.secondaryPreferred());
    }

    public static MongoDriver getInstance(){return driver;}

    public void closeConnection(){
        client.close();
    }
}
