package com.bestorigin.monolith.partneroffice.impl.exception;

public class PartnerOfficeValidationException extends RuntimeException {

    private final int statusCode;

    public PartnerOfficeValidationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
