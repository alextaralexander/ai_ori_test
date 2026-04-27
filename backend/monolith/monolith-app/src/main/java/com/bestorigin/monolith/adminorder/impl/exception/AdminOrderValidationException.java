package com.bestorigin.monolith.adminorder.impl.exception;

import java.util.List;

public class AdminOrderValidationException extends RuntimeException {

    private final List<String> details;

    public AdminOrderValidationException(String messageCode, List<String> details) {
        super(messageCode);
        this.details = details;
    }

    public List<String> details() {
        return details;
    }
}
