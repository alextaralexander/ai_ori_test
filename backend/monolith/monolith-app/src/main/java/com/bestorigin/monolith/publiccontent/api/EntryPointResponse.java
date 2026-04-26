package com.bestorigin.monolith.publiccontent.api;

public record EntryPointResponse(
        String entryKey,
        String labelKey,
        String descriptionKey,
        String targetRoute,
        Audience audience
) {
}
