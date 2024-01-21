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
        if (d.get("lat") instanceof String) {
            this.latitude = Double.parseDouble(d.getString("lat"));
            this.longitude = Double.parseDouble(d.getString("long"));
        }
        else {
            this.latitude = d.getDouble("lat");
            this.longitude = d.getDouble("long");
        }
    }

    public Document toDocument(){
        Document d = new Document();
        d.append("city", this.city);
        d.append("state", this.state);
        d.append("country", this.country);
        d.append("lat", this.latitude);
        d.append("long", this.longitude);
        return d;
    }
}
