package com.lsmsdb.jamsync.service.factory;

import com.lsmsdb.jamsync.service.OpportunityService;
import com.lsmsdb.jamsync.service.impl.OpportunityServiceImpl;

public class OpportunityServiceFactory {

    private OpportunityServiceFactory() {}
    public static OpportunityServiceFactory create() {
        return new OpportunityServiceFactory();
    }

    public OpportunityService getService() {
        return new OpportunityServiceImpl();
    }




}
