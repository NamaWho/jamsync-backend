package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.service.AuthService;
import com.lsmsdb.jamsync.service.factory.AuthServiceFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;

    public AuthController(){
        this.authService = AuthServiceFactory.create().getService();
    }
}
