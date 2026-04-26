package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record FaqItemResponse(
        String itemKey,
        String categoryKey,
        String questionKey,
        String answerKey,
        List<String> tags,
        String relatedInfoSection,
        String relatedDocumentType,
        Audience audience
) {
}
