package com.bestorigin.monolith.adminpim.impl.exception;

public class AdminPimConflictException extends RuntimeException {

    public AdminPimConflictException(String messageCode) {
        super(messageCode);
    }
}
