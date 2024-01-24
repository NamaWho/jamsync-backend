package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.MongoTask;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.lsmsdb.jamsync.routine.Neo4jConsistencyManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.TransactionTerminatedException;

import static com.lsmsdb.jamsync.dao.utils.HashUtil.hashPassword;
import static com.mongodb.client.model.Filters.eq;

public class MusicianDAO {

    public void createMusician(Musician musician) throws DAOException {
        // hash the password
        String digest = hashPassword(musician.getCredentials().getPassword());
        musician.getCredentials().setPassword(digest);

        // 1. Create a new musician in MongoDB
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.MUSICIAN);
            collection.insertOne(musician.toDocument());
        } catch(Exception ex) {
            throw new DAOException(ex);
        }

        // 2. Create a node in Neo4j
        String formattedQuery = "CREATE (m:Musician {_id: %s, username: %s, genres: %s, instruments: %s}) RETURN m;";
        String query = String.format(formattedQuery,
                                    "\"" + musician.get_id() + "\"",
                                    "\"" + musician.getUsername() + "\"",
                                    "\"" + musician.getGenres() + "\"",
                                    "\"" + musician.getInstruments() + "\"");

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.executeWrite(tx -> {
                tx.run(query);
                return null;
            });
        } catch (TransactionTerminatedException e) {
            // Add this task to a queue to be executed later from the Neo4jConsistencyManager
            System.out.println("Transaction terminated. Adding task to queue...");
            Neo4jConsistencyManager.getInstance().pushOperation(query);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            throw new DAOException(e.getMessage());
        }
    }

    public Musician getMusicianById(String id) throws DAOException {
        MongoCursor<Document> cursor = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.MUSICIAN);
            cursor = collection.find(eq("_id", id)).iterator();
            if(cursor.hasNext()) {
                Document document = cursor.next();
                return new Musician(document);
            }
        } catch(Exception ex) {
            throw new DAOException(ex);
        } finally {
            if(cursor != null) cursor.close();
        }
        return null;
    }

    public void deleteMusicianById(String id) throws DAOException {
        // 1. Delete the musician from mongo
        Document deletedDocument = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.MUSICIAN);
            deletedDocument = collection.findOneAndDelete(eq("_id", id));
            if (deletedDocument == null) {
                LogManager.getLogger("MusicianDAO").warn("Musician not found with id " + id + " in MongoDB");
                throw new DAOException("Musician not found");
            }
        } catch(Exception ex) {
            LogManager.getLogger("MusicianDAO").error("Error while deleting musician with id " + id + " in MongoDB");
            throw new DAOException(ex);
        }

        // 2. Add task to update redundancies in the background (eventual consistency)
        MongoUpdater.getInstance().pushTask(new MongoTask("DELETE_MUSICIAN", deletedDocument));

        // 3. Delete the musician from neo4j
        // When deleting a Musician, we also need to delete the Opportunities he published with the application received for that opportunity, and the applications he sent for other opportunities
        String formattedQuery = "MATCH (m:Musician {_id: %s})\n" +
                                "OPTIONAL MATCH (m)-[:PUBLISHED]->(o:Opportunity)\n" +
                                "OPTIONAL MATCH (m)-[r:APPLIED_FOR]->(o2:Opportunity)\n" +
                                "DETACH DELETE m, o\n" +
                                "DELETE r\n";
        String query = String.format(formattedQuery,
                "\"" + id + "\"");

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.executeWrite(tx -> {
                tx.run(query);
                return null;
            });
        } catch (TransactionTerminatedException e) {
            // Add this task to a queue to be executed later from the Neo4jConsistencyManager
            LogManager.getLogger("MusicianDAO").error("Transaction terminated. Adding task to queue...");
            Neo4jConsistencyManager.getInstance().pushOperation(query);
        } catch (Exception e) {
            LogManager.getLogger("MusicianDAO").error("Exception: " + e.getMessage());
            throw new DAOException(e.getMessage());
        }
    }

    public Integer getFollowingCount(String _id) throws DAOException{
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {

            return session.executeRead(tx -> {
                String query = "MATCH (:Musician {_id: $targetId})-[:FOLLOWS]->(followed:Musician) RETURN COUNT(followed)";
                Result result = tx.run(query, Values.parameters("targetId", _id));
                if (result.hasNext()) {
                    Record record = result.next();
                    return record.get(0).asInt();
                }
                return 0;
            });
        } catch (Neo4jException e) {
            throw new DAOException("Error while getting following count");
        }

    }

    public void follow(String id, String followedId, String type) throws DAOException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.executeWrite(tx -> {
                // Check if the relationship already exists
                String checkQuery = String.format("MATCH (follower:Musician {_id: $followerId})-[r:FOLLOWS]->(u:%s {_id: $followedId}) RETURN r", type);
                Result checkResult = tx.run(checkQuery, Values.parameters("followerId", id, "followedId", followedId));
                if (checkResult.hasNext()) {
                    throw new RuntimeException("The musician is already following this user.");
                }

                String query = String.format("MATCH (follower:Musician {_id: $followerId}), (followed:%s {_id: $followedId}) " +
                        "CREATE (follower)-[:FOLLOWS]->(followed)", type);
                tx.run(query, Values.parameters("followerId", id, "followedId", followedId));
                return 0;
            });
        } catch (RuntimeException e) {
            throw new DAOException(e.getMessage());
        }
    }

    public void unfollow(String id, String followedId, String type) throws DAOException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.executeWrite(tx -> {
                // Check if the relationship effectively exists
                String checkQuery = String.format("MATCH (follower:Musician {_id: $followerId})-[r:FOLLOWS]->(u:%s {_id: $followedId}) RETURN r", type);
                Result checkResult = tx.run(checkQuery, Values.parameters("followerId", id, "followedId", followedId));
                if (!checkResult.hasNext()) {
                    throw new RuntimeException("The musician is not following this user.");
                }
                String query = String.format("MATCH (follower:Musician {_id: $followerId})-[r:FOLLOWS]->(u:%s {_id: $followedId}) DELETE r", type);
                tx.run(query, Values.parameters("followerId", id, "followedId", followedId));
                return 0;
            });
        } catch (RuntimeException e) {
            throw new DAOException(e.getMessage());
        }
    }


}
