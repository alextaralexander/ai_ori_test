package com.bestorigin.monolith.adminorder.impl.exception;

public class AdminOrderConflictException extends RuntimeException {

    public AdminOrderConflictException(String messageCode) {
        super(messageCode);
    }
}
