package com.lsmsdb.jamsync.service.factory;

import com.lsmsdb.jamsync.service.GenreService;
import com.lsmsdb.jamsync.service.InstrumentService;
import com.lsmsdb.jamsync.service.impl.GenreServiceImpl;
import com.lsmsdb.jamsync.service.impl.InstrumentServiceImpl;

public class GenreServiceFactory {
    private GenreServiceFactory() {}

    public static GenreServiceFactory create() { return new GenreServiceFactory(); }

    public GenreService getService() { return new GenreServiceImpl(); }
}
