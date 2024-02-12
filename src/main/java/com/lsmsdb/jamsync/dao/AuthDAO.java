package com.lsmsdb.jamsync.dao;

import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.dao.utils.HashUtil;
import com.lsmsdb.jamsync.model.Credentials;
import com.lsmsdb.jamsync.model.Location;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.RegisteredUser;
import com.lsmsdb.jamsync.repository.enums.MongoCollectionsEnum;
import com.lsmsdb.jamsync.service.utils.JwtUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;


import com.lsmsdb.jamsync.repository.MongoDriver;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public class AuthDAO {

    public String login(String type, String user, String password) throws DAOException{
        MongoCollectionsEnum collectionType;
        switch (type) {
            case "musician":
                collectionType = MongoCollectionsEnum.MUSICIAN;
                break;
            case "band":
                collectionType = MongoCollectionsEnum.BAND;
                break;
            case "admin":
                if (user.equals("administrator") && password.equals("administrator"))
                    return JwtUtil.generateToken(user, type, new Document());
                else
                    return null;
            default:
                throw new IllegalArgumentException("Invalid type");
        }

        /*try(MongoCursor<Document> cursor = MongoDriver.getInstance().getCollection(collectionType).find(eq("credentials.user", user)).iterator()){
            if(cursor.hasNext()){*/
        try {
                MongoCollection<Document> collection = MongoDriver.getInstance().getCollection(collectionType);
                Bson filter = eq("credentials.user", user);
                Bson update = set("lastLoginDateTime", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                Document document = collection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
                //Document doc = cursor.next();

            if (document != null) {
                //if isBanned is set, get the value, check it and return null if true
                boolean isBanned = document.containsKey("isBanned");
                if(isBanned)
                    return null;

                String hashedPassword = new Credentials((Document) document.get("credentials")).getPassword();
                String hashedInputPassword = HashUtil.hashPassword(password);

                if(hashedPassword.equals(hashedInputPassword))
                    return JwtUtil.generateToken(user, type, document);
                else
                    return null;
            }
        }catch(Exception ex){
            throw new DAOException(ex);
        }
        return null;
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