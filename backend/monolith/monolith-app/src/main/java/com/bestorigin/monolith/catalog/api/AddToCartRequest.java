package com.bestorigin.monolith.catalog.api;

public record AddToCartRequest(
        String productId,
        String productCode,
        int quantity,
        Audience audience,
        String userContextId,
        String searchUrl,
        String partnerContextId,
        String source
) {
}
