package com.bestorigin.monolith.adminorder.impl.exception;

public class AdminOrderAccessDeniedException extends RuntimeException {

    public AdminOrderAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
