package com.bestorigin.monolith.publiccontent.api;

public record ProductLinkResponse(
        String productRef,
        String labelKey,
        String targetRoute
) {
}
