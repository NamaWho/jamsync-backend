package com.lsmsdb.jamsync.service.factory;

import com.lsmsdb.jamsync.service.MusicianService;
import com.lsmsdb.jamsync.service.impl.MusicianServiceImpl;

public class MusicianServiceFactory {
    private MusicianServiceFactory(){}

    public static MusicianServiceFactory create() {
        return new MusicianServiceFactory();
    }

    public MusicianService getService() {
        return new MusicianServiceImpl();
    }
}
