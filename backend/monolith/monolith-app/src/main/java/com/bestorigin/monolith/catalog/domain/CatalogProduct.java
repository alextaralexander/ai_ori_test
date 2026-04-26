package com.bestorigin.monolith.catalog.domain;

import com.bestorigin.monolith.catalog.api.AvailabilityStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CatalogProduct(
        String id,
        String sku,
        String slug,
        String nameKey,
        String descriptionKey,
        String categorySlug,
        String imageUrl,
        String campaignCode,
        BigDecimal price,
        String currency,
        AvailabilityStatus availability,
        boolean published,
        int popularRank,
        OffsetDateTime createdAt,
        List<String> tags,
        List<String> promoBadges
) {
}
