package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Location;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.model.RegisteredUser;
import com.lsmsdb.jamsync.service.OpportunityService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.factory.OpportunityServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/search")
    public Response searchOpportunities(
            @RequestBody Map<String, Object> body) {
        Document doc = new Document(body);
        if (doc.getString("forUser") == null) {
            return new Response(true, "forUser is required", null);
        }

        try {
            String forUser = doc.getString("forUser");
            String publisherUsername = doc.getString("publisherUsername") != null ? doc.getString("publisherUsername") : null;
            List<String> genres = doc.get("genres") != null ? (List<String>) doc.get("genres") : null;
            List<String> instruments = doc.get("instruments") != null ? (List<String>) doc.get("instruments") : null;
            Location location = doc.get("location") != null ? new Location((Map<String, Object>) doc.get("location")) : null;
            Integer maxDistance = doc.getInteger("maxDistance") != null? doc.getInteger("maxDistance") : null;
            Integer minAge = doc.getInteger("minAge") != null ? doc.getInteger("minAge") : null;
            Integer maxAge = doc.getInteger("maxAge") != null ? doc.getInteger("maxAge") : null;
            String gender = doc.getString("gender") != null ? doc.getString("gender") : null;
            Integer page = doc.getInteger("page") != null ? doc.getInteger("page") : null;
            Integer pageSize = doc.getInteger("pageSize") != null ? doc.getInteger("pageSize") : null;
            page = page == null ? 1 : page;
            pageSize = pageSize == null ? 10 : pageSize;

            List<Opportunity> opportunities = this.opportunityService.searchOpportunities(forUser, publisherUsername, genres, instruments, location, maxDistance, minAge, maxAge, gender, page, pageSize);
            return new Response(false, null, opportunities);
        } catch (Exception e) {
            return new Response(true, e.getMessage(), null);
        }
    }
}
