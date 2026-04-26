package com.bestorigin.monolith.publiccontent.api;

import java.util.Map;

public record ContentSectionResponse(
        String sectionKey,
        String sectionType,
        int sortOrder,
        Map<String, Object> payload
) {
}
