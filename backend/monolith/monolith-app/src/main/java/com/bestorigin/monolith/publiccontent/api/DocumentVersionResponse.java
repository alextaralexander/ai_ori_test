package com.bestorigin.monolith.publiccontent.api;

public record DocumentVersionResponse(
        String versionLabel,
        String publishedAt,
        String viewerUrl,
        String downloadUrl,
        boolean current
) {
}
