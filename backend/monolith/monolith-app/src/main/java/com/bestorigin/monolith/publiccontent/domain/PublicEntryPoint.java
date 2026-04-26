package com.bestorigin.monolith.publiccontent.domain;

import java.util.UUID;

public record PublicEntryPoint(
        UUID id,
        String entryKey,
        String labelI18nKey,
        String descriptionI18nKey,
        String targetRoute,
        String audience,
        int sortOrder,
        boolean enabled
) {
}
