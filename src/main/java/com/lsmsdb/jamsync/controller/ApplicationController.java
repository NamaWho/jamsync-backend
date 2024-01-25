package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Application;
import com.lsmsdb.jamsync.model.Opportunity;
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

    @GetMapping("/{applicationId}")
    public Response getApplicationById(
            @PathVariable String applicationId
    ) {
        try {
            Opportunity opportunity = applicationService.getApplicationById(applicationId);
            return new Response(false, null, opportunity);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @PostMapping("/")
    public Response createApplication(
            @RequestBody Application application,
            @RequestParam String opportunityId
    ) {
        try {
            Application createdApplication = applicationService.createApplication(opportunityId, application);
            return new Response(false, "Application created successfully", createdApplication);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }

    @DeleteMapping("/{applicationId}")
    public Response deleteApplication(
            @PathVariable String applicationId,
            @RequestParam String opportunityId
    ) {
        try {
            applicationService.deleteApplication(opportunityId, applicationId);
            return new Response(false, "Application deleted successfully", null);
        } catch (BusinessException ex) {
            return new Response(true, ex.getMessage(), null);
        }
    }
}
