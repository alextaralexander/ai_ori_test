package com.bestorigin.monolith.publiccontent.impl.service;

public class PublicContentNotFoundException extends RuntimeException {

    public PublicContentNotFoundException(String code) {
        super(code);
    }
}
