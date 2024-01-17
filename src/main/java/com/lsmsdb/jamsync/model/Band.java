package com.lsmsdb.jamsync.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Band {
    private String _id;
    private String name;
    private String about;
    private List<String> genres;
    private Location location;
    private Credentials credentials;
    private String contactEmail;
    private LocalDate creationDateTime;
    private LocalDate lastUpdateDateTime;
    private LocalDate lastLoginDateTime;
    private String profilePictureUrl;
    private double yearsTogether;
    // membersNumber
    // membersAvgAge
    private Integer gigsPlayed;
    private boolean isBanned;
    //followersCount
    private List<Opportunity> opportunities;
    private List<Application> applications;

    public Band(Document d){
        this._id = d.getString("_id");
        this.name = d.getString("name");
        this.about = d.getString("about");
        this.genres = (List<String>) d.get("genres");
        this.location = new Location((Document) d.get("location"));
        this.contactEmail = d.getString("contactEmail");
        this.creationDateTime = LocalDate.parse(d.getString("creationDateTime"));
        this.lastUpdateDateTime = LocalDate.parse(d.getString("lastUpdateDateTime"));
        this.lastLoginDateTime = LocalDate.parse(d.getString("lastLoginDateTime"));
        this.profilePictureUrl = d.getString("profilePictureUrl");
        this.yearsTogether = d.getDouble("yearsTogether");
        this.gigsPlayed = d.getInteger("gigsPlayed");
        this.isBanned = d.getBoolean("isBanned");
        this.opportunities = (List<Opportunity>) d.get("opportunities");
        this.applications = (List<Application>) d.get("applications");
    }
}
