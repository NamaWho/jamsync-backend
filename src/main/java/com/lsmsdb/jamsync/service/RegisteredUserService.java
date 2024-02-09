package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.model.Location;
import com.lsmsdb.jamsync.model.RegisteredUser;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import org.bson.Document;

import java.util.List;

public interface RegisteredUserService {

    List<RegisteredUser> searchUser(String type, String username, List<String> genres, List<String> instruments, Location location, Integer maxDistance, Integer minAge, Integer maxAge, String gender, Integer page, Integer pageSize) throws BusinessException;

    public Integer getFollowersCount(String _id, String type) throws BusinessException;

    List<Document> getTopPublishers() throws BusinessException;
}
