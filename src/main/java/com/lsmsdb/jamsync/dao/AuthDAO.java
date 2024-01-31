package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.dao.utils.HashUtil;
import com.lsmsdb.jamsync.model.Credentials;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.RegisteredUser;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import com.lsmsdb.jamsync.repository.MongoDriver;

import java.util.ResourceBundle;

import static com.mongodb.client.model.Filters.eq;

public class AuthDAO {

    public boolean login(String type, String user, String password) throws DAOException{
        MongoCollectionsEnum collectionType;
        switch (type) {
            case "musician":
                collectionType = MongoCollectionsEnum.MUSICIAN;
                break;
            case "band":
                collectionType = MongoCollectionsEnum.BAND;
                break;
            case "admin":
                return user.equals("administrator") && password.equals("administrator");
            default:
                throw new IllegalArgumentException("Invalid type");
        }

        try(MongoCursor<Document> cursor = MongoDriver.getInstance().getCollection(collectionType).find(eq("credentials.user", user)).iterator()){
            if(cursor.hasNext()){
                Document doc = cursor.next();

                String hashedPassword = new Credentials((Document) doc.get("credentials")).getPassword();
                String hashedInputPassword = HashUtil.hashPassword(password);

                if(hashedPassword.equals(hashedInputPassword)){
                    return true;
                }
            }
        }catch(Exception ex){
            throw new DAOException(ex);
        }
        return false;
    }

    public boolean checkUsername(String username, String type) throws DAOException{
        try(MongoCursor<Document> cursor = MongoDriver.getInstance().getCollection(MongoCollectionsEnum.valueOf(type.toUpperCase())).find(eq("credentials.user", username)).iterator()){
            if(cursor.hasNext()){
                return true;
            }
        }catch(Exception ex){
            throw new DAOException(ex);
        }
        return false;
    }

    public void banUser(String id, String type) throws DAOException {
        try {
            MongoDriver.getInstance().getCollection(MongoCollectionsEnum.valueOf(type.toUpperCase())).updateOne(eq("_id", id), new Document("$set", new Document("isBanned", true)));
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }
}
