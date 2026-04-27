package com.bestorigin.monolith.adminpim.impl.exception;

import java.util.List;

public class AdminPimValidationException extends RuntimeException {

    private final List<String> details;

    public AdminPimValidationException(String messageCode, List<String> details) {
        super(messageCode);
        this.details = details == null ? List.of() : details;
    }

    public List<String> details() {
        return details;
    }
}
