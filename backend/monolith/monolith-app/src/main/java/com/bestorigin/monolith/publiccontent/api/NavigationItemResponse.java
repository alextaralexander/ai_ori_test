package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record NavigationItemResponse(
        String itemKey,
        String labelKey,
        String targetType,
        String targetValue,
        String area,
        List<NavigationItemResponse> children
) {
}
