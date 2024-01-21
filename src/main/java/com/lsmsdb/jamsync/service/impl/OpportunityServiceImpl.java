package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.OpportunityDAO;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.service.OpportunityService;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.dao.exception.DAOException;

public class OpportunityServiceImpl implements OpportunityService {

    private final static OpportunityDAO opportunityDAO = new OpportunityDAO();

    @Override
    public Opportunity getOpportunityById(String id) throws BusinessException {
        try {
            return opportunityDAO.getOpportunityById(id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }
}
