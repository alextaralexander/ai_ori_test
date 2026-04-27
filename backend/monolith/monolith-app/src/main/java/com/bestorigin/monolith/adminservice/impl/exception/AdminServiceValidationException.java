package com.bestorigin.monolith.adminservice.impl.exception;

import java.util.List;

public class AdminServiceValidationException extends RuntimeException {

    private final List<String> details;

    public AdminServiceValidationException(String messageCode, List<String> details) {
        super(messageCode);
        this.details = List.copyOf(details);
    }

    public List<String> details() {
        return details;
    }
}
