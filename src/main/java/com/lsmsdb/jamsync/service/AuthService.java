package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.service.exception.BusinessException;

public interface AuthService {

    public String login(String type, String user, String password) throws BusinessException;

    boolean checkUsername(String username, String type) throws BusinessException;

    void banUser(String id, String type) throws BusinessException;
}
