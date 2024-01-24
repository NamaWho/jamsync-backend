package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.BandDAO;
import com.lsmsdb.jamsync.dao.exception.DAOException;
import com.lsmsdb.jamsync.model.Band;
import com.lsmsdb.jamsync.service.BandService;
import com.lsmsdb.jamsync.service.exception.BusinessException;

public class BandServiceImpl implements BandService {
    private final static BandDAO bandDAO = new BandDAO();

    @Override
    public void createBand(Band band) throws BusinessException {
        try {
            bandDAO.createBand(band);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public Band getBandById(String id) throws BusinessException {
        try {
            return bandDAO.getBandById(id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public Band updateBandById(String id, Band band) throws BusinessException {
        try {
            return bandDAO.updateBandById(id, band);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public void deleteBandById(String id) throws BusinessException {
        try {
            bandDAO.deleteBandById(id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public boolean addMember(String bandId, String memberId) throws BusinessException {
        //TODO: Implement this method
        return false;
    }
}
