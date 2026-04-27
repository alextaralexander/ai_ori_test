package com.bestorigin.monolith.profile.impl.service;

public class ProfileAccessDeniedException extends RuntimeException {

    public ProfileAccessDeniedException(String message) {
        super(message);
    }
}
