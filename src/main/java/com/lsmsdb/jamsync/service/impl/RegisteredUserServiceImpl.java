package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.RegisteredUserDAO;
import com.lsmsdb.jamsync.model.Location;
import com.lsmsdb.jamsync.model.RegisteredUser;
import com.lsmsdb.jamsync.service.RegisteredUserService;
import com.lsmsdb.jamsync.service.exception.BusinessException;

import java.util.List;

public class RegisteredUserServiceImpl implements RegisteredUserService {

    private final static RegisteredUserDAO registeredUserDAO = new RegisteredUserDAO();

    @Override
    public List<RegisteredUser> searchUser(String type, String username, List<String> genres, List<String> instruments, Location location, Integer maxDistance, Integer minAge, Integer maxAge, String gender, Integer page, Integer pageSize) throws BusinessException {
        try {
            return registeredUserDAO.searchUser(type, username, genres, instruments, location, maxDistance, minAge, maxAge, gender, page, pageSize);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public Integer getFollowersCount(String _id, String type) throws BusinessException {
        try {
            return registeredUserDAO.getFollowersCount(_id, type);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
