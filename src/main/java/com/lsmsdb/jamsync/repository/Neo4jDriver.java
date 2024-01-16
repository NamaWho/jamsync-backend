package com.lsmsdb.jamsync.repository;

import lombok.Getter;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jDriver {
    @Getter
    private static final Neo4jDriver instance = new Neo4jDriver();
    @Getter
    private Driver driver;

    private Neo4jDriver() {
        String uri = Neo4jConfig.getNeo4jUri();
        String username = Neo4jConfig.getNeo4jUsername();
        String password = Neo4jConfig.getNeo4jPassword();

        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public void closeConnection(){
        driver.close();
    }
}
