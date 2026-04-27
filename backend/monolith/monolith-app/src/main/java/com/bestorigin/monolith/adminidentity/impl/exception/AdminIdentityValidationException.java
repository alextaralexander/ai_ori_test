package com.bestorigin.monolith.adminidentity.impl.exception;

import java.util.List;

public class AdminIdentityValidationException extends RuntimeException {

    private final List<String> details;

    public AdminIdentityValidationException(String messageCode, List<String> details) {
        super(messageCode);
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public List<String> details() {
        return details;
    }
}
