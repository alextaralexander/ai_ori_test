package com.bestorigin.monolith.catalog.impl.service;

public class DigitalCatalogueNotFoundException extends RuntimeException {

    public DigitalCatalogueNotFoundException(String messageCode) {
        super(messageCode);
    }
}
