package com.bestorigin.monolith.adminbonus.impl.exception;

import java.util.List;

public class AdminBonusValidationException extends RuntimeException {
    private final List<String> details;

    public AdminBonusValidationException(String messageCode, List<String> details) {
        super(messageCode);
        this.details = List.copyOf(details);
    }

    public List<String> details() {
        return details;
    }
}
