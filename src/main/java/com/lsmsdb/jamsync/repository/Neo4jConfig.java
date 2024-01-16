package com.lsmsdb.jamsync.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Neo4jConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = MongoConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNeo4jUri() {
        return properties.getProperty("neo4j.uri");
    }

    public static String getNeo4jUsername() {
        return properties.getProperty("neo4j.username");
    }

    public static String getNeo4jPassword() {
        return properties.getProperty("neo4j.password");
    }
}
