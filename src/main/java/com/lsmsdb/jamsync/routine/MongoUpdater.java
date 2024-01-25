package com.lsmsdb.jamsync.routine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.lsmsdb.jamsync.dao.BandDAO;
import com.lsmsdb.jamsync.dao.MusicianDAO;
import com.lsmsdb.jamsync.dao.OpportunityDAO;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class MongoUpdater {
    private static final MongoUpdater updater = new MongoUpdater();
    private final static MusicianDAO musicianDAO = new MusicianDAO();
    private final static BandDAO bandDAO = new BandDAO();
    private final static OpportunityDAO opportunityDAO = new OpportunityDAO();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<MongoTask> updateTasks = new ArrayList<MongoTask>();
    private List<MongoTask> failedTasks = new ArrayList<MongoTask>();

    private MongoUpdater() {
        LogManager.getLogger("MongoUpdater").info("MongoUpdater constructor called...");
        // Schedule the updater to run every 2 minutes
        scheduler.scheduleAtFixedRate(this::updateMongoData, 0, 10, TimeUnit.SECONDS);
    }

    public static MongoUpdater getInstance() { return updater; }

    public void pushTask(MongoTask mongoTask) {
        updateTasks.add(mongoTask);
    }

    private MongoTask popTask() {
        if (updateTasks.isEmpty()) {
            return null;
        }
        return updateTasks.remove(0);
    }

    private void retryFailedTasks() {
        for (MongoTask task : failedTasks) {
            pushTask(task);
        }
        failedTasks.clear();
    }

    private void updateMongoData() {
        LogManager.getLogger("MongoUpdater").info("updateMongoData routine started...");
        retryFailedTasks();

        MongoTask mongoTask = popTask();
        while (mongoTask != null) {
            switch (mongoTask.getOperation()) {
                case "CREATE_APPLICATION":
                    createApplication(mongoTask);
                    break;
                case "DELETE_APPLICATION":
                    deleteApplication(mongoTask);
                    break;
                case "CREATE_OPPORTUNITY":
                    createOpportunity(mongoTask);
                    break;
                case "DELETE_OPPORTUNITY":
                    deleteOpportunity(mongoTask);
                    break;
                case "UPDATE_MUSICIAN":
                    updateMusician(mongoTask);
                    break;
                case "DELETE_MUSICIAN":
                    deleteMusician(mongoTask);
                    break;
                case "UPDATE_BAND":
                    updateBand(mongoTask);
                    break;
                case "DELETE_BAND":
                    deleteBand(mongoTask);
                    break;
                default:
                    break;
            }
            mongoTask = popTask();
        }
    }

    private void createApplication(MongoTask mongoTask){
        LogManager.getLogger("MongoUpdater").info("createApplication routine started...");
        Document application = mongoTask.getDocument();
        Document applicant = (Document) application.get("applicant");
        String applicantId = applicant.getString("_id");
        String applicantType = application.getString("applicantType");

        MongoCursor<Document> cursor = null;
        try {
            // 1. update the applications array in the applicant document
            MongoCollectionsEnum collectionName = MongoCollectionsEnum.valueOf(applicantType.toUpperCase());
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(collectionName);
            cursor = collection.find(eq("_id", applicantId)).iterator();
            if(cursor.hasNext()) {
                Document document = cursor.next();
                List<Document> applications = (List<Document>) document.get("applications");
                // add the new application
                Document applicationEntry = new Document();
                applicationEntry.put("_id", application.getString("_id"));
                applicationEntry.put("createdAt", application.getString("createdAt"));
                applicationEntry.put("title", application.getString("title"));
                applications.add(applicationEntry);
                // update the applications array
                document.put("applications", applications);
                // update the applicant document
                collection.updateOne(eq("_id", applicantId), new Document("$set", document));
            }
        } catch (Exception ex) {
            failedTasks.add(mongoTask);
            LogManager.getLogger("MongoUpdater").error(ex.getMessage());
        } finally {
            if(cursor != null)
                cursor.close();
        }
    }

    private void deleteApplication(MongoTask mongoTask){
        LogManager.getLogger("MongoUpdater").info("deleteApplication routine started...");
        Document application = mongoTask.getDocument();
        Document applicant = (Document) application.get("applicant");
        String applicantId = applicant.getString("_id");
        String applicantType = application.getString("applicantType");

        MongoCursor<Document> cursor = null;
        try {
            // 1. remove application from the applicant document
            MongoCollectionsEnum collectionName = MongoCollectionsEnum.valueOf(applicantType.toUpperCase());
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(collectionName);
            cursor = collection.find(eq("_id", applicantId)).iterator();
            if(cursor.hasNext()) {
                Document document = cursor.next();
                List<Document> applications = (List<Document>) document.get("applications");
                // remove the application
                applications.removeIf(a -> a.getString("_id").equals(application.getString("_id")));
                // update the applications array
                document.put("applications", applications);
                // update the applicant document
                collection.updateOne(eq("_id", applicantId), new Document("$set", document));
            }
        } catch (Exception ex) {
            failedTasks.add(mongoTask);
            LogManager.getLogger("MongoUpdater").error(ex.getMessage());
        } finally {
            if(cursor != null)
                cursor.close();
        }
    }

    /**
     * After the creation of a new opportunity, it's needed to update the publisher document
     * (either a Musician or a Band) by adding the new opportunity details to the opportunities array.
     * @param mongoTask
     */
    private void createOpportunity(MongoTask mongoTask) {
        LogManager.getLogger("MongoUpdater").info("createOpportunity routine started...");
        Document opportunity = mongoTask.getDocument();
        Document publisher = (Document) opportunity.get("publisher");
        String publisherId = publisher.getString("_id");
        String type = publisher.getString("type");
        MongoCollectionsEnum collectionName = MongoCollectionsEnum.valueOf(type.toUpperCase());

        MongoCursor<Document> cursor = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(collectionName);
            cursor = collection.find(eq("_id", publisherId)).iterator();
            if(cursor.hasNext()) {
                Document document = cursor.next();
                // retrieve the opportunities array
                List<Document> opportunities = (List<Document>) document.get("opportunities");
                // add the new opportunity
                Document opportunityEntry = new Document();
                opportunityEntry.put("_id", opportunity.getString("_id"));
                opportunityEntry.put("title", opportunity.getString("title"));
                opportunityEntry.put("createdAt", opportunity.getString("createdAt"));
                opportunities.add(opportunityEntry);
                // update the opportunities array
                document.put("opportunities", opportunities);
                // update the publisher document
                collection.updateOne(eq("_id", publisherId), new Document("$set", document));
            }
        } catch (Exception ex) {
            failedTasks.add(mongoTask);
            LogManager.getLogger("MongoUpdater").error(ex.getMessage());
        } finally {
            if(cursor != null)
                cursor.close();
        }
    }

    private void deleteOpportunity(MongoTask mongoTask) {
        LogManager.getLogger("MongoUpdater").info("deleteOpportunity routine started...");
        Document opportunity = mongoTask.getDocument();
        MongoCursor<Document> cursor = null;

        try {
            // 1. remove opportunity from the publisher document
            Document publisher = (Document) opportunity.get("publisher");
            String publisherId = publisher.getString("_id");
            String publisherType = publisher.getString("type");
            MongoCollectionsEnum collectionName = MongoCollectionsEnum.valueOf(publisherType.toUpperCase());
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(collectionName);
            cursor = collection.find(eq("_id", publisherId)).iterator();
            if(cursor.hasNext()) {
                Document document = cursor.next();
                // retrieve the opportunities array
                List<Document> opportunities = (List<Document>) document.get("opportunities");
                // remove the opportunity
                opportunities.removeIf(o -> o.getString("_id").equals(opportunity.getString("_id")));
                // update the opportunities array
                document.put("opportunities", opportunities);
                // update the publisher document
                collection.updateOne(eq("_id", publisherId), new Document("$set", document));
            }

            // 2. remove opportunity from the applications documents in the applicants documents
            List<WriteModel<Document>> bulkWrites = new ArrayList<>();
            List<Document> applications = (List<Document>) opportunity.get("applications");
            String applicantType = publisherType.equals("Musician") ? "BAND" : "MUSICIAN";
            collectionName = MongoCollectionsEnum.valueOf(applicantType.toUpperCase());

            for(Document application : applications) {
                Document applicant = (Document) application.get("applicant");
                String applicantId = applicant.getString("_id");

                bulkWrites.add(new UpdateOneModel<>(
                        Filters.eq("_id", applicantId),
                        Updates.pull("applications", Filters.eq("_id", application.getString("_id")))
                ));
            }

            if (bulkWrites.isEmpty()) return;
            collection = MongoDriver.getInstance().getCollection(collectionName);
            BulkWriteResult result = collection.bulkWrite(bulkWrites);
        } catch (Exception ex) {
            failedTasks.add(mongoTask);
            LogManager.getLogger("MongoUpdater").error(ex.getMessage());
        }
    }

    private void updateMusician(MongoTask mongoTask){
        LogManager.getLogger("MongoUpdater").info("updateMusician routine started...");
        Document musician = mongoTask.getDocument();

        try {
            // 1. update the musician's data in his applications among the opportunities documents
            List<WriteModel<Document>> bulkWrites = new ArrayList<>();
            Document newApplicant = new Document();
            newApplicant.put("_id", musician.getString("_id"));
            newApplicant.put("username", musician.getString("username"));
            newApplicant.put("profilePictureUrl", musician.getString("profilePictureUrl"));
            newApplicant.put("contactEmail", musician.getString("contactEmail"));
            List<Document> applications = (List<Document>) musician.get("applications");
            for(Document application : applications) {
                bulkWrites.add(new UpdateOneModel<>(
                        Filters.elemMatch("applications", Filters.eq("_id", application.getString("_id"))),
                        Updates.set("applications.$.applicant", newApplicant)
                ));
            }

            // 2. update the musician's data in his published opportunities
            List<Document> opportunities = (List<Document>) musician.get("opportunities");
            Document newPublisher = new Document();
            newPublisher.put("_id", musician.getString("_id"));
            newPublisher.put("type", "Musician");
            newPublisher.put("username", musician.getString("username"));
            newPublisher.put("profilePictureUrl", musician.getString("profilePictureUrl"));
            for(Document opportunity : opportunities) {
                bulkWrites.add(new UpdateOneModel<>(
                        Filters.eq("_id", opportunity.getString("_id")),
                        Updates.set("publisher", newPublisher)
                ));
            }

            if (!bulkWrites.isEmpty()){
                MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
                BulkWriteResult result = collection.bulkWrite(bulkWrites);
            }
        } catch (Exception ex) {
            failedTasks.add(mongoTask);
            LogManager.getLogger("MongoUpdater").error(ex.getMessage());
        }
    }

    private void deleteMusician(MongoTask mongoTask){
        LogManager.getLogger("MongoUpdater").info("deleteMusician routine started...");
        Document musician = mongoTask.getDocument();

        try {
            // 1. remove all the opportunities published by the musician
            List<Document> opportunities = (List<Document>) musician.get("opportunities");
            for(Document opportunity : opportunities) {
                opportunityDAO.deleteOpportunityById(opportunity.getString("_id"));
            }

            // 2. remove musician's applications in the opportunities documents
            List<WriteModel<Document>> bulkWrites = new ArrayList<>();
            List<Document> applications = (List<Document>) musician.get("applications");
            for(Document application : applications) {
                String applicationId = application.getString("_id");

                bulkWrites.add(new UpdateOneModel<>(
                        Filters.elemMatch("applications", Filters.eq("_id", applicationId)),
                        Updates.pull("applications", Filters.eq("_id", application.getString("_id")))
                ));
            }

            if (bulkWrites.isEmpty()) return;
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
            BulkWriteResult result = collection.bulkWrite(bulkWrites);
        } catch (Exception ex) {
            failedTasks.add(mongoTask);
            LogManager.getLogger("MongoUpdater").error(ex.getMessage());
        }
    }

    private void updateBand(MongoTask mongoTask){
        LogManager.getLogger("MongoUpdater").info("updateBand routine started...");
        Document band = mongoTask.getDocument();

        try {
            // 1. update the band's data in his applications among the opportunities documents
            List<WriteModel<Document>> bulkWrites = new ArrayList<>();
            Document newApplicant = new Document();
            newApplicant.put("_id", band.getString("_id"));
            newApplicant.put("username", band.getString("username"));
            newApplicant.put("profilePictureUrl", band.getString("profilePictureUrl"));
            newApplicant.put("contactEmail", band.getString("contactEmail"));
            List<Document> applications = (List<Document>) band.get("applications");
            for(Document application : applications) {
                bulkWrites.add(new UpdateOneModel<>(
                        Filters.elemMatch("applications", Filters.eq("_id", application.getString("_id"))),
                        Updates.set("applications.$.applicant", newApplicant)
                ));
            }

            // 2. update the band's data in his published opportunities
            List<Document> opportunities = (List<Document>) band.get("opportunities");
            Document newPublisher = new Document();
            newPublisher.put("_id", band.getString("_id"));
            newPublisher.put("type", "Band");
            newPublisher.put("username", band.getString("username"));
            newPublisher.put("profilePictureUrl", band.getString("profilePictureUrl"));
            for(Document opportunity : opportunities) {
                bulkWrites.add(new UpdateOneModel<>(
                        Filters.eq("_id", opportunity.getString("_id")),
                        Updates.set("publisher", newPublisher)
                ));
            }

            if (!bulkWrites.isEmpty()){
                MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
                BulkWriteResult result = collection.bulkWrite(bulkWrites);
            }
        } catch (Exception ex) {
            failedTasks.add(mongoTask);
            LogManager.getLogger("MongoUpdater").error(ex.getMessage());
        }
    }

    private void deleteBand(MongoTask mongoTask){
        LogManager.getLogger(MongoUpdater.class).info("deleteBand routine started...");
        Document band = mongoTask.getDocument();
        MongoCursor<Document> cursor = null;

        try {
            // 1. remove all the opportunities published by the band
            List<Document> opportunities = (List<Document>) band.get("opportunities");
            for(Document opportunity : opportunities) {
                opportunityDAO.deleteOpportunityById(opportunity.getString("_id"));
            }

            // 2. remove band's applications in the opportunities documents
            List<WriteModel<Document>> bulkWrites = new ArrayList<>();
            List<Document> applications = (List<Document>) band.get("applications");
            for(Document application : applications) {
                String applicationId = application.getString("_id");

                bulkWrites.add(new UpdateOneModel<>(
                        Filters.elemMatch("applications", Filters.eq("_id", applicationId)),
                        Updates.pull("applications", Filters.eq("_id", application.getString("_id")))
                ));
            }

            if (bulkWrites.isEmpty()) return;
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.OPPORTUNITY);
            BulkWriteResult result = collection.bulkWrite(bulkWrites);
        } catch (Exception ex) {
            failedTasks.add(mongoTask);
            LogManager.getLogger("MongoUpdater").error(ex.getMessage());
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
