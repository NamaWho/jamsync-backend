package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.RegisteredUserDAO;
import com.lsmsdb.jamsync.service.RegisteredUserService;
import com.lsmsdb.jamsync.service.exception.BusinessException;

public class RegisteredUserServiceImpl implements RegisteredUserService {

    private final static RegisteredUserDAO registeredUserDAO = new RegisteredUserDAO();
    @Override
    public Integer getFollowersCount(String _id, String type) throws BusinessException {
        try {
            return registeredUserDAO.getFollowersCount(_id, type);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
