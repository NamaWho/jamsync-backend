package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.model.Location;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.model.RegisteredUser;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import org.bson.Document;

import java.util.List;

public interface OpportunityService {
    Opportunity getOpportunityById(String id) throws BusinessException;

    Opportunity createOpportunity(Opportunity opportunity) throws BusinessException;

    void deleteOpportunityById(String id) throws BusinessException;

    List<Opportunity> searchOpportunities(String forUser, String publisherUsername, List<String> genres, List<String> instruments, Location location, Integer maxDistance, Integer minAge, Integer maxAge, String gender, Integer page, Integer pageSize) throws BusinessException;

    List<Document> getTopAppliedOpportunities() throws BusinessException;

    List<Document> getOpportunitiesByAgeRange() throws BusinessException;
}
