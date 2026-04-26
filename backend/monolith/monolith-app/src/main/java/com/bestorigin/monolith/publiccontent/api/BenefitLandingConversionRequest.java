package com.bestorigin.monolith.publiccontent.api;

public record BenefitLandingConversionRequest(
        BenefitLandingType landingType,
        String variant,
        String referralCode,
        String campaignId,
        String ctaType,
        String routePath,
        String occurredAt,
        String anonymousSessionId
) {
}
