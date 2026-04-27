package com.bestorigin.monolith.adminreferral.impl.exception;

public class AdminReferralValidationException extends RuntimeException {

    public AdminReferralValidationException(String messageCode) {
        super(messageCode);
    }
}
