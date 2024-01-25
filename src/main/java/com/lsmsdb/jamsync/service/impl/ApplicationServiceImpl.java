package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.ApplicationDAO;
import com.lsmsdb.jamsync.model.Application;
import com.lsmsdb.jamsync.service.ApplicationService;
import com.lsmsdb.jamsync.service.exception.BusinessException;

public class ApplicationServiceImpl implements ApplicationService {

    private final static ApplicationDAO applicationDAO = new ApplicationDAO();

    @Override
    public Application getApplicationById(String opportunityId, String applicationId) throws BusinessException {
        try {
            return applicationDAO.getApplicationById(opportunityId, applicationId);
        } catch (Exception ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public Application createApplication(String opportunityId, Application application) throws BusinessException {
        try {
            return applicationDAO.createApplication(opportunityId, application);
        } catch (Exception ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public void deleteApplication(String opportunityId, String applicationId) throws BusinessException {
        try {
            applicationDAO.deleteApplication(opportunityId, applicationId);
        } catch (Exception ex) {
            throw new BusinessException(ex);
        }
    }
}