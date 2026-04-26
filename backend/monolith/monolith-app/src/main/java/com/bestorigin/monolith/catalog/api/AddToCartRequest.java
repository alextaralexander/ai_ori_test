package com.bestorigin.monolith.catalog.api;

public record AddToCartRequest(
        String productId,
        int quantity,
        Audience audience,
        String userContextId,
        String searchUrl
) {
}
