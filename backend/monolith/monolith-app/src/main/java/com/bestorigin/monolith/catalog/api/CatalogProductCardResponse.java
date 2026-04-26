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
        String unavailableReasonCode,
        String productCode,
        String name,
        String categoryName,
        String brand,
        String volumeLabel,
        String campaignCode,
        CatalogOrderLimits orderLimits,
        List<CatalogProductMedia> media,
        CatalogProductInformation information,
        List<CatalogProductAttachment> attachments,
        List<CatalogProductRecommendation> recommendations
) {
    public record CatalogOrderLimits(int minQuantity, int maxQuantity) {
    }

    public record CatalogProductMedia(String url, String altText, boolean primary, int sortOrder) {
    }

    public record CatalogProductInformation(
            String shortDescription,
            String fullDescription,
            String usageInstructions,
            String ingredients,
            List<CatalogCharacteristic> characteristics
    ) {
    }

    public record CatalogCharacteristic(String name, String value) {
    }

    public record CatalogProductAttachment(String title, String documentType, String url) {
    }

    public record CatalogProductRecommendation(
            String productCode,
            String name,
            String imageUrl,
            BigDecimal price,
            String currency,
            AvailabilityStatus availability,
            String recommendationType
    ) {
    }
}
