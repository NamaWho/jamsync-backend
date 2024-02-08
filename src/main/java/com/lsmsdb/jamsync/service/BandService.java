package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import org.bson.Document;

import java.util.List;

public interface BandService {
    void createBand(Band band) throws BusinessException;
    Band getBandById(String id) throws BusinessException;
    Band updateBandById(String id, Band band) throws BusinessException;
    void deleteBandById(String id) throws BusinessException;

    boolean addMember(String id, String memberId) throws BusinessException;

    boolean removeMember(String id, String memberId) throws BusinessException;
    List<Document> getMembers(String id) throws BusinessException;
    List<Document> getTopUsersByApplications() throws BusinessException;
    List<Opportunity> getSuggestedOpportunities(Band b) throws BusinessException;
}
