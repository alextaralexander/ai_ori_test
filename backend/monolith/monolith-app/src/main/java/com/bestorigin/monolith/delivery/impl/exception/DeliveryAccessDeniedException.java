package com.bestorigin.monolith.delivery.impl.exception;

public class DeliveryAccessDeniedException extends RuntimeException {

    public DeliveryAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
