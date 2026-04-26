package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record BenefitLandingResponse(
        BenefitLandingType landingType,
        String routePath,
        String campaignId,
        String variant,
        SeoMetadataResponse seo,
        ReferralContextResponse referral,
        List<BenefitLandingBlockResponse> blocks
) {
}
