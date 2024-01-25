package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.service.exception.BusinessException;

public interface BandService {
    void createBand(Band band) throws BusinessException;
    Band getBandById(String id) throws BusinessException;
    Band updateBandById(String id, Band band) throws BusinessException;
    void deleteBandById(String id) throws BusinessException;

    boolean addMember(String id, String memberId) throws BusinessException;

}
