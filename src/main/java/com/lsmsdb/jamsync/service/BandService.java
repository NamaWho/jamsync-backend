package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.service.exception.BusinessException;

public interface BandService {
    Band getBandById(String id) throws BusinessException;
}
