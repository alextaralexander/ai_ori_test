package com.bestorigin.monolith.cart.impl.service;

public class CartAccessDeniedException extends RuntimeException {

    public CartAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
