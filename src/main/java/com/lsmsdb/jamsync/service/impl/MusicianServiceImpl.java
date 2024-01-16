package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.MusicianDAO;
import com.lsmsdb.jamsync.service.MusicianService;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.dao.exception.DAOException;

public class MusicianServiceImpl implements MusicianService {
    private final static MusicianDAO musicianDAO = new MusicianDAO();

    @Override
    public Musician getMusicianById(String id) throws BusinessException {
        try {
            return musicianDAO.getMusicianById(id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public void createMusician(Musician musician) throws BusinessException {}

    @Override
    public void deleteMusicianById(String id) throws BusinessException {}
}
