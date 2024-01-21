package com.lsmsdb.jamsync.repository;

import lombok.Getter;
import org.neo4j.driver.*;

import java.util.concurrent.TimeUnit;

public class Neo4jDriver {
    @Getter
    private static final Neo4jDriver instance = new Neo4jDriver();
    @Getter
    private Driver driver;

    private Neo4jDriver() {
        String uri = Neo4jConfig.getNeo4jUri();
        String username = Neo4jConfig.getNeo4jUsername();
        String password = Neo4jConfig.getNeo4jPassword();

        Config config = Config.builder().withMaxConnectionPoolSize(100).
                withMaxTransactionRetryTime(5, TimeUnit.SECONDS).
                build()
                ;

        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password), config);
    }

    public void closeConnection(){
        driver.close();
    }
}
