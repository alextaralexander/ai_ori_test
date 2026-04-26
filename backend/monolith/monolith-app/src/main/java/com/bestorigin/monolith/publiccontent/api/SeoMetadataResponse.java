package com.bestorigin.monolith.publiccontent.api;

public record SeoMetadataResponse(
        String titleKey,
        String descriptionKey,
        String canonicalUrl
) {
}
