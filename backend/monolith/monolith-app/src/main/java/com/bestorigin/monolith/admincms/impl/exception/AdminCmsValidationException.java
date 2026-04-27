package com.bestorigin.monolith.admincms.impl.exception;

public class AdminCmsValidationException extends RuntimeException {

    public AdminCmsValidationException(String messageCode) {
        super(messageCode);
    }
}
