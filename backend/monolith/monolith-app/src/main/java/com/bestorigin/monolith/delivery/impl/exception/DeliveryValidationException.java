package com.bestorigin.monolith.delivery.impl.exception;

import java.util.List;

public class DeliveryValidationException extends RuntimeException {

    private final List<String> details;

    public DeliveryValidationException(String messageCode, List<String> details) {
        super(messageCode);
        this.details = List.copyOf(details);
    }

    public List<String> details() {
        return details;
    }
}
