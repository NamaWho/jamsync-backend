package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.Neo4jException;

import static com.mongodb.client.model.Filters.eq;

public class MusicianDAO {

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
}
