package com.lsmsdb.jamsync.service.factory;

import com.lsmsdb.jamsync.service.ApplicationService;
import com.lsmsdb.jamsync.service.InstrumentService;
import com.lsmsdb.jamsync.service.impl.ApplicationServiceImpl;
import com.lsmsdb.jamsync.service.impl.InstrumentServiceImpl;

public class InstrumentServiceFactory {
    private InstrumentServiceFactory() {}

    public static InstrumentServiceFactory create() { return new InstrumentServiceFactory(); }

    public InstrumentService getService() { return new InstrumentServiceImpl(); }
}
