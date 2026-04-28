package com.bestorigin.monolith.adminfulfillment.impl.exception;

public class AdminFulfillmentConflictException extends RuntimeException {
    public AdminFulfillmentConflictException(String messageCode) {
        super(messageCode);
    }
}
