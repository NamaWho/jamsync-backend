package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import org.bson.Document;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import java.util.List;

public class RegisteredUserDAO {

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
