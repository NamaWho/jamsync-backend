package com.lsmsdb.jamsync.service.factory;

import com.lsmsdb.jamsync.service.OpportunityService;
import com.lsmsdb.jamsync.service.RegisteredUserService;
import com.lsmsdb.jamsync.service.impl.OpportunityServiceImpl;
import com.lsmsdb.jamsync.service.impl.RegisteredUserServiceImpl;

public class RegisteredUserServiceFactory {
    private RegisteredUserServiceFactory() {}
    public static RegisteredUserServiceFactory create() {
        return new RegisteredUserServiceFactory();
    }

    public RegisteredUserService getService() {
        return new RegisteredUserServiceImpl();
    }

}
