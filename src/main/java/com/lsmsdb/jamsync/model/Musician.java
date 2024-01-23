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
public class Musician extends RegisteredUser {
    private String firstName;
    private String lastName;
    private char gender;
    private Integer age;
    private List<String> instruments;

    public Musician(Document d){
        super(d);
        this.firstName = d.getString("firstName");
        this.lastName = d.getString("lastName");
        this.gender = d.getString("gender").charAt(0);
        this.age = d.getInteger("age");
        this.instruments = (List<String>) d.get("instruments");
    }

    public Document toDocument() {
        Document d = super.toDocument();
        d.append("firstName", this.firstName);
        d.append("lastName", this.lastName);
        d.append("gender", this.gender);
        d.append("age", this.age);
        d.append("instruments", this.instruments);
        return d;
    }
}
