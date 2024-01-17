package com.lsmsdb.jamsync.service.factory;

import com.lsmsdb.jamsync.service.ApplicationService;
import com.lsmsdb.jamsync.service.impl.ApplicationServiceImpl;

public class ApplicationServiceFactory {
    private ApplicationServiceFactory() {}

    public static ApplicationServiceFactory create() { return new ApplicationServiceFactory(); }

    public ApplicationService getService() { return new ApplicationServiceImpl(); }
}
