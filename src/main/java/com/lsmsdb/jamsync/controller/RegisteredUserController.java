package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.controller.response.Response;
import com.lsmsdb.jamsync.model.Location;
import com.lsmsdb.jamsync.model.RegisteredUser;
import com.lsmsdb.jamsync.service.RegisteredUserService;
import com.lsmsdb.jamsync.service.factory.MusicianServiceFactory;
import com.lsmsdb.jamsync.service.factory.RegisteredUserServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/users")
public class RegisteredUserController {
    private RegisteredUserService registeredUserService;

    public RegisteredUserController(){
        this.registeredUserService = RegisteredUserServiceFactory.create().getService();
    }

    /**
     * Search for users based on the given parameters.
     * @param body The request body.
     * @return
     */
    @PostMapping("/search")
    public Response searchUsers(
            @RequestBody Map<String, Object> body) {
        Document doc = new Document(body);
        LogManager.getLogger("RegisteredUserController").info("Searching users with parameters: " + doc.toJson());
        if (doc.getString("type") == null)
            return new Response(true, "Invalid type", null);

        try {
            String type = doc.getString("type");
            String username = doc.getString("username") != null ? doc.getString("username") : null;
            List<String> genres = doc.get("genres") != null ? (List<String>) doc.get("genres") : null;
            List<String> instruments = doc.get("instruments") != null ? (List<String>) doc.get("instruments") : null;
            LogManager.getLogger("RegisteredUserController").info("Searching adasdas users with parameters: " + doc.get("location"));
            Location location = doc.get("location") != null ? new Location((Map<String, Object>) doc.get("location")) : null;
            LogManager.getLogger("RegisteredUserController").info("Searchingssssss users with parameters: " + doc.toJson());
            Integer maxDistance = doc.getInteger("maxDistance") != null? doc.getInteger("maxDistance") : null;
            Integer minAge = doc.getInteger("minAge") != null ? doc.getInteger("minAge") : null;
            Integer maxAge = doc.getInteger("maxAge") != null ? doc.getInteger("maxAge") : null;
            String gender = doc.getString("gender") != null ? doc.getString("gender") : null;
            Integer page = doc.getInteger("page") != null ? doc.getInteger("page") : null;
            Integer pageSize = doc.getInteger("pageSize") != null ? doc.getInteger("pageSize") : null;
            page = page == null ? 1 : page;
            pageSize = pageSize == null ? 10 : pageSize;


            List<RegisteredUser> users = this.registeredUserService.searchUser(type, username, genres, instruments, location, maxDistance, minAge, maxAge, gender, page, pageSize);
            return new Response(false, null, users);
        } catch (Exception e) {
            LogManager.getLogger("RegisteredUserController").error(e);
            return new Response(true, e.getMessage(), null);
        }
    }


}
