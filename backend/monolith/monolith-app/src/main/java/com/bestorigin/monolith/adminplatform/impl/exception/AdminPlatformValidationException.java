package com.bestorigin.monolith.adminplatform.impl.exception;

import java.util.List;

public class AdminPlatformValidationException extends RuntimeException {
    private final List<String> details;

    public AdminPlatformValidationException(String messageCode, List<String> details) {
        super(messageCode);
        this.details = details;
    }

    public List<String> details() {
        return details;
    }
}