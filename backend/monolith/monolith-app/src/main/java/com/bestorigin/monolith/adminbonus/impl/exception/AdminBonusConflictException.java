package com.bestorigin.monolith.adminbonus.impl.exception;

public class AdminBonusConflictException extends RuntimeException {
    public AdminBonusConflictException(String messageCode) {
        super(messageCode);
    }
}
