package com.lsmsdb.jamsync.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class Location {
    private String city;
    private String state;
    private String country;
    private GeoJSON geojson;

    public Location(Document d){
        this.city = d.getString("city");
        this.state = d.getString("state");
        this.country = d.getString("country");
        this.geojson = new GeoJSON((Document) d.get("geojson"));
    }

    public Location(Map<String, Object> d){
        this.city = (String) d.get("city");
        this.state = (String) d.get("state");
        this.country = (String) d.get("country");
        this.geojson = new GeoJSON((Map<String, Object>) d.get("geojson"));
    }

    public Document toDocument(){
        Document d = new Document();
        d.append("city", this.city);
        d.append("state", this.state);
        d.append("country", this.country);
        d.append("geojson", this.geojson.toDocument());
        return d;
    }
}