package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.service.OpportunityService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.factory.OpportunityServiceFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private OpportunityService opportunityService;

    public OpportunityController() {
        this.opportunityService = OpportunityServiceFactory.create().getService();
    }

    @GetMapping("/{id}")
    public Response getOpportunityById(@PathVariable String id) {
        try {
            Opportunity o = opportunityService.getOpportunityById(id);
            return new Response(false,"", o);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }
}
