package com.lsmsdb.jamsync.model;

import com.lsmsdb.jamsync.service.RegisteredUserService;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
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
        this.instruments = (List<String>) d.get("instruments");
        this.genres = (List<String>) d.get("genres");
        this.minimumAge = d.getInteger("minimumAge") == null ? 0 : d.getInteger("minimumAge");
        this.maximumAge = d.getInteger("maximumAge") == null ? 0 : d.getInteger("maximumAge");
        this.gender = d.getString("gender") == null ? '-' : d.getString("gender").charAt(0);

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
        System.out.println(expiresAtString + " " + expiresAtString.length());
        if(expiresAtString.length() > 10) {
            this.expiresAt = LocalDateTime.parse(expiresAtString).toLocalDate();
        } else {
            this.expiresAt = LocalDate.parse(expiresAtString);
        }
        this.visits = d.getInteger("visits");
        this.publisher = (Document) d.get("publisher");
        this.applications = (List<Application>) d.get("applications");
    }
}
