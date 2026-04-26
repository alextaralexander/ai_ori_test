package com.bestorigin.monolith.publiccontent.api;

public record NewsItemResponse(
        String newsKey,
        String contentId,
        String titleKey,
        String summaryKey,
        String categoryKey,
        String imageUrl,
        String publishedAt,
        String targetRoute
) {
}
