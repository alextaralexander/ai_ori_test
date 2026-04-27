package com.bestorigin.monolith.admincatalog.impl.exception;

import java.util.List;

public class AdminCatalogValidationException extends RuntimeException {

    private final List<String> details;

    public AdminCatalogValidationException(String message, List<String> details) {
        super(message);
        this.details = details;
    }

    public List<String> details() {
        return details;
    }
}
