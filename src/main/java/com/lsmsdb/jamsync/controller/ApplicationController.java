package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.service.ApplicationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lsmsdb.jamsync.service.factory.ApplicationServiceFactory;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private ApplicationService applicationService;

    public ApplicationController() {
        this.applicationService = ApplicationServiceFactory.create().getService();
    }
}
