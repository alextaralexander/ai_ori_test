package com.bestorigin.monolith.partnerbenefits.impl.service;

public class PartnerBenefitsValidationException extends RuntimeException {
    private final int statusCode;

    public PartnerBenefitsValidationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
