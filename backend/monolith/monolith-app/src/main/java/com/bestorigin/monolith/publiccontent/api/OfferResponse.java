package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record OfferResponse(
        String offerId,
        String titleKey,
        String summaryKey,
        List<BreadcrumbResponse> breadcrumbs,
        SeoMetadataResponse seo,
        OfferHeroResponse hero,
        List<ContentSectionResponse> sections,
        List<ContentAttachmentResponse> attachments,
        List<ProductLinkResponse> productLinks,
        List<ContentCtaResponse> ctas
) {
}
