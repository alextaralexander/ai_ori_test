package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record DocumentResponse(
        String documentKey,
        String documentType,
        String titleKey,
        String descriptionKey,
        String versionLabel,
        String publishedAt,
        String viewerUrl,
        String downloadUrl,
        boolean required,
        boolean current,
        Audience audience,
        List<DocumentVersionResponse> archive
) {
}
