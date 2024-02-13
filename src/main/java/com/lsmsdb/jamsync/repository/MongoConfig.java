package com.lsmsdb.jamsync.repository;

import com.mongodb.WriteConcern;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MongoConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = MongoConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMongoHost() {
        return properties.getProperty("mongo.host");
    }

    public static int getMongoPortOne() {
        return Integer.parseInt(properties.getProperty("mongo.portOne"));
    }

    public static int getMongoPortTwo() { return Integer.parseInt(properties.getProperty("mongo.portTwo")); }

    public static int getMongoPortThree() { return Integer.parseInt(properties.getProperty("mongo.portThree")); }

    public static String getMongoDatabase() {
        return properties.getProperty("mongo.database");
    }
}
