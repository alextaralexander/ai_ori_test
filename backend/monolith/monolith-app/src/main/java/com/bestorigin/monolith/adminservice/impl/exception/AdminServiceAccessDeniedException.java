package com.bestorigin.monolith.adminservice.impl.exception;

public class AdminServiceAccessDeniedException extends RuntimeException {

    public AdminServiceAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
