package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.model.Musician;
import com.lsmsdb.jamsync.service.factory.BandServiceFactory;

public interface MusicianService {


    void createMusician(Musician musician) throws BusinessException;

    Musician getMusicianById(String id) throws BusinessException;

    Musician updateMusicianById(String id, Musician musician) throws BusinessException;

    void deleteMusicianById(String id) throws BusinessException;

    Integer getFollowingCount(String _id) throws BusinessException;

    void follow(String id, String followedId, String type) throws BusinessException;

    void unfollow(String id, String followedId, String type) throws BusinessException;

}
