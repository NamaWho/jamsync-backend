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
    private String opportunityId;

    public Application(Document d) {
        this._id = d.getString("_id");
        this.createdAt = LocalDate.parse(d.getString("createdAt"));
        this.applicant = (Document) d.get("applicant");
        this.text = d.containsKey("text") ? d.getString("text") : "";
        this.status = d.containsKey("status") ? d.getInteger("status") : 0;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("_id", this._id);
        document.put("createdAt", this.createdAt.toString());
        document.put("applicant", this.applicant);
        if (this.text != null && !this.text.isEmpty())
            document.put("text", this.text);
        if (this.status != null && this.status > 0)
            document.put("status", this.status);
        return document;
    }
}
