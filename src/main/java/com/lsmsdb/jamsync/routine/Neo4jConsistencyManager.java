package com.lsmsdb.jamsync.routine;

// Neo4jConsistencyManager.java

import com.lsmsdb.jamsync.repository.Neo4jDriver;
import org.apache.logging.log4j.LogManager;
import org.neo4j.driver.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Neo4jConsistencyManager {

    private static final Neo4jConsistencyManager manager = new Neo4jConsistencyManager();
    //private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ConcurrentLinkedQueue<String> operations = new ConcurrentLinkedQueue<>();

    // retrieve Driver from Neo4jDriver class in repository package

    private Neo4jConsistencyManager() {
        System.out.println("Neo4jConsistencyManager ready...");
        //scheduler.scheduleAtFixedRate(this::retryOperation, 0, 5, TimeUnit.MINUTES);
    }

    public static Neo4jConsistencyManager getInstance() { return manager; }

    public void pushOperation(String operation) {
        operations.add(operation);
    }

    private String popOperation() {
        return operations.poll();
    }

    @Scheduled(cron = "0 0/5 * * * ?") // this runs the task every 5 minutes
    private void retryOperation() {
        LogManager.getLogger("Neo4jConsistencyManager").info("Retrying Neo4j failed operations...");
        String operation = popOperation();
        List<String> failedOperations = new ArrayList<>();
        while (operation != null) {
            try {
                Session session = Neo4jDriver.getInstance().getDriver().session();
                session.run(operation);
                session.close();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                failedOperations.add(operation);
            }
            operation = popOperation();
        }
        for (String op : failedOperations) {
            operations.add(op);
        }
    }

    public void shutdown() {
        //scheduler.shutdown();
    }
}
