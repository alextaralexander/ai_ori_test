package com.bestorigin.monolith.admincms.impl.exception;

public class AdminCmsAccessDeniedException extends RuntimeException {

    public AdminCmsAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
