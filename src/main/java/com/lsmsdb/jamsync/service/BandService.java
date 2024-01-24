package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.service.exception.BusinessException;

public interface BandService {
    void createBand(Band band) throws BusinessException;
    Band getBandById(String id) throws BusinessException;
    void deleteBandById(String id) throws BusinessException;

    boolean addMember(String bandId, String memberId) throws BusinessException;

}
