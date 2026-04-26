package com.bestorigin.monolith.publiccontent.api;

public record ReferralContextResponse(
        String code,
        ReferralCodeStatus status,
        String sponsorPublicNameKey,
        String messageCode
) {
}
