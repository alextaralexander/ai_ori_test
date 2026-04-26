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
        String brand,
        String volumeLabel,
        String campaignCode,
        BigDecimal price,
        BigDecimal promoPrice,
        String currency,
        AvailabilityStatus availability,
        int availableQuantity,
        int minOrderQuantity,
        int maxOrderQuantity,
        boolean published,
        int popularRank,
        OffsetDateTime createdAt,
        List<String> tags,
        List<String> promoBadges
) {
}
