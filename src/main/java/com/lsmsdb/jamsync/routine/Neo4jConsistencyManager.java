package com.lsmsdb.jamsync.routine;

// Neo4jConsistencyManager.java

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Neo4jConsistencyManager {

    private static final Neo4jConsistencyManager manager = new Neo4jConsistencyManager();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<String> operations = new ArrayList<>();

    // retrieve Driver from Neo4jDriver class in repository package

    private Neo4jConsistencyManager() {
        System.out.println("Neo4jConsistencyManager ready...");
        scheduler.scheduleAtFixedRate(this::retryOperation, 0, 5, TimeUnit.MINUTES);
    }

    public static Neo4jConsistencyManager getInstance() { return manager; }

    public void pushOperation(String operation) {
        operations.add(operation);
    }
    private void retryOperation() {}

    public void shutdown() {
        scheduler.shutdown();
    }
}
