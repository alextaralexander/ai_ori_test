package com.bestorigin.monolith.partnerreporting.impl.service;

public class PartnerReportValidationException extends RuntimeException {

    private final int statusCode;

    public PartnerReportValidationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
