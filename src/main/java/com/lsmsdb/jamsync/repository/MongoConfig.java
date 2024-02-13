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

    public static String getMongoHostOne() {
        return properties.getProperty("mongo.hostOne");
    }

    public static String getMongoHostTwo() { return properties.getProperty("mongo.hostTwo"); }

    public static String getMongoHostThree() { return properties.getProperty("mongo.hostThree"); }

    public static int getMongoPort() {
        return Integer.parseInt(properties.getProperty("mongo.port"));
    }

    public static String getMongoDatabase() {
        return properties.getProperty("mongo.database");
    }
}
