package com.increff.pos.controller;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.MessageData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DuplicateKeyException;

@RestControllerAdvice
public class AppRestControllerAdvice {

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageData handle(ApiException e) {
        return new MessageData(e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageData handle(Throwable e) {
        return new MessageData("An internal error occurred");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageData handle(DuplicateKeyException e) {
        return new MessageData("A record with this key already exists");
    }
} 