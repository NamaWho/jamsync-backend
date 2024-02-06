package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.AuthDAO;
import com.lsmsdb.jamsync.service.AuthService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.utils.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthServiceImpl implements AuthService {
    private final static AuthDAO authDAO = new AuthDAO();

    @Override
    public String login(String type, String user, String password) throws BusinessException {
        try {
            return authDAO.login(type, user, password);
 /*           if (result) {
                return JwtUtil.generateToken(user);
            }
            else
                return null;*/
        } catch (Exception ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public boolean checkUsername(String username, String type) throws BusinessException {
        try {
            return authDAO.checkUsername(username, type);
        } catch (Exception ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public void banUser(String id, String type) throws BusinessException {
        try {
            authDAO.banUser(id, type);
        } catch (Exception ex) {
            throw new BusinessException(ex);
        }
    }
}
