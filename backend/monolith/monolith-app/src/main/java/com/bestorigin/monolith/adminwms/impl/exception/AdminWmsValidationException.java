package com.bestorigin.monolith.adminwms.impl.exception;

import java.util.List;

public class AdminWmsValidationException extends RuntimeException {

    private final List<String> details;

    public AdminWmsValidationException(String message, List<String> details) {
        super(message);
        this.details = details;
    }

    public List<String> details() {
        return details;
    }
}
