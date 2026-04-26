package com.bestorigin.monolith.catalog.api;

import java.math.BigDecimal;
import java.util.List;

public record CatalogProductCardResponse(
        String id,
        String sku,
        String slug,
        String nameKey,
        String descriptionKey,
        String categorySlug,
        String imageUrl,
        BigDecimal price,
        String currency,
        AvailabilityStatus availability,
        List<String> tags,
        List<String> promoBadges,
        boolean canAddToCart,
        String unavailableReasonCode
) {
}
