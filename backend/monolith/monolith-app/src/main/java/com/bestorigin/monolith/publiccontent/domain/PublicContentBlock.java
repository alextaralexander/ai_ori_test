package com.bestorigin.monolith.publiccontent.domain;

import java.util.Map;
import java.util.UUID;

public record PublicContentBlock(
        UUID id,
        UUID pageConfigId,
        String blockKey,
        String blockType,
        int sortOrder,
        boolean enabled,
        Map<String, Object> payload
) {
}
