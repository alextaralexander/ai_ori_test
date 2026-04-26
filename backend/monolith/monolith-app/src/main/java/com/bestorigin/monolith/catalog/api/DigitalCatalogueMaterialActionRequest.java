package com.bestorigin.monolith.catalog.api;

public record DigitalCatalogueMaterialActionRequest(
        Audience audience,
        String userContextId,
        String returnUrl
) {
}
