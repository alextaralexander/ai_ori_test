package com.bestorigin.monolith.catalog.api;

import java.time.OffsetDateTime;

public record DigitalCatalogueMaterialActionResponse(
        String url,
        OffsetDateTime expiresAt,
        String messageCode
) {
}
