package com.bestorigin.monolith.publiccontent.domain;

import java.util.UUID;

public record PublicNavigationItem(
        UUID id,
        UUID parentId,
        String area,
        String itemKey,
        String labelI18nKey,
        String targetType,
        String targetValue,
        int sortOrder,
        String audience,
        boolean enabled
) {
}
