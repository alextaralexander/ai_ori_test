package com.bestorigin.monolith.auth.impl.service;

import com.bestorigin.monolith.auth.api.AuthDtos.AuthActivePartnerRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthActivePartnerResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthImpersonationResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthImpersonationStartRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthInvitationCodeRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthInvitationCodeResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthLoginRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthLogoutResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthPartnerOptionResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthPartnerSearchResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthRouteAccessRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthRouteAccessResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthRoutePolicyResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthSessionResponse;
import com.bestorigin.monolith.auth.domain.AuthSessionRepository;
import com.bestorigin.monolith.auth.domain.AuthSessionSnapshot;
import com.bestorigin.monolith.auth.impl.exception.AuthAccessDeniedException;
import com.bestorigin.monolith.auth.impl.exception.AuthSessionExpiredException;
import com.bestorigin.monolith.auth.impl.exception.AuthValidationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultAuthService implements AuthService {

    private static final AuthActivePartnerResponse DEFAULT_PARTNER = new AuthActivePartnerResponse(
            "PART-024-001",
            "P-024-0001",
            "Partner 024",
            "DOWNLINE_LEADER",
            "DOWNLINE",
            "2026-04-27T08:00:00Z"
    );

    private final AuthSessionRepository repository;

    public DefaultAuthService(AuthSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthSessionResponse login(AuthLoginRequest request) {
        String role = normalizeRole(request);
        String token = "test-token-" + role;
        AuthSessionSnapshot snapshot = new AuthSessionSnapshot(
                token,
                userId(role),
                displayName(role),
                roles(role),
                defaultRoute(role),
                request == null ? null : request.invitationCode(),
                invitationStatus(request == null ? null : request.invitationCode()),
                isPartner(role) ? DEFAULT_PARTNER : null,
                null,
                Instant.now().plusSeconds(3600),
                false
        );
        repository.save(snapshot);
        return response(snapshot);
    }

    @Override
    public AuthSessionResponse currentSession(String token) {
        return response(resolve(token));
    }

    @Override
    public AuthLogoutResponse logout(String token) {
        AuthSessionSnapshot current = resolve(token);
        repository.save(new AuthSessionSnapshot(
                current.token(),
                current.userId(),
                current.displayName(),
                current.roles(),
                current.defaultRoute(),
                null,
                "NOT_PROVIDED",
                null,
                null,
                current.expiresAt(),
                true
        ));
        return new AuthLogoutResponse(true, true);
    }

    @Override
    public AuthRouteAccessResponse routeAccess(String token, AuthRouteAccessRequest request) {
        AuthSessionSnapshot current = resolve(token);
        if (request == null || blank(request.route())) {
            throw new AuthValidationException("STR_MNEMO_AUTH_ROUTE_INVALID");
        }
        if (!allowed(current.roles(), request.route())) {
            throw new AuthAccessDeniedException("STR_MNEMO_AUTH_ACCESS_DENIED");
        }
        return new AuthRouteAccessResponse(true, null, current.defaultRoute(), true);
    }

    @Override
    public AuthInvitationCodeResponse saveInvitationCode(String token, AuthInvitationCodeRequest request) {
        AuthSessionSnapshot current = resolve(token);
        String code = request == null ? null : request.invitationCode();
        String status = invitationStatus(code);
        repository.save(new AuthSessionSnapshot(
                current.token(),
                current.userId(),
                current.displayName(),
                current.roles(),
                current.defaultRoute(),
                code,
                status,
                current.activePartner(),
                current.impersonation(),
                current.expiresAt(),
                current.revoked()
        ));
        return invitation(code);
    }

    @Override
    public AuthPartnerSearchResponse searchPartners(String token, String query) {
        AuthSessionSnapshot current = resolve(token);
        if (query == null || query.trim().length() < 3) {
            throw new AuthValidationException("STR_MNEMO_AUTH_PARTNER_SEARCH_INVALID");
        }
        if (!(hasRole(current, "partner") || hasRole(current, "partner-leader") || hasRole(current, "employee-support") || hasRole(current, "supervisor"))) {
            throw new AuthAccessDeniedException("STR_MNEMO_AUTH_PARTNER_SCOPE_DENIED");
        }
        return new AuthPartnerSearchResponse(List.of(partnerOption()), true);
    }

    @Override
    public AuthSessionResponse setActivePartner(String token, AuthActivePartnerRequest request) {
        AuthSessionSnapshot current = resolve(token);
        if (request == null || !"PART-024-001".equals(request.partnerId())) {
            throw new AuthAccessDeniedException("STR_MNEMO_AUTH_PARTNER_SCOPE_DENIED");
        }
        AuthSessionSnapshot updated = new AuthSessionSnapshot(
                current.token(),
                current.userId(),
                current.displayName(),
                current.roles(),
                current.defaultRoute(),
                current.invitationCode(),
                current.invitationStatus(),
                DEFAULT_PARTNER,
                current.impersonation(),
                current.expiresAt(),
                current.revoked()
        );
        repository.save(updated);
        return response(updated);
    }

    @Override
    public AuthImpersonationResponse startImpersonation(String token, UUID elevatedSessionId, AuthImpersonationStartRequest request) {
        AuthSessionSnapshot current = resolve(token);
        validateImpersonationRequest(request);
        if (!(hasRole(current, "supervisor") || hasRole(current, "admin") || (hasRole(current, "employee-support") && !current.userId().contains("no-elevated")))) {
            throw new AuthAccessDeniedException("STR_MNEMO_AUTH_IMPERSONATION_FORBIDDEN");
        }
        AuthImpersonationResponse impersonation = new AuthImpersonationResponse(
                UUID.fromString("02400000-0000-0000-0000-000000000004"),
                current.userId(),
                request.targetUserId(),
                request.targetRole(),
                request.reasonCode(),
                "ACTIVE",
                "2026-04-27T08:10:00Z",
                "2026-04-27T08:30:00Z",
                true
        );
        repository.save(new AuthSessionSnapshot(
                current.token(),
                current.userId(),
                current.displayName(),
                current.roles(),
                current.defaultRoute(),
                current.invitationCode(),
                current.invitationStatus(),
                current.activePartner(),
                impersonation,
                current.expiresAt(),
                current.revoked()
        ));
        return impersonation;
    }

    @Override
    public AuthSessionResponse finishImpersonation(String token, UUID impersonationSessionId) {
        AuthSessionSnapshot current = resolve(token);
        AuthSessionSnapshot updated = new AuthSessionSnapshot(
                current.token(),
                current.userId(),
                current.displayName(),
                current.roles(),
                current.defaultRoute(),
                current.invitationCode(),
                current.invitationStatus(),
                current.activePartner(),
                null,
                current.expiresAt(),
                current.revoked()
        );
        repository.save(updated);
        return response(updated);
    }

    private AuthSessionSnapshot resolve(String rawToken) {
        String token = normalizeToken(rawToken);
        return repository.findByToken(token)
                .filter(snapshot -> !snapshot.revoked())
                .orElseGet(() -> {
                    String role = roleFromToken(token);
                    if (blank(role)) {
                        throw new AuthSessionExpiredException("STR_MNEMO_AUTH_SESSION_EXPIRED");
                    }
                    AuthSessionSnapshot snapshot = new AuthSessionSnapshot(token, userId(role), displayName(role), roles(role), defaultRoute(role), null, "NOT_PROVIDED", isPartner(role) ? DEFAULT_PARTNER : null, null, Instant.now().plusSeconds(3600), false);
                    repository.save(snapshot);
                    return snapshot;
                });
    }

    private AuthSessionResponse response(AuthSessionSnapshot snapshot) {
        return new AuthSessionResponse(
                snapshot.token(),
                snapshot.userId(),
                snapshot.displayName(),
                snapshot.roles(),
                snapshot.defaultRoute(),
                routePolicies(snapshot.roles()),
                snapshot.activePartner(),
                new AuthInvitationCodeResponse(snapshot.invitationCode(), snapshot.invitationStatus(), warning(snapshot.invitationStatus()), true),
                snapshot.impersonation(),
                true
        );
    }

    private static List<AuthRoutePolicyResponse> routePolicies(List<String> roles) {
        return roles.stream()
                .map(role -> new AuthRoutePolicyResponse(pattern(role), role, module(role), true, null))
                .toList();
    }

    private static AuthInvitationCodeResponse invitation(String code) {
        String status = invitationStatus(code);
        return new AuthInvitationCodeResponse(code, status, warning(status), true);
    }

    private static AuthPartnerOptionResponse partnerOption() {
        return new AuthPartnerOptionResponse("PART-024-001", "P-024-0001", "Partner 024", "DOWNLINE_LEADER", "DOWNLINE");
    }

    private static boolean allowed(List<String> roles, String route) {
        if (route.startsWith("/employee")) {
            return roles.stream().anyMatch(role -> role.contains("employee") || "supervisor".equals(role) || "admin".equals(role));
        }
        if (route.startsWith("/business")) {
            return roles.stream().anyMatch(role -> role.contains("partner") || "supervisor".equals(role) || "admin".equals(role));
        }
        if (route.startsWith("/admin")) {
            return roles.contains("admin");
        }
        return true;
    }

    private static void validateImpersonationRequest(AuthImpersonationStartRequest request) {
        if (request == null || blank(request.targetUserId()) || blank(request.targetRole()) || blank(request.reasonCode()) || blank(request.reasonText()) || request.durationMinutes() < 1 || request.durationMinutes() > 120) {
            throw new AuthValidationException("STR_MNEMO_AUTH_IMPERSONATION_INVALID");
        }
    }

    private static String normalizeRole(AuthLoginRequest request) {
        if (request == null) {
            return "guest";
        }
        if (!blank(request.role())) {
            return request.role();
        }
        return blank(request.username()) ? "guest" : request.username();
    }

    private static String normalizeToken(String rawToken) {
        if (rawToken == null) {
            return "";
        }
        return rawToken.replace("Bearer ", "").trim();
    }

    private static String roleFromToken(String token) {
        if (token.startsWith("test-token-")) {
            return token.substring("test-token-".length());
        }
        if (token.endsWith("-api-session-ui")) {
            return token.substring(0, token.length() - "-api-session-ui".length());
        }
        if (token.contains("-api-session-")) {
            return token.substring(0, token.indexOf("-api-session-"));
        }
        return token;
    }

    private static List<String> roles(String role) {
        return switch (role) {
            case "supervisor" -> List.of("supervisor", "employee-support");
            case "admin" -> List.of("admin", "supervisor", "employee-support");
            case "partner-leader" -> List.of("partner-leader", "partner");
            default -> List.of(role);
        };
    }

    private static String userId(String role) {
        return switch (role) {
            case "supervisor" -> "EMP-024-SUP";
            case "employee-support", "employee-support-no-elevated" -> "EMP-024-001" + ("employee-support-no-elevated".equals(role) ? "-no-elevated" : "");
            case "partner", "partner-leader" -> "USR-024-PARTNER";
            case "customer" -> "USR-024-CUST";
            default -> role + "-user";
        };
    }

    private static String displayName(String role) {
        return switch (role) {
            case "supervisor" -> "Supervisor 024";
            case "partner", "partner-leader" -> "Partner 024";
            case "customer" -> "Customer 024";
            default -> role;
        };
    }

    private static String defaultRoute(String role) {
        return switch (role) {
            case "customer" -> "/profile-settings";
            case "partner", "partner-leader" -> "/business";
            case "employee-support", "employee-support-no-elevated" -> "/employee";
            case "supervisor" -> "/employee/super-user";
            case "admin" -> "/admin";
            default -> "/";
        };
    }

    private static String pattern(String role) {
        if (role.contains("employee") || "supervisor".equals(role)) {
            return "/employee/**";
        }
        if (role.contains("partner")) {
            return "/business/**";
        }
        if ("customer".equals(role)) {
            return "/profile-settings/**";
        }
        return "/**";
    }

    private static String module(String role) {
        if (role.contains("employee") || "supervisor".equals(role)) {
            return "employee";
        }
        if (role.contains("partner")) {
            return "partner";
        }
        return "auth";
    }

    private static String invitationStatus(String code) {
        if (blank(code)) {
            return "NOT_PROVIDED";
        }
        if ("INV-024-SPONSOR".equals(code)) {
            return "VALID";
        }
        return "UNKNOWN";
    }

    private static String warning(String status) {
        return "UNKNOWN".equals(status) ? "STR_MNEMO_AUTH_INVITATION_CODE_INVALID" : null;
    }

    private static boolean hasRole(AuthSessionSnapshot snapshot, String role) {
        return snapshot.roles().contains(role);
    }

    private static boolean isPartner(String role) {
        return role.contains("partner");
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
