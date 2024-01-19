package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.service.RegisteredUserService;
import com.lsmsdb.jamsync.service.factory.MusicianServiceFactory;
import com.lsmsdb.jamsync.service.factory.RegisteredUserServiceFactory;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.lsmsdb.jamsync.service.MusicianService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
@RestController
@RequestMapping("/api/musicians")
public class MusicianController {

    private MusicianService musicianService;
    private RegisteredUserService registeredUserService;

    public MusicianController(){
        this.musicianService = MusicianServiceFactory.create().getService();
        this.registeredUserService = RegisteredUserServiceFactory.create().getService();
    }

    @GetMapping("/{id}")
    public Response getMusicianById(@PathVariable String id) {
        try {
            Musician m = musicianService.getMusicianById(id);
            return new Response(false,"", m);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @GetMapping("/{id}/followers")
    public Response getFollowersCount(@PathVariable String id) {
        try {
            Integer followersCount = registeredUserService.getFollowersCount(id, "Musician");
            return new Response(false,"", followersCount);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @GetMapping("/{id}/following")
    public Response getFollowingCount(@PathVariable String id) {
        try {
            Integer followingCount = musicianService.getFollowingCount(id);
            return new Response(false,"", followingCount);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @PostMapping("/{id}/follow")
    public Response follow(@PathVariable String id, @RequestParam String userId, @RequestParam String type) {
        if (!type.equals("Musician") && !type.equals("Band")) {
            return new Response(true, "Invalid type", null);
        }
        try {
            musicianService.follow(id, userId, type);
            return new Response(false,"", null);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }
}
