package com.bestorigin.monolith.auth.impl.exception;

public class AuthSessionExpiredException extends RuntimeException {

    public AuthSessionExpiredException(String message) {
        super(message);
    }
}
