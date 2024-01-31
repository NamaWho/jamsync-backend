package com.lsmsdb.jamsync.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Band extends RegisteredUser{
    private double yearsTogether;
    // membersNumber
    // membersAvgAge
    private Integer gigsPlayed;
    //followersCount

    public Band(Document d){
        super(d);
        this.yearsTogether = d.getDouble("yearsTogether");
        if (d.get("gigsPlayed") instanceof String) {
            this.gigsPlayed = Integer.parseInt(d.getString("gigsPlayed"));
        }
        else {
            this.gigsPlayed = d.getInteger("gigsPlayed");
        }
    }

    public Document toDocument() {
        Document d = super.toDocument();
        d.append("yearsTogether", this.yearsTogether);
        d.append("gigsPlayed", this.gigsPlayed);
        return d;
    }
}
