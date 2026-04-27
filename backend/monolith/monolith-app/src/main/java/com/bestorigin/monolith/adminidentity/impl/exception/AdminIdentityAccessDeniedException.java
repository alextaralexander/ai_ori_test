package com.bestorigin.monolith.adminidentity.impl.exception;

public class AdminIdentityAccessDeniedException extends RuntimeException {

    public AdminIdentityAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
