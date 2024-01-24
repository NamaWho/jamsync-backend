package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.service.OpportunityService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.factory.OpportunityServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private OpportunityService opportunityService;

    public OpportunityController() {
        this.opportunityService = OpportunityServiceFactory.create().getService();
    }
    @PostMapping("/")
    public Response createOpportunity(@RequestBody Opportunity opportunity) {
        LogManager.getLogger().info("createOpportunity: " + opportunity);
        try {
            Opportunity o = opportunityService.createOpportunity(opportunity);
            return new Response(false,"", o);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @GetMapping("/{id}")
    public Response getOpportunityById(@PathVariable String id) {
        LogManager.getLogger().info("getOpportunityById: " + id);
        try {
            Opportunity o = opportunityService.getOpportunityById(id);
            return new Response(false,"", o);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @DeleteMapping("/{id}")
    public Response deleteOpportunityById(@PathVariable String id) {
        try {
            opportunityService.deleteOpportunityById(id);
            return new Response(false,"", null);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }
}
