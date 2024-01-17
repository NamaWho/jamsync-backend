package com.lsmsdb.jamsync.controller;

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
    public Band getBandById(@PathVariable String id) {
        try {
            return bandService.getBandById(id);
        } catch (BusinessException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}
