package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.dao.MusicianDAO;
import com.lsmsdb.jamsync.model.Opportunity;
import com.lsmsdb.jamsync.service.MusicianService;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.dao.exception.DAOException;
import org.bson.Document;

import java.util.List;

public class MusicianServiceImpl implements MusicianService {
    private final static MusicianDAO musicianDAO = new MusicianDAO();

    @Override
    public void createMusician(Musician musician) throws BusinessException {
        try {
            musicianDAO.createMusician(musician);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public Musician getMusicianById(String id) throws BusinessException {
        try {
            return musicianDAO.getMusicianById(id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public Musician updateMusicianById(String id, Musician musician) throws BusinessException {
        try {
            return musicianDAO.updateMusicianById(id, musician);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public void deleteMusicianById(String id) throws BusinessException {
        try {
            musicianDAO.deleteMusicianById(id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public Integer getFollowingCount(String _id) throws BusinessException {
        try {
            return musicianDAO.getFollowingCount(_id);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public boolean checkFollow(String id, String userId, String type) throws BusinessException {
        try {
            return musicianDAO.checkFollow(id, userId, type);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public void follow(String id, String followedId, String type) throws BusinessException {
        try {
            musicianDAO.follow(id, followedId, type);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public void unfollow(String id, String followedId, String type) throws BusinessException {
        try {
            musicianDAO.unfollow(id, followedId, type);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    @Override
    public List<Opportunity> getSuggestedOpportunities(Musician m) throws BusinessException {
        try {
            return musicianDAO.getSuggestedOpportunities(m);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }
    @Override
    public List<Document> suggestOpportunitiesByFollowers(Musician m) throws BusinessException {
        try {
            return musicianDAO.suggestOpportunitiesByFollowers(m);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

}
