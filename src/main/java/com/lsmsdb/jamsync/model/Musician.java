package com.lsmsdb.jamsync.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Musician extends RegisteredUser {
    private String firstName;
    private String lastName;
    private char gender;
    private Integer age;
    private List<String> instruments;

    public Musician(Document d){
        super(d);
        this.firstName = d.containsKey("firstName") ? d.getString("firstName") : "";
        this.lastName = d.containsKey("lastName") ? d.getString("lastName") : "";
        this.gender = d.getString("gender").charAt(0);
        this.age = d.getInteger("age");
        this.instruments = d.containsKey("instruments") ? (List<String>) d.get("instruments") : new ArrayList<>();
    }

    public Document toDocument() {
        Document d = super.toDocument();
        if (this.firstName != null && !this.firstName.isEmpty())
            d.append("firstName", this.firstName);
        if (this.lastName != null && !this.lastName.isEmpty())
            d.append("lastName", this.lastName);
        d.append("gender", this.gender);
        d.append("age", this.age);
        if (this.instruments != null && !this.instruments.isEmpty())
            d.append("instruments", this.instruments);
        return d;
    }
}
