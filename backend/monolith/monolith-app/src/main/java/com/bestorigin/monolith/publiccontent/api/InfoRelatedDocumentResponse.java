package com.bestorigin.monolith.publiccontent.api;

public record InfoRelatedDocumentResponse(
        String documentType,
        String titleKey,
        String targetRoute
) {
}
