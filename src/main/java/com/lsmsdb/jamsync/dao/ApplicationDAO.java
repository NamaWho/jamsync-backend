package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Application;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.Neo4jDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.routine.MongoTask;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.lsmsdb.jamsync.routine.Neo4jConsistencyManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.TransactionTerminatedException;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class ApplicationDAO {

    public Opportunity getApplicationById(String applicationId) throws DAOException {
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

            MongoCursor<Document> cursor = collection.find(new Document("applications._id", applicationId)).iterator();

            if (cursor.hasNext()) {
                Document opportunityDocument = cursor.next();

                List<Document> applications = opportunityDocument.getList("applications", Document.class);

                Document applicationDocument = applications.stream()
                        .filter(appDoc -> applicationId.equals(appDoc.getString("_id")))
                        .findFirst()
                        .orElse(null);

                if (applicationDocument != null) {
                    opportunityDocument.remove("applications");
                    opportunityDocument.append("applications", List.of(applicationDocument));

                    return new Opportunity(opportunityDocument);
                }
            } else {
                throw new DAOException("Application not found");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return null;
    }

    public Application createApplication(String opportunityId, Application application) throws DAOException {
        try {
            // 1. Add application to opportunity
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
            Document applicationDocument = application.toDocument();
            Document updatedDocument = collection.findOneAndUpdate(
                    new Document("_id", opportunityId),
                    Updates.push("applications", applicationDocument),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));

            // 2. Add task to queue for update redundancies
            Document publisher = updatedDocument.get("publisher", Document.class);
            String type = publisher.getString("type");
            String applicantType = type.equalsIgnoreCase("band") ? "Musician" : "Band";
            applicationDocument.append("applicantType", applicantType);
            applicationDocument.append("opportunityTitle", updatedDocument.getString("title"));
            MongoUpdater.getInstance().pushTask(new MongoTask("CREATE_APPLICATION", applicationDocument));

            // 3. Add application to Neo4j

            String formattedQuery = "MATCH (o:Opportunity {_id: %s}), (u:%s {_id: %s}) " + "CREATE (u)-[:APPLIED_FOR]->(o)";
            String query = String.format(formattedQuery,
                    "\"" + opportunityId + "\"",
                    applicantType,
                    "\"" + application.getApplicant().getString("_id") + "\"");

            try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
                session.executeWrite(tx -> {
                    tx.run(query);
                    return null;
                });
            } catch (TransactionTerminatedException e) {
                LogManager.getLogger("ApplicationDAO").warn("Transaction terminated. Adding task to queue...");
                Neo4jConsistencyManager.getInstance().pushOperation(query);
            }

            return new Application(applicationDocument);
        } catch (Exception ex) {
            LogManager.getLogger("ApplicationDAO").error(ex.getMessage());
            throw new DAOException(ex);
        }
    }

    public void deleteApplication(String opportunityId, String applicationId) throws DAOException {
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

            Document oldDocument = collection.findOneAndUpdate(
                    new Document("applications._id", applicationId),
                    Updates.pull("applications", eq("_id", applicationId)),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE));

            // 2. Add task to queue for update redundancies
            Document publisher = oldDocument.get("publisher", Document.class);
            String type = publisher.getString("type");
            String applicantType = type.equalsIgnoreCase("band") ? "Musician" : "Band";
            List<Document> applications = oldDocument.getList("applications", Document.class);
            Document applicationDocument = applications.stream()
                    .filter(appDoc -> applicationId.equals(appDoc.getString("_id")))
                    .findFirst()
                    .orElse(null);
            if (applicationDocument != null) {
                applicationDocument.append("applicantType", applicantType);
                MongoUpdater.getInstance().pushTask(new MongoTask("DELETE_APPLICATION", applicationDocument));
            } else
                throw new DAOException("Application not found");

            String formattedQuery = "MATCH (u:%s {_id: %s})-[r:APPLIED_FOR]->(o:Opportunity {_id: %s}) " +
                    "DELETE r";
            String query = String.format(formattedQuery,
                    applicantType,
                    "\"" + ((Document)applicationDocument.get("applicant")).getString("_id") + "\"",
                    "\"" + opportunityId + "\"");

            try (Session session = Neo4jDriver.getInstance().getDriver().session()) {
                session.executeWrite(tx -> {
                    tx.run(query);
                    return null;
                });
            } catch (TransactionTerminatedException e) {
                LogManager.getLogger("ApplicationDAO").warn("Transaction terminated. Adding task to queue...");
                Neo4jConsistencyManager.getInstance().pushOperation(query);
            }
        } catch (Exception ex) {
            LogManager.getLogger("ApplicationDAO").error(ex.getMessage());
            throw new DAOException(ex);
        }
    }

    public void acceptApplication(String applicationId) throws DAOException {
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);

            collection.updateOne(
                    new Document("applications._id", applicationId),
                    Updates.set("applications.$.status", true));

        } catch (Exception ex) {
            LogManager.getLogger("ApplicationDAO").error(ex.getMessage());
            throw new DAOException(ex);
        }
    }
}
