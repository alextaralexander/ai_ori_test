package com.bestorigin.monolith.cart.impl.service;

public class CartValidationException extends RuntimeException {

    public CartValidationException(String messageCode) {
        super(messageCode);
    }
}
