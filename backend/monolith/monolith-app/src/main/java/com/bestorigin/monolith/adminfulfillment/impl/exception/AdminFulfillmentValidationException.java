package com.bestorigin.monolith.adminfulfillment.impl.exception;

import java.util.List;

public class AdminFulfillmentValidationException extends RuntimeException {
    private final List<String> details;

    public AdminFulfillmentValidationException(String messageCode, List<String> details) {
        super(messageCode);
        this.details = List.copyOf(details);
    }

    public List<String> details() {
        return details;
    }
}
