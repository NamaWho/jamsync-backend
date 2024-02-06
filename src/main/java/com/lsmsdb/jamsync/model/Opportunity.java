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
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Opportunity {
    /**
     * _id, --> INDEX
     *     location: [ --> INDEX
     *         city,
     *         country
     *         state,
     *         lat,
     *         long
     *     ],
     *     title,
     *     description,
     *     role,
     *     instruments: [],
     *     genres: [],
     *     minimumAge,
     *     maximumAge,
     *     gender,
     *     createdAt,
     *     modifiedAt,
     *     expiresAt,
     *     visits,
     *     publisher: {
     *         _id
     *         type,
     *         username,
     *         profilePictureUrl
     *     },
     *     applications: [
     *         {
     *             _id, -- INDEX ON THIS (this is the index of the APPLICATION itself)
     *             createdAt,
     *             applicant: {
     *                 _id
     *                 username,
     *                 profilePictureUrl,
     *                 contactEmail
     *             },
     *             text,
     *             status
     *         }
     *     ]
     */
    private String _id;
    private Location location;
    private String title;
    private String description;
    private String role;
    private List<String> instruments;
    private List<String> genres;
    private int minimumAge;
    private int maximumAge;
    private char gender;
    private LocalDate createdAt;
    private LocalDate modifiedAt;
    private LocalDate expiresAt;
    private Integer visits;
    private Document publisher;
    private List<Application> applications;

    public Opportunity(Document d){
        this._id = d.getString("_id");
        this.location = new Location((Document) d.get("location"));
        this.title = d.getString("title");
        this.description = d.getString("description");
        this.role = d.getString("role");
        LogManager.getLogger().info("parsing opportunity");
        this.instruments = (d.get("instruments") == null || d.get("instruments").toString().isEmpty()) ? null : (List<String>) d.get("instruments");
        LogManager.getLogger().info("parsing opportunity2");
        this.genres = (List<String>) d.get("genres");
        LogManager.getLogger().info("parsing opportunity3");
        // parseInteger minimum and maximum age
        this.minimumAge = d.getInteger("minimumAge") == null ? 0 : d.getInteger("minimumAge");
        this.maximumAge = d.getInteger("maximumAge") == null ? 0 : d.getInteger("maximumAge");
        //Object genderObj = d.get("gender");
        //this.gender = genderObj == null ? '-' : ((Character) genderObj);
        this.gender = (d.getString("gender") == null || d.getString("gender").isEmpty()) ? '-' : d.getString("gender").charAt(0);

        LogManager.getLogger().info("parsing opportunity");

        String creationDateTimeString = d.getString("createdAt");
        if(creationDateTimeString.length() > 10) {
            this.createdAt = LocalDateTime.parse(creationDateTimeString).toLocalDate();
        } else {
            this.createdAt = LocalDate.parse(creationDateTimeString);
        }

        String lastUpdateDateTimeString = d.getString("modifiedAt");
        if(lastUpdateDateTimeString.length() > 10) {
            this.modifiedAt = LocalDateTime.parse(lastUpdateDateTimeString).toLocalDate();
        } else {
            this.modifiedAt = LocalDate.parse(lastUpdateDateTimeString);
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
        this.visits = d.getInteger("visits");
        this.publisher = (Document) d.get("publisher");
        this.applications = (List<Application>) d.get("applications");
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("_id", this._id);
        document.put("location", this.location.toDocument());
        document.put("title", this.title);
        document.put("description", this.description);
        document.put("role", this.role);
        document.put("instruments", this.instruments);
        document.put("genres", this.genres);
        document.put("minimumAge", this.minimumAge);
        document.put("maximumAge", this.maximumAge);
        document.put("gender", this.gender);
        document.put("createdAt", this.createdAt.toString());
        document.put("modifiedAt", this.modifiedAt.toString());
        document.put("expiresAt", this.expiresAt.toString());
        document.put("visits", this.visits);
        document.put("publisher", this.publisher);
        document.put("applications", this.applications);
        return document;
    }
}
