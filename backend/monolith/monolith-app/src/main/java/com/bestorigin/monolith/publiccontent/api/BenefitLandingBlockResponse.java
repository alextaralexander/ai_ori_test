package com.bestorigin.monolith.publiccontent.api;

import java.util.List;
import java.util.Map;

public record BenefitLandingBlockResponse(
        String blockKey,
        BenefitLandingBlockType blockType,
        String titleKey,
        String bodyKey,
        Map<String, Object> payload,
        int sortOrder,
        List<BenefitLandingCtaResponse> ctas
) {
}
