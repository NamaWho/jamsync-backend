package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.service.GenreService;
import com.lsmsdb.jamsync.service.InstrumentService;

import java.util.Arrays;
import java.util.List;

public class GenreServiceImpl implements GenreService {

    private static final List<String> ALLOWED_GENRES = Arrays.asList(
            "Rock",
            "Pop",
            "Jazz",
            "Blues",
            "Country",
            "Folk",
            "Classical",
            "Electronic",
            "Hip Hop",
            "Rap",
            "Reggae",
            "Metal",
            "Punk",
            "Alternative",
            "Indie",
            "Other"
    );

    @Override
    public List<String> getGenres() {
        return ALLOWED_GENRES;
    }
}
