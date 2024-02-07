package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.model.Location;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.RegisteredUser;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegisteredUserDAO {
    /**
     * Search for a user by some filters
     * @param type the type of user to search for
     *             (musician or band)
     * @param username the username of the user to search for (optional)
     * @param genres the genres of the user to search for (optional)
     * @param instruments the instruments of the musician to search for (optional)
     * @param location the location of the user to search for (optional)
     * @param maxDistance the maximum distance from the location of the user to search for (optional)
     * @param minAge the minimum age of the user to search for (optional)
     * @param maxAge the maximum age of the user to search for (optional)
     * @param gender the gender of the user to search for (optional)
     * @param page the page of the results to return (optional) (default: 1)
     * @param pageSize the number of results to return (optional) (default: 10)
     * @return
     * @throws DAOException
     */
    public List<RegisteredUser> searchUser(String type,
                                           String username,
                                           List<String> genres,
                                           List<String> instruments,
                                           Location location,
                                           Integer maxDistance,
                                           Integer minAge,
                                           Integer maxAge,
                                           String gender,
                                           Integer page,
                                           Integer pageSize) throws DAOException {
        MongoCollectionsEnum collectionType = switch (type) {
            case "Musician" -> MongoCollectionsEnum.MUSICIAN;
            case "Band" -> MongoCollectionsEnum.BAND;
            default -> throw new IllegalArgumentException("Invalid type");
        };

        // Create a new list of filters
        List<Bson> filters = new ArrayList<>();

        // Add filters based on the provided parameters
        if (username != null && !username.isEmpty()) {
            filters.add(Filters.eq("username", username));
        }
        if (genres != null && !genres.isEmpty()) {
            filters.add(Filters.in("genres", genres));
        }
        if (type.equals("Musician") && instruments != null && !instruments.isEmpty()) {
            filters.add(Filters.in("instruments", instruments));
        }
        if (location != null && !location.getCity().isEmpty()) {
            filters.add(Filters.
                    near("location.geojson",
                            new Point(new Position(location.getGeojson().getCoordinates().get(0)
                                    , location.getGeojson().getCoordinates().get(1)))
                                    , maxDistance.doubleValue()*1000, null));
        }
        if (type.equals("Musician") && minAge != null && minAge > 0) {
            filters.add(Filters.gte("age", minAge));
        }
        if (type.equals("Musician") && maxAge != null && maxAge > 0) {
            filters.add(Filters.lte("age", maxAge));
        }
        if (type.equals("Musician") && gender != null && !gender.equals("-")) {
            filters.add(Filters.eq("gender", gender));
        }

        // Combine all filters. If there are no filters create an empty filter
        Bson filter = filters.isEmpty() ? new Document() : Filters.and(filters);
        // Calculate the number of documents to skip
        int skip = (page - 1) * pageSize;
        // Execute the query
        try (MongoCursor<Document> cursor = MongoDriver.getInstance().getCollection(collectionType).find(filter).skip(skip).limit(pageSize).iterator()) {
            List<RegisteredUser> users = new ArrayList<>();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                users.add(switch (type) {
                    case "Musician" -> new Musician(doc);
                    case "Band" -> new Band(doc);
                    default -> throw new IllegalArgumentException("Invalid type");
                });
            }
            return users;
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    public Integer getFollowersCount(String _id, String type) throws DAOException {
        try (Session session = Neo4jDriver.getInstance().getDriver().session()) {

            return session.executeRead(tx -> {
                String query = String.format("MATCH (:%s {_id: $targetId})<-[:FOLLOWS]-(follower:Musician) RETURN COUNT(follower)",
                        type);
                Result result = tx.run(query, Values.parameters("targetId", _id));
                if (result.hasNext()) {
                    Record record = result.next();
                    return record.get(0).asInt();
                }
                return 0;
            });
        } catch (Neo4jException e) {
            throw new DAOException("Error while getting followers count");
        }
    }
}
