package com.lsmsdb.jamsync.routine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class MongoUpdater {

    private static final MongoUpdater updater = new MongoUpdater();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<MongoTask> updateTasks = new ArrayList<MongoTask>();

    private MongoUpdater() {
        System.out.println("MongoUpdater ready...");
        // Schedule the updater to run every 2 minutes
        scheduler.scheduleAtFixedRate(this::updateMongoData, 0, 2, TimeUnit.MINUTES);
    }

    public static MongoUpdater getInstance() { return updater; }

    public void pushTask(MongoTask mongoTask) {
        System.out.println("Pushing task to MongoUpdater...");
        updateTasks.add(mongoTask);
    }

    private MongoTask popTask() {
        if (updateTasks.isEmpty()) {
            return null;
        }
        return updateTasks.remove(0);
    }

    private void updateMongoData() {
        System.out.println("Updating MongoDB data...");
        MongoTask mongoTask = popTask();
        while (mongoTask != null) {
            switch (mongoTask.getOperation()) {
                case "CREATE_APPLICATION":
                    break;
                case "UPDATE_APPLICATION":
                    break;
                case "DELETE_APPLICATION":
                    break;
                case "CREATE_OPPORTUNITY":
                    createOpportunity(mongoTask);
                    break;
                case "DELETE_OPPORTUNITY":
                    break;
                case "UPDATE_MUSICIAN":
                    break;
                case "DELETE_MUSICIAN":
                    break;
                case "UPDATE_BAND":
                    break;
                case "DELETE_BAND":
                    break;
                default:
                    break;
            }
            mongoTask = popTask();
        }
    }

    /**
     * After the creation of a new opportunity, it's needed to update the publisher document
     * (either a Musician or a Band) by adding the new opportunity details to the opportunities array.
     * @param mongoTask
     */
    private void createOpportunity(MongoTask mongoTask) {
        System.out.println("Updating publisher document...");
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
            pushTask(mongoTask);
            throw new RuntimeException(ex);
        } finally {
            if(cursor != null)
                cursor.close();
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
