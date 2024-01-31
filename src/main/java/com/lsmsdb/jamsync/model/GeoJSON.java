package com.lsmsdb.jamsync.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GeoJSON {
    private String type;
    private List<Double> coordinates;

    public GeoJSON(Document d){
        this.type = d.getString("type");
        this.coordinates = (List<Double>) d.get("coordinates");
    }

    public GeoJSON(Map<String, Object> d){
        this.type = (String) d.get("type");
        this.coordinates = (List<Double>) d.get("coordinates");
    }

    public Document toDocument(){
        Document d = new Document();
        d.append("type", this.type);
        d.append("coordinates", this.coordinates);
        return d;
    }
}
