package com.bestorigin.monolith.bonuswallet.impl.service;

public class BonusWalletValidationException extends RuntimeException {

    private final int statusCode;

    public BonusWalletValidationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
