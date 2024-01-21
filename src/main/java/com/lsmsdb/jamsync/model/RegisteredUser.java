package com.lsmsdb.jamsync.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public abstract class RegisteredUser {
    protected String _id;
    protected String username;
    protected String contactEmail;
    protected String about;
    protected String profilePictureUrl;
    protected List<String> genres;
    protected Credentials credentials;
    protected Location location;
    protected boolean isBanned;
    protected LocalDate creationDateTime;
    protected LocalDate lastUpdateDateTime;
    protected LocalDate lastLoginDateTime;
    protected List<Opportunity> opportunities;
    protected List<Application> applications;
    
    public RegisteredUser(Document d) {
        this._id = d.getString("_id");
        this.username = d.getString("username");
        this.contactEmail = d.getString("contactEmail");
        this.profilePictureUrl = d.getString("profilePictureUrl");
        this.location = new Location((Document) d.get("location"));
        this.about = d.getString("about");
        this.genres = (List<String>) d.get("genres");
        this.isBanned = d.getBoolean("isBanned");
        this.opportunities = (List<Opportunity>) d.get("opportunities");
        this.applications = (List<Application>) d.get("applications");

        String creationDateTimeString = d.getString("creationDateTime");
        if(creationDateTimeString.length() > 10) {
            this.creationDateTime = LocalDateTime.parse(creationDateTimeString).toLocalDate();
        } else {
            this.creationDateTime = LocalDate.parse(creationDateTimeString);
        }

        String lastUpdateDateTimeString = d.getString("lastUpdateDateTime");
        if(lastUpdateDateTimeString.length() > 10) {
            this.lastUpdateDateTime = LocalDateTime.parse(lastUpdateDateTimeString).toLocalDate();
        } else {
            this.lastUpdateDateTime = LocalDate.parse(lastUpdateDateTimeString);
        }

        String lastLoginDateTimeString = d.getString("lastLoginDateTime");
        if(lastLoginDateTimeString.length() > 10) {
            this.lastLoginDateTime = LocalDateTime.parse(lastLoginDateTimeString).toLocalDate();
        } else {
            this.lastLoginDateTime = LocalDate.parse(lastLoginDateTimeString);
        }
    }
}
