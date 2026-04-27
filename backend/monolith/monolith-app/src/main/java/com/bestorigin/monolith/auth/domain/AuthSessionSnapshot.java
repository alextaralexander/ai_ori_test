package com.bestorigin.monolith.auth.domain;

import com.bestorigin.monolith.auth.api.AuthDtos.AuthActivePartnerResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthImpersonationResponse;
import java.time.Instant;
import java.util.List;

public record AuthSessionSnapshot(
        String token,
        String userId,
        String displayName,
        List<String> roles,
        String defaultRoute,
        String invitationCode,
        String invitationStatus,
        AuthActivePartnerResponse activePartner,
        AuthImpersonationResponse impersonation,
        Instant expiresAt,
        boolean revoked
) {
}
