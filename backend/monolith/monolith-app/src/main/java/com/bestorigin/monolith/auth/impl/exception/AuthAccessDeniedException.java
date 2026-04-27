package com.bestorigin.monolith.auth.impl.exception;

public class AuthAccessDeniedException extends RuntimeException {

    public AuthAccessDeniedException(String message) {
        super(message);
    }
}
