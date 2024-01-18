package com.lsmsdb.jamsync.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

@Getter
@Setter
@AllArgsConstructor
public class Credentials {
    private String user;
    private String password;

    public Credentials(Document d){
        this.user = d.getString("user");
        this.password = d.getString("password");
    }
}
