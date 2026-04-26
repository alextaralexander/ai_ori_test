package com.bestorigin.monolith.catalog.impl.service;

public class DigitalCatalogueForbiddenException extends RuntimeException {

    public DigitalCatalogueForbiddenException(String messageCode) {
        super(messageCode);
    }
}
