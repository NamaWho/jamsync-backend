package com.lsmsdb.jamsync.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class Application {
    private String _id;
    private LocalDate createdAt;
    private Document applicant; // Represented as a Document
    private String text;
    private Integer status;

    public Application(Document d) {
        this._id = d.getString("_id");
        this.createdAt = LocalDate.parse(d.getString("createdAt"));
        this.applicant = (Document) d.get("applicant"); // Directly assigned as Document
        this.text = d.getString("text");
        this.status = d.getInteger("status");
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("_id", this._id);
        document.put("createdAt", this.createdAt.toString());
        document.put("applicant", this.applicant); // Directly assigned as Document
        document.put("text", this.text);
        document.put("status", this.status);
        return document;
    }
}
