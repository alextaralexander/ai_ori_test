package com.bestorigin.monolith.employee.impl.exception;

public class EmployeeValidationException extends RuntimeException {

    private final int statusCode;

    public EmployeeValidationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
