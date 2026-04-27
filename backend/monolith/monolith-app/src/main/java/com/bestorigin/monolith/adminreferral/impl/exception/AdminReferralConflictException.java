package com.bestorigin.monolith.adminreferral.impl.exception;

public class AdminReferralConflictException extends RuntimeException {

    public AdminReferralConflictException(String messageCode) {
        super(messageCode);
    }
}
