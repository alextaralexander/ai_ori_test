package com.bestorigin.monolith.order.impl.service;

public class OrderCheckoutNotFoundException extends RuntimeException {

    public OrderCheckoutNotFoundException(String message) {
        super(message);
    }
}
