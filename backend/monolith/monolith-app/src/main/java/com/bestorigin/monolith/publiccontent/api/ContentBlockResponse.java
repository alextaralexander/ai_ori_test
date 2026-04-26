package com.bestorigin.monolith.publiccontent.api;

import java.util.Map;

public record ContentBlockResponse(
        String blockKey,
        String blockType,
        int sortOrder,
        Map<String, Object> payload
) {
}
