package com.lsmsdb.jamsync.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.json.JsonObject;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Musician {
    private String _id;
    private String firstName;
    private String lastName;
    private char gender;
    private Integer age;
    private String username;
    // password
    private String contactEmail;
    private String profilePictureUrl;
    private Location location;
    private String about;
    private List<String> genres;
    private List<String> instruments;
    private boolean isBanned;
    private LocalDate creationDateTime;
    private LocalDate lastUpdateDateTime;
    private LocalDate lastLoginDateTime;
    //followersCount
    //followingCount
    private List<Opportunity> opportunities;
    private List<Application> applications;

    public Musician(Document d){
        this._id = d.getString("_id");
        this.firstName = d.getString("firstName");
        this.lastName = d.getString("lastName");
        this.gender = d.getString("gender").charAt(0);
        this.age = d.getInteger("age");
        this.username = d.getString("username");
        this.contactEmail = d.getString("contactEmail");
        this.profilePictureUrl = d.getString("profilePictureUrl");

        this.location = new Location((Document) d.get("location"));
        this.about = d.getString("about");
        this.genres = (List<String>) d.get("genres");
        this.instruments = (List<String>) d.get("instruments");
        this.isBanned = d.getBoolean("isBanned");
        this.creationDateTime = LocalDate.parse(d.getString("creationDateTime"));
        this.lastUpdateDateTime = LocalDate.parse(d.getString("lastUpdateDateTime"));
        this.lastLoginDateTime = LocalDate.parse(d.getString("lastLoginDateTime"));
        this.opportunities = (List<Opportunity>) d.get("opportunities");
        this.applications = (List<Application>) d.get("applications");
    }
}
