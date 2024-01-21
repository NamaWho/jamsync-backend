package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.service.exception.BusinessException;

public interface OpportunityService {
    Opportunity getOpportunityById(String id) throws BusinessException;

    Opportunity createOpportunity(Opportunity opportunity) throws BusinessException;
}
