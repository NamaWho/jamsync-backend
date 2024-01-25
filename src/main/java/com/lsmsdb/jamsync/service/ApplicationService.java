package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.model.Application;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.service.exception.BusinessException;

public interface ApplicationService {

    Opportunity getApplicationById(String applicationId) throws BusinessException;

    Application createApplication(String opportunityId, Application application) throws BusinessException;

    void deleteApplication(String opportunityId, String applicationId) throws BusinessException;
}

