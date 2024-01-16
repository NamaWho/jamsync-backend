package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.service.factory.MusicianServiceFactory;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lsmsdb.jamsync.service.MusicianService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
@RestController
@RequestMapping("/api/musicians")
public class MusicianController {

    private MusicianService musicianService;

    public MusicianController(){
        this.musicianService = MusicianServiceFactory.create().getService();
    }

    @GetMapping("/{id}")
    public Musician getMusicianById(@PathVariable String id) {
        try {
            return musicianService.getMusicianById(id);
        } catch (BusinessException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}
