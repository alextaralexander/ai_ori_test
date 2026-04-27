package com.bestorigin.monolith.auth.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record AuthLoginRequest(String username, String role, String invitationCode) {
    }

    public record AuthSessionResponse(
            String token,
            String userId,
            String displayName,
            List<String> roles,
            String defaultRoute,
            List<AuthRoutePolicyResponse> routePolicies,
            AuthActivePartnerResponse activePartner,
            AuthInvitationCodeResponse invitationCodeState,
            AuthImpersonationResponse impersonation,
            boolean auditRecorded
    ) {
    }

    public record AuthRoutePolicyResponse(String routePattern, String roleCode, String moduleKey, boolean allowed, String deniedCode) {
    }

    public record AuthRouteAccessRequest(String route) {
    }

    public record AuthRouteAccessResponse(boolean allowed, String deniedCode, String defaultRoute, boolean auditRecorded) {
    }

    public record AuthInvitationCodeRequest(String invitationCode) {
    }

    public record AuthInvitationCodeResponse(String invitationCode, String status, String warningCode, boolean auditRecorded) {
    }

    public record AuthPartnerSearchResponse(List<AuthPartnerOptionResponse> items, boolean auditRecorded) {
    }

    public record AuthPartnerOptionResponse(
            String partnerId,
            String personNumber,
            String displayName,
            String roleInStructure,
            String scopeCode
    ) {
    }

    public record AuthActivePartnerRequest(String partnerId) {
    }

    public record AuthActivePartnerResponse(
            String partnerId,
            String personNumber,
            String displayName,
            String roleInStructure,
            String scopeCode,
            String selectedAt
    ) {
    }

    public record AuthImpersonationStartRequest(
            String targetUserId,
            String targetRole,
            String reasonCode,
            String reasonText,
            int durationMinutes
    ) {
    }

    public record AuthImpersonationResponse(
            UUID impersonationSessionId,
            String actorUserId,
            String targetUserId,
            String targetRole,
            String reasonCode,
            String status,
            String startedAt,
            String expiresAt,
            boolean auditRecorded
    ) {
    }

    public record AuthLogoutResponse(boolean loggedOut, boolean auditRecorded) {
    }

    public record AuthErrorResponse(String code, List<String> details, Map<String, String> metadata) {
    }
}
