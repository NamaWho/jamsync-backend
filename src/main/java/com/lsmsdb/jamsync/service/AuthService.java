package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.service.exception.BusinessException;

public interface AuthService {

    public String login(String type, String user, String password) throws BusinessException;
}
