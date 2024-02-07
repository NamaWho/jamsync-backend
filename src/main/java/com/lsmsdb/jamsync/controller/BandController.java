package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.service.BandService;
import com.lsmsdb.jamsync.service.RegisteredUserService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.factory.BandServiceFactory;
import com.lsmsdb.jamsync.service.factory.MusicianServiceFactory;
import com.lsmsdb.jamsync.service.factory.RegisteredUserServiceFactory;
import com.lsmsdb.jamsync.service.MusicianService;
import org.apache.logging.log4j.LogManager;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/bands")
public class BandController {

    private BandService bandService;
    private MusicianService musicianService;
    private RegisteredUserService registeredUserService;

    public BandController() {
        this.bandService = BandServiceFactory.create().getService();
        this.registeredUserService = RegisteredUserServiceFactory.create().getService();
        this.musicianService = MusicianServiceFactory.create().getService();
    }

    @PostMapping("/")
    public Response createBand(@RequestBody Band band) {
        if (band.getCredentials() == null) {
            return new Response(true, "Credentials are required", null);
        }

        try {
            bandService.createBand(band);
            return new Response(false, "", null);
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

    @PutMapping("/{id}")
    public Response updateBandById(@PathVariable String id, @RequestBody Band band) {
        LogManager.getLogger(BandController.class).info("Updating band with id " + id);
        try {
            Band b = bandService.updateBandById(id, band);
            return new Response(false, "", b);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @DeleteMapping("/{id}")
    public Response deleteBandById(@PathVariable String id) {
        LogManager.getLogger(BandController.class).info("Deleting band with id: " + id);
        try {
            bandService.deleteBandById(id);
            return new Response(false, "", null);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @GetMapping("/{id}/followers")
    public Response getFollowersCount(@PathVariable String id) {
        try {
            Integer followersCount = registeredUserService.getFollowersCount(id, "Band");
            return new Response(false, "", followersCount);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @PostMapping("/{id}/member")
    public Response addMember(@PathVariable String id, @RequestParam String memberId) {
        try {
            if (id == null || memberId == null || id.isEmpty() || memberId.isEmpty()) {
                return new Response(true, "Band ID and Musician ID are required", null);
            }

            Band band = bandService.getBandById(id);
            if (band == null) {
                return new Response(true, "Band not found", null);
            }

            Musician musician = musicianService.getMusicianById(memberId);
            if (musician == null) {
                return new Response(true, "Musician not found", null);
            }

            boolean result = bandService.addMember(band.get_id(), musician.get_id());

            return new Response(false, "", result);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }
    @DeleteMapping("/{id}/member")
    public Response removeMember(@PathVariable String id, @RequestParam String memberId) {
        try {
            if (id == null || memberId == null || id.isEmpty() || memberId.isEmpty()) {
                return new Response(true, "Band ID and Musician ID are required", null);
            }

            Band band = bandService.getBandById(id);
            if (band == null) {
                return new Response(true, "Band not found", null);
            }

            Musician musician = musicianService.getMusicianById(memberId);
            if (musician == null) {
                return new Response(true, "Musician not found", null);
            }

            boolean result = bandService.removeMember(band.get_id(), musician.get_id());

            return new Response(false, "", result);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @GetMapping("/{id}/members")
    public Response getMembers(@PathVariable String id) {
        try {
            if (id == null || id.isEmpty()) {
                return new Response(true, "Band ID is required", null);
            }

            return new Response(false, "", bandService.getMembers(id));
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @PostMapping("/suggestedOpportunities")
    public Response getSuggestedOpportunities(@RequestBody Band b) {
        try {
            return new Response(false,"", bandService.getSuggestedOpportunities(b));
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }
}