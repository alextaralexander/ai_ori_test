package com.bestorigin.monolith.adminplatform.impl.exception;

public class AdminPlatformAccessDeniedException extends RuntimeException {
    public AdminPlatformAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}