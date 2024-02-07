package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.*;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.MongoTask;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.lsmsdb.jamsync.routine.Neo4jConsistencyManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.neo4j.driver.Record;
import org.bson.conversions.Bson;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.TransactionTerminatedException;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.TypeSystem;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;

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

            band.setCredentials(new Credentials(oldDocument.get("credentials", Document.class)));
            newDocument = band.toDocument();
            updatedDocument = collection.findOneAndReplace(eq("_id", id), newDocument, new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER));
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
        String formattedQuery = "MATCH (b:Band {_id: %s}), (m:Musician {_id: %s})" +
                "CREATE (m)-[:MEMBER_OF]->(b)";
         query = String.format(formattedQuery,
                "\"" + bandId + "\"",
                "\"" + musicianId + "\"");


        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {

            String finalQuery = query;
            session.executeWrite(tx -> {
                tx.run(finalQuery);
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
    public boolean removeMember(String bandId, String musicianId) throws DAOException {
        String query = null;
        String formattedQuery = "MATCH (b:Band {_id: '%s'})<-[r:MEMBER_OF]-(m:Musician {_id: '%s'})" +
                "DELETE r";
        query = String.format(formattedQuery, bandId, musicianId);

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            String finalQuery = query;
            session.executeWrite(tx -> {
                tx.run(finalQuery);
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

    public List<Document> getMembers(String id) throws DAOException {
        String query = "MATCH (b:Band {_id: %s})<-[:MEMBER_OF]-(m:Musician) RETURN m";
        query = String.format(query, "\"" + id + "\"");
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            String finalQuery = query;
            return session.readTransaction(tx -> {
                List<Record> records = tx.run(finalQuery).list();
                List<Document> documents = new ArrayList<>();
                for (Record record : records) {
                    Value value = record.get("m");
                    if (value.type().name().equals("NODE")) {
                        Node node = value.asNode();
                        documents.add(new Document(node.asMap()));
                    }
                }
                return documents;
            });
        } catch (Exception e) {
            LogManager.getLogger(BandDAO.class).error("Exception: " + e.getMessage());
            throw new DAOException(e.getMessage());
        }
    }

    public List<Opportunity> getSuggestedOpportunities(Band b) throws DAOException {
        List<Opportunity> suggestedOpportunities = new ArrayList<>();
        List<String> bandGenres = b.getGenres();
        Location bandLocation = b.getLocation();
        String bandCountry = bandLocation.getCountry().isEmpty() ? bandLocation.getState() : bandLocation.getCountry();
        Integer maxDistance = 50;
        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);
        String today = LocalDate.now().toString();

        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

        Bson expiresAtFilter = Filters.or(
                Filters.eq("expiresAt", null),
                Filters.gte("expiresAt", today)
        );
        Bson bandCountryFilter = Filters.or(
                Filters.eq("location.state", bandCountry),
                Filters.eq("location.country", bandCountry)
        );
        Bson createdAtFilter = Filters.gte("createdAt", sixtyDaysAgo.toString());
        Bson genresFilter = Filters.in("genres", bandGenres);
        Bson typeFilter = Filters.eq("publisher.type", "Musician");

        List<Bson> filters = new ArrayList<>();
        filters.add(expiresAtFilter);
        filters.add(bandCountryFilter);
        filters.add(createdAtFilter);
        filters.add(genresFilter);
        filters.add(typeFilter);

        if (bandLocation != null && !bandLocation.getCity().isEmpty()) {
            Point musicianPoint = new Point(new Position(bandLocation.getGeojson().getCoordinates().get(0),
                    bandLocation.getGeojson().getCoordinates().get(1)));
            Bson geoNearFilter = Filters.near("location.geojson", musicianPoint, maxDistance.doubleValue() * 1000, null);
            filters.add(geoNearFilter);
        }

        Bson query = Filters.and(filters);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        while (cursor.hasNext()) {
            Document opportunityDoc = cursor.next();
            Opportunity opportunity = new Opportunity(opportunityDoc);
            suggestedOpportunities.add(opportunity);
        }
        cursor.close();

        return suggestedOpportunities;
    }
}
