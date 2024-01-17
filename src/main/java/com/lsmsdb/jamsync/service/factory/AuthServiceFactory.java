package com.lsmsdb.jamsync.service.factory;

import com.lsmsdb.jamsync.service.AuthService;
import com.lsmsdb.jamsync.service.impl.AuthServiceImpl;

public class AuthServiceFactory {

    private AuthServiceFactory() {}

    public static AuthServiceFactory create() {
        return new AuthServiceFactory();
    }

    public AuthService getService() {
        return new AuthServiceImpl();
    }
}
