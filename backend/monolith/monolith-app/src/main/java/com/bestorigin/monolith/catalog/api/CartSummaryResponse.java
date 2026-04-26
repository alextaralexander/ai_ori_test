package com.bestorigin.monolith.catalog.api;

public record CartSummaryResponse(
        int itemsCount,
        int totalQuantity,
        String messageCode,
        boolean partnerContext
) {
}
