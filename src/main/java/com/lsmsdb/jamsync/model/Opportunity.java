package com.lsmsdb.jamsync.model;

import com.lsmsdb.jamsync.service.RegisteredUserService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Opportunity {
    private String _id;
    private Location location;
    private String title;
    private String description;
    private String role;
    private List<String> instruments;
    private List<String> genres;
    private int minimumAge;
    private int maximumAge;
    private String gender;
    private LocalDate createdAt;
    private LocalDate expiresAt;
    private Integer visits;
    private Document publisher;
    private List<Application> applications;

    public Opportunity(Document d){
        this._id = d.getString("_id");
        this.location = new Location((Document) d.get("location"));
        this.title = d.getString("title");
        this.description = d.containsKey("description") ? d.getString("description") : "";
        this.role = d.containsKey("role") ? d.getString("role") : "";
        this.instruments = (d.containsKey("instruments") && d.get("instruments") != null && !d.get("instruments").toString().isEmpty()) ? (List<String>) d.get("instruments") : new ArrayList<>();
        this.genres = d.containsKey("genres") ? (List<String>) d.get("genres") : new ArrayList<>();
        this.minimumAge = d.containsKey("minimumAge") ? d.getInteger("minimumAge") : 0;
        this.maximumAge = d.containsKey("maximumAge") ? d.getInteger("maximumAge") : 0;
        this.gender = d.containsKey("gender") ? d.getString("gender") : "-";

        String creationDateTimeString = d.getString("createdAt");
        if(creationDateTimeString.length() > 10) {
            this.createdAt = LocalDateTime.parse(creationDateTimeString).toLocalDate();
        } else {
            this.createdAt = LocalDate.parse(creationDateTimeString);
        }

        String expiresAtString = d.getString("expiresAt");
        if (expiresAtString == null) {
            this.expiresAt = null;
        } else {
            if (expiresAtString.length() > 10) {
                this.expiresAt = LocalDateTime.parse(expiresAtString).toLocalDate();
            } else {
                this.expiresAt = LocalDate.parse(expiresAtString);
            }
        }
        this.visits = d.containsKey("visits") ? d.getInteger("visits") : 0;
        this.publisher = (Document) d.get("publisher");
        this.applications = d.containsKey("applications") ? (List<Application>) d.get("applications") : new ArrayList<>();
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("_id", this._id);
        document.put("location", this.location.toDocument());
        document.put("title", this.title);
        if (this.description != null && !this.description.isEmpty())
            document.put("description", this.description);
        if (this.role != null && !this.role.isEmpty())
            document.put("role", this.role);
        if (this.instruments != null && !this.instruments.isEmpty())
            document.put("instruments", this.instruments);
        if (this.genres != null && !this.genres.isEmpty())
            document.put("genres", this.genres);
        if (this.minimumAge != 0)
            document.put("minimumAge", this.minimumAge);
        if (this.maximumAge != 0)
            document.put("maximumAge", this.maximumAge);
        document.put("gender", this.gender);
        document.put("createdAt", this.createdAt.toString());
        document.put("expiresAt", this.expiresAt.toString());
        if (this.visits != null && this.visits != 0)
            document.put("visits", this.visits);
        document.put("publisher", this.publisher);
        if (this.applications != null && !this.applications.isEmpty())
            document.put("applications", this.applications);
        return document;
    }
}
