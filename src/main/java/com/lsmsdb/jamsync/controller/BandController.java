package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.service.BandService;
import com.lsmsdb.jamsync.service.RegisteredUserService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.factory.BandServiceFactory;
import com.lsmsdb.jamsync.service.factory.RegisteredUserServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bands")
public class BandController {

    private BandService bandService;
    private RegisteredUserService registeredUserService;

    public BandController(){
        this.bandService = BandServiceFactory.create().getService();
        this.registeredUserService = RegisteredUserServiceFactory.create().getService();
    }

    @PostMapping("/")
    public Response createBand(@RequestBody Band band) {
        if (band.getCredentials() == null) {
            return new Response(true, "Credentials are required", null);
        }

        try {
            bandService.createBand(band);
            return new Response(false,"", null);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @GetMapping("/{id}")
    public Response getBandById(@PathVariable String id) {
        try {
            Band b = bandService.getBandById(id);
            return new Response(false, null, b);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @DeleteMapping("/{id}")
    public Response deleteBandById(@PathVariable String id) {
        LogManager.getLogger(BandController.class).info("Deleting band with id: " + id);
        try {
            bandService.deleteBandById(id);
            return new Response(false,"", null);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @GetMapping("/{id}/followers")
    public Response getFollowersCount(@PathVariable String id) {
        try {
            Integer followersCount = registeredUserService.getFollowersCount(id, "Band");
            return new Response(false,"", followersCount);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @PostMapping("/{id}/member")
    public Response addMember(@PathVariable String id, @RequestParam String memberId) {
        // TODO: implement
        return null;
    }
}
