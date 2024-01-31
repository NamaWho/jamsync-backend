package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.service.AuthService;
import com.lsmsdb.jamsync.service.factory.AuthServiceFactory;
import lombok.Getter;
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
                return new Response(false, "Login successful", jwt);
            } else {
                return new Response(true, "Login failed", null);
            }
        } catch (Exception e) {
            return new Response(true, e.getMessage(), null);
        }
    }

    @GetMapping("/check/{username}")
    public Response checkUsername(@PathVariable String username, @RequestParam String type){
        if (type == null || type.isEmpty() || (!type.equals("musician") && !type.equals("band"))) {
            return new Response(true, "Invalid type field", null);
        }

        try {
            boolean exists = authService.checkUsername(username, type);
            return new Response(false, null, exists);
        } catch (Exception e) {
            return new Response(true, e.getMessage(), null);
        }
    }

    @PostMapping("/{id}/ban")
    public Response banUser(@PathVariable String id, @RequestParam String type) {
        if (type == null || type.isEmpty() || (!type.equals("musician") && !type.equals("band"))) {
            return new Response(true, "Invalid type field", null);
        }

        try {
            authService.banUser(id, type);
            return new Response(false, "User banned", null);
        } catch (Exception e) {
            return new Response(true, e.getMessage(), null);
        }
    }
}
