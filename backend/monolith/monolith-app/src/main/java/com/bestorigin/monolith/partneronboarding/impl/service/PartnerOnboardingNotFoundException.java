package com.bestorigin.monolith.partneronboarding.impl.service;

public class PartnerOnboardingNotFoundException extends RuntimeException {

    public PartnerOnboardingNotFoundException(String messageCode) {
        super(messageCode);
    }
}
