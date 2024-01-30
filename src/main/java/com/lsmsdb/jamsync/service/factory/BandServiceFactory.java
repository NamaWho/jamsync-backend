package com.lsmsdb.jamsync.service.factory;

import com.lsmsdb.jamsync.service.BandService;
import com.lsmsdb.jamsync.service.impl.BandServiceImpl;

public class BandServiceFactory {

    private BandServiceFactory(){}

    public static BandServiceFactory create() {
        return new BandServiceFactory();
    }

    public BandService getService() {
        return new BandServiceImpl();
    }


}
