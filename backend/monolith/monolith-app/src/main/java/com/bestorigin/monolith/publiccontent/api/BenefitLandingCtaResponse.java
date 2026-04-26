package com.bestorigin.monolith.publiccontent.api;

public record BenefitLandingCtaResponse(
        BenefitCtaType ctaType,
        String labelKey,
        String targetRoute,
        boolean preserveReferralContext
) {
}
