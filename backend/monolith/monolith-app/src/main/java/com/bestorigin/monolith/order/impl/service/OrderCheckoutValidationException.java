package com.bestorigin.monolith.order.impl.service;

public class OrderCheckoutValidationException extends RuntimeException {

    private final int statusCode;

    public OrderCheckoutValidationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
