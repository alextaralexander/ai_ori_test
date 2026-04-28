package com.bestorigin.monolith.adminbenefitprogram.impl.exception;

import java.util.List;

public class AdminBenefitProgramValidationException extends RuntimeException {
    private final List<String> details;

    public AdminBenefitProgramValidationException(String message, List<String> details) {
        super(message);
        this.details = details;
    }

    public List<String> details() {
        return details;
    }
}
