package com.bestorigin.monolith.catalog.impl.service;

public class CatalogItemUnavailableException extends RuntimeException {

    public CatalogItemUnavailableException(String message) {
        super(message);
    }
}
