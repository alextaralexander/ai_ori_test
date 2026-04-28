package com.bestorigin.monolith.adminbonus.impl.exception;

public class AdminBonusAccessDeniedException extends RuntimeException {
    public AdminBonusAccessDeniedException(String messageCode) {
        super(messageCode);
    }
}
