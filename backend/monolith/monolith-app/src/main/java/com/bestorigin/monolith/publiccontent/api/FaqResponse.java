package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record FaqResponse(
        List<FaqCategoryResponse> categories,
        List<FaqItemResponse> items,
        String emptyStateCode
) {
}
