package com.bestorigin.monolith.profile.impl.service;

public class ProfileValidationException extends RuntimeException {

    private final int statusCode;

    public ProfileValidationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
