package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record NewsFeedResponse(
        List<NewsItemResponse> items,
        NewsItemResponse featured,
        String emptyStateCode
) {
}
