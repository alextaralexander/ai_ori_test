package com.bestorigin.monolith.adminidentity.impl.exception;

public class AdminIdentityConflictException extends RuntimeException {

    public AdminIdentityConflictException(String messageCode) {
        super(messageCode);
    }
}
