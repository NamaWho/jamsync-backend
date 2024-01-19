package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.service.exception.BusinessException;
import org.bson.Document;

import java.util.List;

public interface RegisteredUserService {

    public Integer getFollowersCount(String _id, String type) throws BusinessException;
}
