package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.service.AuthService;
import com.lsmsdb.jamsync.service.factory.AuthServiceFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;

    public AuthController(){
        this.authService = AuthServiceFactory.create().getService();
    }

    @PostMapping("/login")
    public Response login(@RequestParam String type, @RequestParam String user, @RequestParam String password) {
        try {
            String jwt = authService.login(type, user, password);
            if (jwt != null) {
                return new Response(true, "Login successful", jwt);
            } else {
                return new Response(false, "Login failed", null);
            }
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }
}
