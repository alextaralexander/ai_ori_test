package com.bestorigin.monolith.publiccontent.api;

public record FaqCategoryResponse(
        String categoryKey,
        String titleKey,
        int questionCount
) {
}
