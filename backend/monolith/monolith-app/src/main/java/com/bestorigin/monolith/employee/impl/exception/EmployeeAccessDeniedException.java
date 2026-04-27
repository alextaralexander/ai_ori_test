package com.bestorigin.monolith.employee.impl.exception;

public class EmployeeAccessDeniedException extends RuntimeException {

    public EmployeeAccessDeniedException(String message) {
        super(message);
    }
}
