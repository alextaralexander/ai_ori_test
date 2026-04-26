package com.bestorigin.monolith.publiccontent.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PublicPageConfig(
        UUID id,
        String pageKey,
        String routePath,
        String titleI18nKey,
        String status,
        OffsetDateTime activeFrom,
        OffsetDateTime activeTo
) {
}
