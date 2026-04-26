package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record DocumentCollectionResponse(
        String documentType,
        String titleKey,
        String descriptionKey,
        List<BreadcrumbResponse> breadcrumbs,
        List<DocumentResponse> documents,
        String emptyStateCode
) {
}
