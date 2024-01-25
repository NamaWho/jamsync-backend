package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.MongoTask;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.lsmsdb.jamsync.routine.Neo4jConsistencyManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
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

    public Band updateBandById(String id, Band band) throws DAOException {
        // 1. Update the band in mongo
        Document updatedDocument = null;
        Document oldDocument = null;
        Document newDocument = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.BAND);

            // retrieve the old document
            MongoCursor<Document> cursor = collection.find(eq("_id", id)).iterator();
            if(cursor.hasNext())
                oldDocument = cursor.next();
            else {
                LogManager.getLogger("BandDAO").warn("Band not found with id " + id + " in MongoDB");
                throw new DAOException("Band not found");
            }

            newDocument = band.toDocument();
            newDocument.put("credentials", oldDocument.get("credentials"));
            updatedDocument = collection.findOneAndReplace(eq("_id", id), newDocument);
            if (updatedDocument == null) {
                LogManager.getLogger("BandDAO").warn("Band not found with id " + id + " in MongoDB");
                throw new DAOException("Band not found");
            }
        } catch(Exception ex) {
            LogManager.getLogger("BandDAO").error("Error while updating band with id " + id + " in MongoDB");
            throw new DAOException(ex);
        }

        // 2. Add task to update redundancies in the background (eventual consistency)
        MongoUpdater.getInstance().pushTask(new MongoTask("UPDATE_BAND", updatedDocument));

        // 3. Update the band in neo4j
        String formattedQuery = "MATCH (b:Band {_id: %s})\n" +
                "SET b.username = %s, b.genres = %s\n" +
                "RETURN b;";
        String query = String.format(formattedQuery,
                "\"" + id + "\"",
                "\"" + band.getUsername() + "\"",
                "\"" + band.getGenres() + "\"");

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.executeWrite(tx -> {
                tx.run(query);
                return null;
            });
        } catch (TransactionTerminatedException e) {
            // Add this task to a queue to be executed later from the Neo4jConsistencyManager
            LogManager.getLogger("BandDAO").error("Transaction terminated. Adding task to queue...");
            Neo4jConsistencyManager.getInstance().pushOperation(query);
        } catch (Exception e) {
            LogManager.getLogger("BandDAO").error("Exception: " + e.getMessage());
            throw new DAOException(e.getMessage());
        }

        return band;
    }

    public void deleteBandById(String id) throws DAOException {
        // 1. Delete the band from mongo
        Document deletedDocument = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.BAND);
            deletedDocument = collection.findOneAndDelete(eq("_id", id));
            if (deletedDocument == null) {
                LogManager.getLogger(BandDAO.class).warn("Band not found with id " + id + " in MongoDB");
                throw new DAOException("Band not found");
            }
        } catch(Exception ex) {
            LogManager.getLogger("BandDAO").error("Error while deleting band with id " + id + " in MongoDB");
            throw new DAOException(ex);
        }

        // 2. Add task to update redundancies in the background (eventual consistency)
        MongoUpdater.getInstance().pushTask(new MongoTask("DELETE_BAND", deletedDocument));

        // 3. Delete the musician from neo4j
        // When deleting a Musician, we also need to delete the Opportunities he published with the application received for that opportunity, and the applications he sent for other opportunities
        String formattedQuery = "MATCH (b:Band {_id: %s})\n" +
                "OPTIONAL MATCH (b)-[:PUBLISHED]->(o:Opportunity)\n" +
                "OPTIONAL MATCH (b)-[r:APPLIED_FOR]->(o2:Opportunity)\n" +
                "DETACH DELETE b, o\n" +
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
            LogManager.getLogger(BandDAO.class).error("Transaction terminated. Adding task to queue...");
            Neo4jConsistencyManager.getInstance().pushOperation(query);
        } catch (Exception e) {
            LogManager.getLogger(BandDAO.class).error("Exception: " + e.getMessage());
            throw new DAOException(e.getMessage());
        }
    }
    public boolean addMember(String bandId, String musicianId) throws DAOException {
        String query = null;
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            query = "MATCH (b:Band {_id: $bandId}), (m:Musician {_id: $musicianId})" +
                    "CREATE (b)-[:MEMBER_OF]->(m)";

            String finalQuery = query;
            session.executeWrite(tx -> {
                tx.run(finalQuery, Values.parameters("bandId", bandId, "musicianId", musicianId));
                return null;
            });

            return true;
        } catch (TransactionTerminatedException e) {
            // Add this task to a queue to be executed later from the Neo4jConsistencyManager
            LogManager.getLogger(BandDAO.class).error("Transaction terminated. Adding task to queue...");
            Neo4jConsistencyManager.getInstance().pushOperation(query);
            return false;
        } catch (Exception e) {
            LogManager.getLogger(BandDAO.class).error("Exception: " + e.getMessage());
            throw new DAOException(e.getMessage());
        }
    }

}
