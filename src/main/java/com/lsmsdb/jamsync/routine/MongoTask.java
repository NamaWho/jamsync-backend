package com.lsmsdb.jamsync.routine;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

@Getter
@Setter
public class MongoTask {
    private String operation;
    private Document document;

    public MongoTask(String operation, Document document) {
        this.operation = operation;
        this.document = document;
    }
}
