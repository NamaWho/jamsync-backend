package com.lsmsdb.jamsync.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

@Getter
@Setter
@AllArgsConstructor
public class Location {
    private String city;
    private String state;
    private String country;
    private double latitude;
    private double longitude;

    public Location(Document d){
        this.city = d.getString("city");
        this.state = d.getString("state");
        this.country = d.getString("country");
        this.latitude = d.getDouble("lat");
        this.longitude = d.getDouble("long");
    }
}
