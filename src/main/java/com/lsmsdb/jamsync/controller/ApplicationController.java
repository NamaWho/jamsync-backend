package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Application;
import com.lsmsdb.jamsync.service.ApplicationService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.service.factory.ApplicationServiceFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private ApplicationService applicationService;

    public ApplicationController() {
        this.applicationService = ApplicationServiceFactory.create().getService();
    }

    @GetMapping("/{opportunityId}/{applicationId}")
    public Response getApplicationById(
            @PathVariable String opportunityId,
            @PathVariable String applicationId
    ) {
        try {
            Application application = applicationService.getApplicationById(opportunityId, applicationId);
            return new Response(false, null, application);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @PostMapping("/{opportunityId}")
    public Response createApplication(
            @PathVariable String opportunityId,
            @RequestBody Application application
    ) {
        try {
            Application createdApplication = applicationService.createApplication(opportunityId, application);
            return new Response(false, "Application created successfully", createdApplication);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @DeleteMapping("/{opportunityId}/{applicationId}")
    public Response deleteApplication(
            @PathVariable String opportunityId,
            @PathVariable String applicationId
    ) {
        try {
            applicationService.deleteApplication(opportunityId, applicationId);
            return new Response(false, "Application deleted successfully", null);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }
}
