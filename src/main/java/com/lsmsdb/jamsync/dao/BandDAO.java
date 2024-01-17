package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.repository.MongoDriver;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class BandDAO {
    public Band getBandById(String id) throws DAOException {
        MongoCursor<Document> cursor = null;
        try {
            MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.BAND);
            cursor = collection.find(eq("_id", id)).iterator();
            if(cursor.hasNext()) {
                Document document = cursor.next();
                return new Band(document);
            }
        } catch(Exception ex) {
            throw new DAOException(ex);
        } finally {
            if(cursor != null) cursor.close();
        }
        return null;
    }
}
