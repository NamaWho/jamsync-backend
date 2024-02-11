package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.OpportunityDAO;
import com.lsmsdb.jamsync.model.Location;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.routine.MongoTask;
import com.lsmsdb.jamsync.routine.MongoUpdater;
import com.lsmsdb.jamsync.service.OpportunityService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.dao.exception.DAOException;
import org.bson.Document;

import java.util.List;

public class OpportunityServiceImpl implements OpportunityService {

    private final static OpportunityDAO opportunityDAO = new OpportunityDAO();

    @Override
    public Opportunity createOpportunity(Opportunity opportunity) throws BusinessException {
        try {
            return opportunityDAO.createOpportunity(opportunity);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public Opportunity getOpportunityById(String id) throws BusinessException {
        try {
            return opportunityDAO.getOpportunityById(id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public void deleteOpportunityById(String id) throws BusinessException {
        try {
            opportunityDAO.deleteOpportunityById(id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public List<Opportunity> searchOpportunities(String forUser, String publisherUsername, List<String> genres, List<String> instruments, Location location, Integer maxDistance, Integer minAge, Integer maxAge, String gender, Integer page, Integer pageSize) throws BusinessException {
        try {
            return opportunityDAO.searchOpportunities(forUser, publisherUsername, genres, instruments, location, maxDistance, minAge, maxAge, gender, page, pageSize);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public List<Document> getTopAppliedOpportunities() throws BusinessException {
        try {
            return opportunityDAO.getTopAppliedOpportunities();
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public List<Document> getOpportunitiesByAgeRange() throws BusinessException {
        try {
            return opportunityDAO.getOpportunitiesByAgeRange();
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public List<Document> getTopGenres() throws BusinessException {
        try {
            return opportunityDAO.getTopGenres();
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public List<Document> getTopLocationsForOpportunities() throws BusinessException{
        try{
            return opportunityDAO.getTopLocationsForOpportunities();
        }catch (DAOException ex){
            throw new BusinessException(ex);
        }
    }

    @Override
    public List<Document> getTopPublishers() throws BusinessException{
        try{
            return opportunityDAO.getTopPublishers();
        }catch (DAOException ex){
            throw new BusinessException(ex);
        }
    }
}
