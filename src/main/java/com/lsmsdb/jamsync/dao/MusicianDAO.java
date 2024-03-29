package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.*;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.MongoTask;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.lsmsdb.jamsync.routine.Neo4jConsistencyManager;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.TransactionTerminatedException;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.neo4j.driver.types.Node;


import java.time.LocalDate;
import java.util.UUID;


import static com.lsmsdb.jamsync.dao.utils.HashUtil.hashPassword;
import static com.mongodb.client.model.Filters.*;


public class MusicianDAO {

    public void createMusician(Musician musician) throws DAOException {
        // hash the password
        String digest = hashPassword(musician.getCredentials().getPassword());
        musician.getCredentials().setPassword(digest);

        String uniqueId = UUID.randomUUID().toString();
        musician.set_id(uniqueId);

        // 1. Create a new musician in MongoDB
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.MUSICIAN);
            collection.insertOne(musician.toDocument());
        } catch(Exception ex) {
            throw new DAOException(ex);
        }

        // 2. Create a node in Neo4j
        String formattedQuery = "CREATE (m:Musician {_id: %s, username: %s}) RETURN m;";
        String query = String.format(formattedQuery,
                                    "\"" + musician.get_id() + "\"",
                                    "\"" + musician.getUsername() + "\"");

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.executeWrite(tx -> {
                tx.run(query);
                return null;
            });
        } catch (TransactionTerminatedException e) {
            // Add this task to a queue to be executed later from the Neo4jConsistencyManager
            LogManager.getLogger("MusicianDAO").warn("Transaction terminated. Adding task to queue...");
            Neo4jConsistencyManager.getInstance().pushOperation(query);
        } catch (Exception e) {
            LogManager.getLogger("MusicianDAO").error("Exception: " + e.getMessage());
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

    public Musician updateMusicianById(String id, Musician musician) throws DAOException {
        // 1. Update the musician in mongo
        Document updatedDocument = null;
        Document oldDocument = null;
        Document newDocument = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.MUSICIAN);

            // retrieve the old document
            MongoCursor<Document> cursor = collection.find(eq("_id", id)).iterator();
            if(cursor.hasNext())
                oldDocument = cursor.next();
            else {
                LogManager.getLogger("MusicianDAO").warn("Musician not found with id " + id + " in MongoDB");
                throw new DAOException("Musician not found");
            }

            musician.setCredentials(new Credentials(oldDocument.get("credentials", Document.class)));
            newDocument = musician.toDocument();
            updatedDocument = collection.findOneAndReplace(eq("_id", id), newDocument, new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER));
            if (updatedDocument == null) {
                LogManager.getLogger("MusicianDAO").warn("Musician not found with id " + id + " in MongoDB");
                throw new DAOException("Musician not found");
            }
        } catch(Exception ex) {
            LogManager.getLogger("MusicianDAO").error("Error while updating musician with id " + id + " in MongoDB");
            throw new DAOException(ex);
        }

        // 2. Add task to update redundancies in the background (eventual consistency)
        MongoUpdater.getInstance().pushTask(new MongoTask("UPDATE_MUSICIAN", updatedDocument));

        // 3. Update the musician in neo4j
        String formattedQuery = "MATCH (m:Musician {_id: %s})\n" +
                                "SET m.username = %s \n" +
                                "RETURN m;";
        String query = String.format(formattedQuery,
                                    "\"" + id + "\"",
                                    "\"" + musician.getUsername() + "\"");

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

        return musician;
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

    public boolean checkFollow(String id, String userId, String type) throws DAOException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            return session.executeRead(tx -> {
                String query = String.format("MATCH (follower:Musician {_id: $followerId})-[r:FOLLOWS]->(u:%s {_id: $followedId}) RETURN r", type);
                Result result = tx.run(query, Values.parameters("followerId", id, "followedId", userId));
                return result.hasNext();
            });
        } catch (Neo4jException e) {
            throw new DAOException("Error while checking follow");
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
    public List<Opportunity> getSuggestedOpportunities(Musician m) throws DAOException {
        List<Opportunity> suggestedOpportunities = new ArrayList<>();
        List<String> musicianGenres = m.getGenres();
        List<String> musicianInstruments = m.getInstruments();
        Location musicianLocation = m.getLocation();
        String musicianCountry = musicianLocation.getCountry();
        Integer maxDistance = 150;
        String today = LocalDate.now().toString();

        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

        Bson musicianExpiresAtFilter = Filters.or(
                Filters.eq("expiresAt", null),
                Filters.gte("expiresAt", today)
        );
        Bson musicianCountryFilter = Filters.or(
                Filters.eq("location.state", musicianCountry),
                Filters.eq("location.country", musicianCountry));
        Bson genresinstrumentsFilter = Filters.or(
                Filters.in("genres", musicianGenres),
                Filters.in("instruments", musicianInstruments)
        );
        Bson typeFilter = Filters.eq("publisher.type", "Band");

        List<Bson> filters = new ArrayList<>();
        filters.add(musicianExpiresAtFilter);
        filters.add(musicianCountryFilter);
        if (!musicianGenres.isEmpty() || !musicianInstruments.isEmpty())
            filters.add(genresinstrumentsFilter);
        filters.add(typeFilter);

        if (musicianLocation != null && !musicianLocation.getCity().isEmpty()) {
           Point musicianPoint = new Point(new Position(musicianLocation.getGeojson().getCoordinates().get(0),
                   musicianLocation.getGeojson().getCoordinates().get(1)));
            Bson geoNearFilter = Filters.near("location.geojson", musicianPoint, maxDistance.doubleValue() * 1000, null);
            filters.add(geoNearFilter);
        }

        Bson query = Filters.and(filters);

        MongoCursor<Document> cursor = collection.find(query).limit(10).iterator();
        while (cursor.hasNext()) {
            Document opportunityDoc = cursor.next();
            Opportunity opportunity = new Opportunity(opportunityDoc);
            suggestedOpportunities.add(opportunity);
        }
        cursor.close();

        return suggestedOpportunities;
    }

    public List<Document> suggestOpportunitiesByFollowers(Musician musician) throws DAOException {
        String formattedQuery = "MATCH (m:Musician {_id: '%s'})-[:FOLLOWS]->(b:Band)-[:PUBLISHED]->(o:Opportunity)\n" +
                "RETURN o LIMIT 5 UNION\n" +
                "MATCH (m:Musician {_id: '%s'})-[:FOLLOWS]->(fm:Musician)-[:APPLIED_FOR]->(o:Opportunity)\n" +
                "RETURN o\n" +
                "LIMIT 5";

        String query = String.format(formattedQuery, musician.get_id(),musician.get_id());
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            String finalQuery = query;
            return session.readTransaction(tx -> {
                List<Record> records = tx.run(finalQuery).list();
                List<Document> documents = new ArrayList<>();
                for (Record record : records) {
                    Value value = record.get("o");
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

    public List<Musician> getSuggestedMusiciansBySimilarities(Musician m) throws DAOException{
        List<Musician> suggestedMusicians = new ArrayList<>();
        List<String> musicianGenres = m.getGenres();
        List<String> musicianInstruments = m.getInstruments();
        Location musicianLocation = m.getLocation();
        String musicianCountry = musicianLocation.getCountry();
        Integer maxDistance = 150;

        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.MUSICIAN);

        Bson exludeMusicianFilter = Filters.ne("_id", m.get_id());
        Bson musicianCountryFilter = Filters.or(
                Filters.eq("location.state", musicianCountry),
                Filters.eq("location.country", musicianCountry));
        Bson genresinstrumentsFilter = Filters.or(
                Filters.in("genres", musicianGenres),
                Filters.in("instruments", musicianInstruments)
        );

        List<Bson> filters = new ArrayList<>();
        filters.add(exludeMusicianFilter);
        filters.add(musicianCountryFilter);
        if (!musicianGenres.isEmpty() || !musicianInstruments.isEmpty())
            filters.add(genresinstrumentsFilter);

        if (musicianLocation != null && !musicianLocation.getCity().isEmpty()) {
            Point musicianPoint = new Point(new Position(musicianLocation.getGeojson().getCoordinates().get(0),
                    musicianLocation.getGeojson().getCoordinates().get(1)));
            Bson geoNearFilter = Filters.near("location.geojson", musicianPoint, maxDistance.doubleValue() * 1000, null);
            filters.add(geoNearFilter);
        }

        Bson query = Filters.and(filters);

        MongoCursor<Document> cursor = collection.find(query).limit(10).iterator();
        while (cursor.hasNext()) {
            Document musicianDoc = cursor.next();
            Musician musician = new Musician(musicianDoc);
            suggestedMusicians.add(musician);
        }
        cursor.close();

        return suggestedMusicians;
    }

    public List<Document> getSuggestedMusiciansByNetwork(String id)  throws DAOException {
        String formattedQuery = "MATCH (m:Musician {_id:'%s'})-[:FOLLOWS]->(:Musician)-[:FOLLOWS]->(recommended:Musician)\n" +
                "WHERE NOT (m)-[:FOLLOWS]->(recommended)\n" +
                "RETURN recommended\n" +
                "LIMIT 12";

        String query = String.format(formattedQuery, id);
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            String finalQuery = query;
            return session.readTransaction(tx -> {
                List<Record> records = tx.run(finalQuery).list();
                List<Document> documents = new ArrayList<>();
                for (Record record : records) {
                    Value value = record.get("recommended");
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

    public List<Band> getSuggestedBandsBySimilarities(Musician m) throws DAOException{
        List<Band> suggestedBands = new ArrayList<>();
        List<String> musicianGenres = m.getGenres();
        Location musicianLocation = m.getLocation();
        String musicianCountry = musicianLocation.getCountry();
        Integer maxDistance = 150;

        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.BAND);

        Bson exludeBandFilter = Filters.ne("_id", m.get_id());
        Bson bandCountryFilter = Filters.or(
                Filters.eq("location.state", musicianCountry),
                Filters.eq("location.country", musicianCountry));
        Bson genresFilter = Filters.in("genres", musicianGenres);

        List<Bson> filters = new ArrayList<>();
        filters.add(exludeBandFilter);
        filters.add(bandCountryFilter);
        if (!musicianGenres.isEmpty())
        filters.add(genresFilter);

        if (musicianLocation != null && !musicianLocation.getCity().isEmpty()) {
            Point bandPoint = new Point(new Position(musicianLocation.getGeojson().getCoordinates().get(0),
                    musicianLocation.getGeojson().getCoordinates().get(1)));
            Bson geoNearFilter = Filters.near("location.geojson", bandPoint, maxDistance.doubleValue() * 1000, null);
            filters.add(geoNearFilter);
        }

        Bson query = Filters.and(filters);

        MongoCursor<Document> cursor = collection.find(query).limit(10).iterator();
        while (cursor.hasNext()) {
            Document bandDoc = cursor.next();
            Band band = new Band(bandDoc);
            suggestedBands.add(band);
        }
        cursor.close();

        return suggestedBands;
    }

    public List<Document> getSuggestedBandsByNetwork(String id) throws DAOException {
        String formattedQuery = "MATCH (m:Musician {_id:'%s'})-[:FOLLOWS]->(:Musician)-[:FOLLOWS]->(follower)-[:MEMBER_OF]->(recommended:Band)\n" +
                "WHERE NOT (m)-[:MEMBER_OF]->(recommended)\n" +
                "RETURN recommended\n" +
                "LIMIT 12";

        String query = String.format(formattedQuery, id);
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            String finalQuery = query;
            return session.readTransaction(tx -> {
                List<Record> records = tx.run(finalQuery).list();
                List<Document> documents = new ArrayList<>();
                for (Record record : records) {
                    Value value = record.get("recommended");
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

    public Integer getBandAffinity(String musicianId, String bandId) throws DAOException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            return session.executeRead(tx -> {
                String formattedQuery = "MATCH p=shortestPath((m1:Musician {_id: '%s'})-[:FOLLOWS|MEMBER_OF*..50]-(b1:Band {_id: '%s'})) RETURN length(p) as pathDistance";
                String query = String.format(formattedQuery, musicianId, bandId);
                // result of the query is a number representing the length of the shortest path
                Result result = tx.run(query);
                LogManager.getLogger("MusicianDAO").info("Result: " + result.toString());
                if (result.hasNext()) {
                    Record record = result.next();
                    return record.get("pathDistance").asInt();
                }
                return -1;
            });
        } catch (Neo4jException e) {
            LogManager.getLogger("MusicianDAO").error(e.getMessage());
            throw new DAOException("Error while getting band affinity");
        }
    }
}