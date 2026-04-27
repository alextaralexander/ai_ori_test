package com.bestorigin.monolith.admincms.impl.exception;

public class AdminCmsConflictException extends RuntimeException {

    public AdminCmsConflictException(String messageCode) {
        super(messageCode);
    }
}
