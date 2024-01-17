package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.service.OpportunityService;
import com.lsmsdb.jamsync.service.factory.OpportunityServiceFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private OpportunityService opportunityService;

    public OpportunityController() {
        this.opportunityService = OpportunityServiceFactory.create().getService();
    }
}
