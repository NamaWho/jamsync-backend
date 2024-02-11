package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.*;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.MongoTask;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.*;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.TransactionTerminatedException;

import com.lsmsdb.jamsync.routine.Neo4jConsistencyManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.inc;

public class OpportunityDAO {

    public Opportunity getOpportunityById(String id) throws DAOException{
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
            Bson filter = eq("_id", id);
            Bson update = inc("visits", 1);
            Document document = collection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
            if(document != null) {
                return new Opportunity(document);
            }
        } catch(Exception ex) {
            LogManager.getLogger("OpportunityDAO.class").error(ex.getMessage());
            throw new DAOException(ex);
        }
        return null;
    }

    public Opportunity createOpportunity(Opportunity opportunity) throws DAOException{
        // 0. Check if the user has already created 10 opportunities
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
            long count = collection.countDocuments(eq("publisher._id", opportunity.getPublisher().getString("_id")));
            if (count >= 10) {
                throw new DAOException("User has already created 10 opportunities");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        // 1. Create a new opportunity in MongoDB
        String uniqueID = UUID.randomUUID().toString();
        opportunity.set_id(uniqueID);
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
        String formattedQuery = "CREATE (o:Opportunity {_id: %s, title: %s, genres: %s, instruments: %s}) " +
                "WITH o " +
                "MATCH (u:%s {_id: %s}) " +
                "CREATE (u)-[:PUBLISHED]->(o)";

        String query = String.format(formattedQuery,
                "\"" + opportunity.get_id() + "\"",
                "\"" + opportunity.getTitle() + "\"",
                "\"" + opportunity.getGenres() + "\"",
                "\"" + opportunity.getInstruments() + "\"",
                opportunity.getPublisher().getString("type"),
                "\"" + opportunity.getPublisher().getString("_id") + "\"");

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

        return new Opportunity(document);
    }

    public void deleteOpportunityById(String id) throws DAOException {
        // 1. Delete the opportunity in MongoDB
        MongoCursor<Document> cursor = null;
        Document deletedDocument = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
            deletedDocument = collection.findOneAndDelete(eq("_id", id));
            if (deletedDocument == null) {
                LogManager.getLogger("OpportunityDAO").warn("Opportunity not found with id " + id + " in MongoDB");
                throw new Exception("Opportunity not found");
            }
        } catch(Exception ex) {
            LogManager.getLogger("OpportunityDAO").error(ex.getMessage());
            throw new DAOException(ex);
        } finally {
            if(cursor != null)
                cursor.close();
        }

        // 2. Add task to update redundancies in the background (eventual consistency)
        MongoUpdater.getInstance().pushTask(new MongoTask("DELETE_OPPORTUNITY", deletedDocument));

        // 3. Delete the node in Neo4j
        String formattedQuery = "MATCH (o:Opportunity {_id: %s}) " +
                                "DETACH DELETE o";

        String query = String.format(formattedQuery,
                "\"" + id + "\"");

        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
            session.executeWrite(tx -> {
                tx.run(query);
                return null;
            });
        } catch (TransactionTerminatedException e) {
            // Add this task to a queue to be executed later from the Neo4jConsistencyManager
            LogManager.getLogger("OpportunityDAO").warn(e.getMessage());
            Neo4jConsistencyManager.getInstance().pushOperation(query);
        } catch (Exception e) {
            LogManager.getLogger("OpportunityDAO").error(e.getMessage());
            throw new DAOException(e.getMessage());
        }
    }

    public List<Opportunity> searchOpportunities(String forUser, String publisherUsername, List<String> genres, List<String> instruments, Location location, Integer maxDistance, Integer minAge, Integer maxAge, String gender, Integer page, Integer pageSize) throws DAOException {
        // Create a new list of filters
        List<Bson> filters = new ArrayList<>();

        String byUser = switch (forUser) {
            case "Musician" -> "Band";
            case "Band" -> "Musician";
            default -> throw new IllegalArgumentException("Invalid type");
        };
        filters.add(Filters.eq("publisher.type", byUser));

        // Add filters based on the provided parameters
        if (publisherUsername != null && !publisherUsername.isEmpty()) {
            filters.add(Filters.eq("publisher.username", publisherUsername));
        }
        if (genres != null && !genres.isEmpty()) {
            filters.add(Filters.in("genres", genres));
        }
        if (forUser.equals("Musician") && instruments != null && !instruments.isEmpty()) {
            filters.add(Filters.in("instruments", instruments));
        }
        if (location != null && !location.getCity().isEmpty()) {
            filters.add(Filters.near("location.geojson", new Point(new Position(location.getGeojson().getCoordinates().get(0), location.getGeojson().getCoordinates().get(1))), maxDistance.doubleValue()*1000, null));
        }
        if (forUser.equals("Musician") && minAge != null && minAge > 0) {
            filters.add(gte("minimumAge", minAge));
        }
        if (forUser.equals("Musician") && maxAge != null && maxAge > 0) {
            filters.add(Filters.lte("maximumAge", maxAge));
        }
        if (forUser.equals("Musician") && gender != null && !gender.equals("-")) {
            filters.add(Filters.eq("gender", gender));
        }

        // Combine all filters. If there are no filters create an empty filter
        Bson filter = filters.isEmpty() ? new Document() : Filters.and(filters);
        // Calculate the number of documents to skip
        int skip = (page - 1) * pageSize;
        LogManager.getLogger("OpportunityDAO").info("Opportunity filter: " + filter);
        // Execute the query
        try (MongoCursor<Document> cursor = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY).find(filter).skip(skip).limit(pageSize).iterator()) {
            List<Opportunity> opportunities = new ArrayList<>();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                opportunities.add(new Opportunity(doc));
            }
            return opportunities;
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    public List<Document> getTopAppliedOpportunities() throws DAOException {
        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

        return collection.aggregate(Arrays.asList(
                Aggregates.addFields(new Field("numApplications", new Document("$size", "$applications"))),
                Aggregates.sort(Sorts.descending("numApplications")),
                Aggregates.project(fields(
                        include("_id", "title", "numApplications", "publisher.type")
                )),
                Aggregates.limit(5)
        )).into(new ArrayList<>());
    }

    public List<Document> getOpportunitiesByAgeRange() throws DAOException {
        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

        return collection.aggregate(Arrays.asList(
                // add a field to check if the opportunity is for musicians, checking the type of the publisher
                Aggregates.addFields(new Field("isForMusicians", new Document("$eq", Arrays.asList("$publisher.type", "Band")))),
                // filter the opportunities that are for musicians
                Aggregates.match(Filters.eq("isForMusicians", true)),
                // filter the opportunities that have a minimum and maximum age
                Aggregates.match(Filters.and(
                        Filters.ne("minimumAge", null),
                        Filters.and(
                            Filters.ne("maximumAge", null),
                            Filters.ne("maximumAge", 0)
                        )
                )),
                // add a field to calculate the average age of the musicians
                Aggregates.addFields(new Field("averageAge", new Document("$avg", Arrays.asList("$minimumAge", "$maximumAge")))),
                // group the opportunities by the average age creating 10 groups of 10 years each
                Aggregates.group(new Document("averageAge",
                        new Document("$subtract", Arrays.asList("$averageAge",
                                new Document("$mod", Arrays.asList("$averageAge", 10))
                        ))
                ), Accumulators.sum("count", 1)),
                // project the fields to be returned
                Aggregates.project(fields(
                        include("_id", "count"),
                        Projections.computed("ageRange", new Document("$concat", Arrays.asList(
                                new Document("$toString", "$_id.averageAge"),
                                new Document("$concat", Arrays.asList(" - ", new Document("$toString", new Document("$add", Arrays.asList("$_id.averageAge", 10)))))
                        )))
                )),
                Aggregates.sort(Sorts.ascending("_id"))
        )).into(new ArrayList<>());
    }

    public List<Document> getTopGenres() throws DAOException {
        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

        return collection.aggregate(Arrays.asList(
                // unwind the genres array
                Aggregates.unwind("$genres"),
                // group the opportunities by genre
                Aggregates.group("$genres", Accumulators.sum("count", 1)),
                // sort the genres by count
                Aggregates.sort(Sorts.descending("count")),
                // project the fields to be returned
                Aggregates.project(fields(
                        include("_id", "count")
                )),
                // limit the results to the top 5 genres
                Aggregates.limit(5)
        )).into(new ArrayList<>());
    }

    public List<Document> getTopLocationsForOpportunities() throws DAOException {
        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
        return collection.aggregate(Arrays.asList(
                // group the opportunities by the city, country
                Aggregates.group(new Document("city", "$location.city")
                                .append("country", "$location.country")
                                .append("state", "$location.state"),
                        Accumulators.sum("totalOpportunities", 1)),
                // project the fields to be returned
                Aggregates.project(fields(
                        include("_id", "totalOpportunities")
                )),
                // sort the results by the count in descending order
                Aggregates.sort(Sorts.descending("totalOpportunities")),
                Aggregates.limit(10)
        )).into(new ArrayList<>());
    }

    public List<Document> getTopPublishers() throws DAOException {
        MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

        LocalDate oneWeekAgoLocalDate = LocalDate.now().minusWeeks(1);
        Date oneWeekAgo = Date.from(oneWeekAgoLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Bson match = Aggregates.match(gte("createdAt", oneWeekAgoLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        Bson group = Aggregates.group("$publisher._id",
                Accumulators.first("username", "$publisher.username"),
                Accumulators.first("profilePictureUrl", "$publisher.profilePictureUrl"),
                Accumulators.first("type", "$publisher.type"),
                Accumulators.sum("totalApplications", new Document("$size", "$applications")),
                Accumulators.sum("totalOpportunities", 1)
        );
        Bson project = Aggregates.project(fields(include("username", "totalApplications", "totalOpportunities", "profilePictureUrl", "type")));
        Bson sort = Aggregates.sort(Sorts.descending("totalApplications"));
        Bson limit = Aggregates.limit(5);

        return collection.aggregate(Arrays.asList(
                match, group, project, sort, limit
        )).into(new ArrayList<>());
    }
}
