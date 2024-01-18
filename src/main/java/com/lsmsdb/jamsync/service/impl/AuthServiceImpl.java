package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.AuthDAO;
import com.lsmsdb.jamsync.service.AuthService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthServiceImpl implements AuthService {
    private final static AuthDAO authDAO = new AuthDAO();

    @Override
    public String login(String type, String user, String password) throws BusinessException {
        try {
            boolean result = authDAO.login(type, user, password);
            if (result) {
                return JwtUtil.generateToken(user);
            }
            else
                return null;
        } catch (Exception ex) {
            throw new BusinessException(ex);
        }

    }
}
