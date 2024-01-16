package com.lsmsdb.jamsync.service.exception;

public class BusinessException extends Exception {
    public BusinessException(Exception ex){
        super(ex);
    }
    public BusinessException(String message){
        super(message);
    }
}