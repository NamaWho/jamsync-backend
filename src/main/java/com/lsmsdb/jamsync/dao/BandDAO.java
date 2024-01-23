package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.Neo4jConsistencyManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.TransactionTerminatedException;

import static com.lsmsdb.jamsync.dao.utils.HashUtil.hashPassword;
import static com.mongodb.client.model.Filters.eq;

public class BandDAO {

    public void createBand(Band band) throws DAOException {
        // hash the password
        String digest = hashPassword(band.getCredentials().getPassword());
        band.getCredentials().setPassword(digest);

        // 1. Create a new band in MongoDB
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.BAND);
            collection.insertOne(band.toDocument());
        } catch(Exception ex) {
            throw new DAOException(ex);
        }

        // 2. Create a node in Neo4j
        String formattedQuery = "CREATE (b:Band {_id: %s, username: %s, genres: %s }) RETURN b;";
        String query = String.format(formattedQuery,
                "\"" + band.get_id() + "\"",
                "\"" + band.getUsername() + "\"",
                "\"" + band.getGenres() + "\"");

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

    public Band getBandById(String id) throws DAOException {
        MongoCursor<Document> cursor = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.BAND);
            cursor = collection.find(eq("_id", id)).iterator();
            if(cursor.hasNext()) {
                Document document = cursor.next();
                //document.put("username", document.get("name"));
                return new Band(document);
            }
        } catch(Exception ex) {
            throw new DAOException(ex);
        } finally {
            if(cursor != null) cursor.close();
        }
        return null;
    }

    public boolean addMember(String bandId, String musicianId) throws DAOException {
        //TODO: implement
        return false;
    }
}
