package com.bestorigin.monolith.adminpricing.impl.exception;

import java.util.List;

public class AdminPricingValidationException extends RuntimeException {

    private final List<String> details;

    public AdminPricingValidationException(String message, List<String> details) {
        super(message);
        this.details = details;
    }

    public List<String> details() {
        return details;
    }
}
