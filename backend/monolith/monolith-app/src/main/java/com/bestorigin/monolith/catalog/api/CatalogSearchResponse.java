package com.bestorigin.monolith.catalog.api;

import java.util.List;

public record CatalogSearchResponse(
        List<CatalogProductCardResponse> items,
        List<CatalogProductCardResponse> recommendations,
        int page,
        int pageSize,
        int totalItems,
        boolean hasNextPage,
        String messageCode
) {
}
