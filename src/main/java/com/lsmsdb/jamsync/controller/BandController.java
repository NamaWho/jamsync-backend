package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.service.BandService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.factory.BandServiceFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bands")
public class BandController {

    private BandService bandService;

    public BandController(){
        this.bandService = BandServiceFactory.create().getService();
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
}
