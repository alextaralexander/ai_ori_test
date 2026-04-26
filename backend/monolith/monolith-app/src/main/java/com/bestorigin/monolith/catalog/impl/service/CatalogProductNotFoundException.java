package com.bestorigin.monolith.catalog.impl.service;

public class CatalogProductNotFoundException extends RuntimeException {

    public CatalogProductNotFoundException(String messageCode) {
        super(messageCode);
    }
}
