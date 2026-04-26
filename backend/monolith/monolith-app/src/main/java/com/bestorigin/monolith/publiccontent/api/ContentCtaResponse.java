package com.bestorigin.monolith.publiccontent.api;

public record ContentCtaResponse(
        String labelKey,
        String targetType,
        String targetValue,
        Audience audience
) {
}
