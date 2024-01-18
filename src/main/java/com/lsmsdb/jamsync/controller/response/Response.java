package com.lsmsdb.jamsync.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// This class is a placeholder for the response object that will be returned by the controller.
// The payload object will be the object returned by the controller.
@Getter
@Setter
@AllArgsConstructor
public class Response {
    private boolean error;
    private String message;
    private Object payload;
}