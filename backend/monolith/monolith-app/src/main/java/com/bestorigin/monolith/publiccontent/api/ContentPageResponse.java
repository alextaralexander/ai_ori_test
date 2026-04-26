package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record ContentPageResponse(
        String contentId,
        String templateCode,
        String titleKey,
        String descriptionKey,
        List<BreadcrumbResponse> breadcrumbs,
        SeoMetadataResponse seo,
        List<ContentSectionResponse> sections,
        List<ContentAttachmentResponse> attachments,
        List<ProductLinkResponse> productLinks,
        List<ContentCtaResponse> ctas
) {
}
