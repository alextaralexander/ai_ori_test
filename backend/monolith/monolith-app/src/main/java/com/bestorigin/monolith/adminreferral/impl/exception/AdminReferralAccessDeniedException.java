package com.bestorigin.monolith.adminreferral.impl.exception;

public class AdminReferralAccessDeniedException extends RuntimeException {

    public AdminReferralAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
