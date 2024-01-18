package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.service.GenreService;
import com.lsmsdb.jamsync.service.InstrumentService;
import com.lsmsdb.jamsync.service.factory.GenreServiceFactory;
import com.lsmsdb.jamsync.service.factory.InstrumentServiceFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private GenreService genreService;

    public GenreController() { this.genreService = GenreServiceFactory.create().getService(); }

    @GetMapping("/")
    public List<String> getGenres() { return genreService.getGenres(); }
}
