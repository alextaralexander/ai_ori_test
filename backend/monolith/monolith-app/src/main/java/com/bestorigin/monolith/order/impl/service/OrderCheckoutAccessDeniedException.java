package com.bestorigin.monolith.order.impl.service;

public class OrderCheckoutAccessDeniedException extends RuntimeException {

    public OrderCheckoutAccessDeniedException(String message) {
        super(message);
    }
}
