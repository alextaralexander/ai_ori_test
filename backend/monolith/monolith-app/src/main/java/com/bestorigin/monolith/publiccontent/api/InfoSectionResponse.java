package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record InfoSectionResponse(
        String sectionCode,
        String titleKey,
        String descriptionKey,
        List<BreadcrumbResponse> breadcrumbs,
        SeoMetadataResponse seo,
        List<ContentSectionResponse> sections,
        List<InfoRelatedDocumentResponse> documents,
        List<ContentCtaResponse> ctas
) {
}
