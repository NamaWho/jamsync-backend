package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.MongoTask;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.lsmsdb.jamsync.routine.Neo4jOperation;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.TransactionTerminatedException;

import com.lsmsdb.jamsync.routine.Neo4jConsistencyManager;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class OpportunityDAO {

    public Opportunity getOpportunityById(String id) throws DAOException{
        MongoCursor<Document> cursor = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
            cursor = collection.find(eq("_id", id)).iterator();
            if(cursor.hasNext()) {
                Document document = cursor.next();
                return new Opportunity(document);
            }
        } catch(Exception ex) {
            throw new DAOException(ex);
        } finally {
            if(cursor != null) cursor.close();
        }
        return null;
    }

    public Opportunity createOpportunity(Opportunity opportunity) throws DAOException{
        // 1. Create a new opportunity in MongoDB

        MongoCursor<Document> cursor = null;
        Document document = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
            document = opportunity.toDocument();
            collection.insertOne(document);
        } catch(Exception ex) {
            throw new DAOException(ex);
        } finally {
            if(cursor != null) cursor.close();
        }

        // 2. Add task to update redundancies in the background (eventual consistency)
        MongoUpdater.getInstance().pushTask(new MongoTask("CREATE_OPPORTUNITY", document));

        // 3. Create a node in Neo4j
        // TODO: fix this
        Neo4jOperation operation = new Neo4jOperation();
        operation.setQuery("CREATE (o:Opportunity {_id: $id, title: $title, genres: $genres, instruments: $instruments}) " +
                            "WITH o " +
                            "MATCH (u:" + opportunity.getPublisher().getString("type") + " {_id: $publisherId}) " +
                            "CREATE (u)-[:CREATED]->(o)");
        operation.setParameters(Map.of(
                "id", UUID.randomUUID().toString(),
                "title", opportunity.getTitle(),
                "genres", opportunity.getGenres(),
                "instruments", opportunity.getInstruments(),
                "publisherId", opportunity.getPublisher().getString("_id")));

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.executeWrite(tx -> {
                return tx.run(operation.getQuery(), Values.parameters(operation.getParameters()));
            });
        } catch (TransactionTerminatedException e) {
            // Add this task to a queue to be executed later from the Neo4jConsistencyManager
            System.out.println("Transaction terminated. Adding task to queue...");
            Neo4jConsistencyManager.getInstance().pushOperation(operation);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            throw new DAOException(e.getMessage());
        }

        return new Opportunity(document);
    }
}
