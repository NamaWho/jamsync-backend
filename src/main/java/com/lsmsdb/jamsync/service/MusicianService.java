package com.lsmsdb.jamsync.service;

import com.lsmsdb.jamsync.service.exception.BusinessException;
import com.lsmsdb.jamsync.model.Musician;

public interface MusicianService {

    Musician getMusicianById(String id) throws BusinessException;

    void createMusician(Musician musician) throws BusinessException;

    void deleteMusicianById(String id) throws BusinessException;

    Integer getFollowingCount(String _id) throws BusinessException;
}
