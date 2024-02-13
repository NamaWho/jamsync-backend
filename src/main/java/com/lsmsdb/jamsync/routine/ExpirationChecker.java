package com.lsmsdb.jamsync.routine;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.client.model.Updates;
import org.apache.logging.log4j.LogManager;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.TransactionTerminatedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ExpirationChecker {

    private static final MongoCollection<Document> opportunityCollection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

    public ExpirationChecker() {}

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // this runs the task every 24 hours
    public void checkExpiration() {
        String today = LocalDate.now().toString();
        //List<Document> expiredOpportunities = opportunityCollection.find(Filters.lte("expiresAt", today)).into(new ArrayList<>());
        // filter with lte("expiresAt", today) and eq("location.country", "Italy")
        List<Document> expiredOpportunities = opportunityCollection.find(Filters.and(
                Filters.lte("expiresAt", today),
                Filters.ne("isExpired", true)
        )).into(new ArrayList<>());
        LogManager.getLogger().info("Checking for expired opportunities..." + expiredOpportunities.size() + " expired opportunities found");
        for (Document doc : expiredOpportunities) {

            // Delete the node in Neo4j
            String formattedQuery = "MATCH (o:Opportunity {_id: %s}) " +
                    "DETACH DELETE o";

            String query = String.format(formattedQuery,
                    "\"" + doc.getString("_id") + "\"");

            try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
                session.executeWrite(tx -> {
                    tx.run(query);
                    return null;
                });
            } catch (TransactionTerminatedException e) {
                // Add this task to a queue to be executed later from the Neo4jConsistencyManager
                LogManager.getLogger("ExpirationChecker").warn(e.getMessage());
                Neo4jConsistencyManager.getInstance().pushOperation(query);
            } catch (Exception e) {
                LogManager.getLogger("ExpirationChecker").error(e.getMessage());
            }

            // Mark the opportunity as expired in MongoDB
            opportunityCollection.updateOne(
                    Filters.eq("_id", doc.get("_id")),
                    Updates.set("isExpired", true)
            );

            MongoUpdater.getInstance().pushTask(new MongoTask("DELETE_OPPORTUNITY", doc));
        }
    }
}
