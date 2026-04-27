package com.bestorigin.monolith.adminpim.impl.exception;

public class AdminPimAccessDeniedException extends RuntimeException {

    public AdminPimAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
