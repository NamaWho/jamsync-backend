package com.lsmsdb.jamsync.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Band extends RegisteredUser{
    private Double yearsTogether;
    private Integer gigsPlayed;

    public Band(Document d){
        super(d);
        this.yearsTogether = d.containsKey("yearsTogether") ? d.getDouble("yearsTogether"): null;
        if (d.containsKey("gigsPlayed")){
            if (d.get("gigsPlayed") instanceof String) {
                this.gigsPlayed = Integer.parseInt(d.getString("gigsPlayed"));
            }
            else {
                this.gigsPlayed = d.getInteger("gigsPlayed");
            }
        } else {
            this.gigsPlayed = null;
        }
    }

    public Document toDocument() {
        Document d = super.toDocument();
        if (this.yearsTogether != null)
            d.append("yearsTogether", this.yearsTogether);
        if (this.gigsPlayed != null)
            d.append("gigsPlayed", this.gigsPlayed);
        return d;
    }
}
