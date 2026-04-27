package com.bestorigin.monolith.profile.domain;

import java.util.UUID;

public record ProfileSnapshot(
        UUID profileId,
        String ownerUserId,
        String firstName,
        String lastName,
        String preferredLanguage
) {
}
