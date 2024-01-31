package com.lsmsdb.jamsync.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    @JsonProperty("isBanned")
    protected boolean isBanned;
    protected LocalDate creationDateTime;
    protected LocalDate lastUpdateDateTime;
    protected LocalDate lastLoginDateTime;
    protected List<Document> opportunities;
    protected List<Document> applications;
    
    public RegisteredUser(Document d) {
        this._id = d.getString("_id");
        this.username = d.getString("username");
        this.contactEmail = d.getString("contactEmail");
        this.profilePictureUrl = d.getString("profilePictureUrl");
        this.location = new Location((Document) d.get("location"));
        this.about = d.getString("about");
        this.genres = (List<String>) d.get("genres");
        this.isBanned = d.getBoolean("isBanned");
        this.opportunities = (List<Document>) d.get("opportunities");
        this.applications = (List<Document>) d.get("applications");

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

    protected Document toDocument() {
        Document d = new Document();
        d.append("_id", this._id);
        d.append("username", this.username);
        d.append("contactEmail", this.contactEmail);
        d.append("about", this.about);
        d.append("profilePictureUrl", this.profilePictureUrl);
        d.append("genres", this.genres);
        d.append("credentials", this.credentials.toDocument());
        d.append("location", this.location.toDocument());
        d.append("isBanned", this.isBanned);
        d.append("creationDateTime", this.creationDateTime.toString());
        d.append("lastUpdateDateTime", this.lastUpdateDateTime.toString());
        d.append("lastLoginDateTime", this.lastLoginDateTime.toString());
        d.append("opportunities", this.opportunities);
        d.append("applications", this.applications);
        return d;
    }
}
