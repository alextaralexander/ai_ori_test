package com.bestorigin.monolith.adminfulfillment.impl.exception;

public class AdminFulfillmentAccessDeniedException extends RuntimeException {
    public AdminFulfillmentAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
