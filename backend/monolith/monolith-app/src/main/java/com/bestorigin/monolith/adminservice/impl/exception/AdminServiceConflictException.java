package com.bestorigin.monolith.adminservice.impl.exception;

public class AdminServiceConflictException extends RuntimeException {

    public AdminServiceConflictException(String messageCode) {
        super(messageCode);
    }
}
