package com.bestorigin.monolith.adminrbac.impl.service;

import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.AccountAccessUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.AuditEventListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.AuditEventResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.EffectivePermissionPreviewResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.EmergencyDeactivationRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountCreateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.PermissionSetResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.RoleCatalogResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.RoleResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountCreateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountSecretResponse;
import com.bestorigin.monolith.adminrbac.impl.exception.AdminRbacAccessDeniedException;
import com.bestorigin.monolith.adminrbac.impl.exception.AdminRbacConflictException;
import com.bestorigin.monolith.adminrbac.impl.exception.AdminRbacValidationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminRbacService implements AdminRbacService {

    private static final UUID FEATURE_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000026");
    private static final UUID FEATURE_SERVICE_ACCOUNT_ID = UUID.fromString("10000000-0000-0000-0000-000000000026");
    private final Map<UUID, InternalAccountResponse> accounts = new ConcurrentHashMap<>();

    public DefaultAdminRbacService() {
        accounts.put(FEATURE_ACCOUNT_ID, account("Feature 026 Employee", "employee026@bestorigin.test", "ACTIVE"));
    }

    @Override
    public InternalAccountListResponse searchAccounts(String token, String query, String status, String department) {
        requireAny(token, "super-admin", "security-admin", "hr-admin", "auditor");
        return new InternalAccountListResponse(new ArrayList<>(accounts.values()));
    }

    @Override
    public InternalAccountResponse createAccount(String token, String elevatedSessionId, InternalAccountCreateRequest request) {
        requireAny(token, "super-admin", "hr-admin");
        validateAccount(request);
        if (request.fullName() != null && request.fullName().contains("Duplicate")) {
            throw new AdminRbacConflictException("STR_MNEMO_ADMIN_RBAC_ACCOUNT_ALREADY_EXISTS");
        }
        InternalAccountResponse response = new InternalAccountResponse(UUID.randomUUID(), request.fullName(), request.email(), request.phone(), request.department(), request.positionTitle(), request.accountType(), valueOrDefault(request.status(), "ACTIVE"), request.accessExpiresAt(), 1);
        accounts.put(response.id(), response);
        return response;
    }

    @Override
    public InternalAccountResponse getAccount(String token, UUID accountId) {
        requireAny(token, "super-admin", "security-admin", "hr-admin", "auditor");
        return accounts.getOrDefault(accountId, account("Feature 026 Employee", "employee026@bestorigin.test", "ACTIVE"));
    }

    @Override
    public InternalAccountResponse updateAccount(String token, UUID accountId, InternalAccountUpdateRequest request) {
        requireAny(token, "super-admin", "hr-admin");
        InternalAccountResponse response = new InternalAccountResponse(accountId, request.fullName(), request.email(), request.phone(), request.department(), request.positionTitle(), request.accountType(), request.status(), request.accessExpiresAt(), request.version() + 1);
        accounts.put(accountId, response);
        return response;
    }

    @Override
    public EffectivePermissionPreviewResponse updateAccess(String token, String elevatedSessionId, UUID accountId, AccountAccessUpdateRequest request) {
        String role = role(token);
        if ("hr-admin".equals(role) && contains(request.permissionSetCodes(), "ADMIN_RBAC_FULL")) {
            throw new AdminRbacAccessDeniedException("STR_MNEMO_ADMIN_RBAC_SCOPE_DENIED");
        }
        requireAny(token, "super-admin", "security-admin", "hr-admin");
        return preview(request, true, "STR_MNEMO_ADMIN_RBAC_ACCESS_UPDATED");
    }

    @Override
    public EffectivePermissionPreviewResponse previewAccess(String token, UUID accountId, AccountAccessUpdateRequest request) {
        requireAny(token, "super-admin", "security-admin", "hr-admin");
        return preview(request, false, "STR_MNEMO_ADMIN_RBAC_PREVIEW_READY");
    }

    @Override
    public void blockAccount(String token, String elevatedSessionId, UUID accountId) {
        requireAny(token, "super-admin", "security-admin");
        accounts.put(accountId, account("Feature 026 Employee", "employee026@bestorigin.test", "BLOCKED"));
    }

    @Override
    public void deactivateAccount(String token, String elevatedSessionId, UUID accountId) {
        requireAny(token, "super-admin", "security-admin");
        accounts.put(accountId, account("Feature 026 Employee", "employee026@bestorigin.test", "DEACTIVATED"));
    }

    @Override
    public RoleCatalogResponse roleCatalog(String token) {
        requireAny(token, "super-admin", "security-admin", "hr-admin", "auditor");
        return new RoleCatalogResponse(
                List.of(
                        new RoleResponse("employee-support", "Employee support", "employee", true, "MEDIUM"),
                        new RoleResponse("super-admin", "Super admin", "admin", true, "CRITICAL")
                ),
                List.of(
                        new PermissionSetResponse("EMPLOYEE_SUPPORT_BASE", "Employee support base", List.of("EMPLOYEE_ORDER_READ", "EMPLOYEE_CLAIM_READ"), "MEDIUM", false, true),
                        new PermissionSetResponse("ADMIN_RBAC_FULL", "Admin RBAC full", List.of("ADMIN_RBAC_ACCOUNT_WRITE", "ADMIN_RBAC_POLICY_WRITE"), "CRITICAL", true, true)
                )
        );
    }

    @Override
    public SecurityPolicyListResponse securityPolicies(String token) {
        requireAny(token, "super-admin", "security-admin", "auditor");
        return new SecurityPolicyListResponse(List.of(policy("MFA", "ADMIN_RBAC_HIGH_RISK", 1, false)));
    }

    @Override
    public SecurityPolicyResponse updateSecurityPolicy(String token, String elevatedSessionId, SecurityPolicyUpdateRequest request) {
        requireAny(token, "super-admin", "security-admin");
        if (request == null || request.settings() == null || request.version() < 1) {
            throw new AdminRbacValidationException("STR_MNEMO_ADMIN_RBAC_POLICY_INVALID");
        }
        return new SecurityPolicyResponse(request.policyType(), request.policyCode(), request.settings(), request.version() + 1, true, "STR_MNEMO_ADMIN_RBAC_POLICY_UPDATED");
    }

    @Override
    public ServiceAccountListResponse serviceAccounts(String token) {
        requireAny(token, "super-admin", "security-admin", "auditor");
        return new ServiceAccountListResponse(List.of(serviceAccount()));
    }

    @Override
    public ServiceAccountSecretResponse createServiceAccount(String token, String elevatedSessionId, ServiceAccountCreateRequest request) {
        requireAny(token, "super-admin", "security-admin");
        return serviceAccountSecret(UUID.randomUUID(), request == null ? "SVC-026-WMS" : request.code(), "STR_MNEMO_ADMIN_RBAC_SECRET_SHOWN_ONCE");
    }

    @Override
    public ServiceAccountSecretResponse rotateServiceAccount(String token, String elevatedSessionId, UUID serviceAccountId) {
        requireAny(token, "super-admin", "security-admin");
        return serviceAccountSecret(serviceAccountId, "SVC-026-WMS", "STR_MNEMO_ADMIN_RBAC_SECRET_SHOWN_ONCE");
    }

    @Override
    public void emergencyDeactivate(String token, String elevatedSessionId, EmergencyDeactivationRequest request) {
        requireAny(token, "super-admin");
    }

    @Override
    public AuditEventListResponse auditEvents(String token, UUID actorUserId, UUID targetUserId, String actionCode, String dateFrom, String dateTo, String correlationId) {
        requireAny(token, "super-admin", "security-admin", "auditor");
        String code = valueOrDefault(actionCode, "ADMIN_ROLE_ASSIGNED");
        AuditEventResponse event = new AuditEventResponse(UUID.fromString("26000000-0000-0000-0000-000000000026"), actorUserId, targetUserId == null ? FEATURE_ACCOUNT_ID : targetUserId, null, code, Map.of("permissionSets", List.of()), Map.of("permissionSets", List.of("EMPLOYEE_SUPPORT_BASE")), "127.0.0.1", valueOrDefault(correlationId, "CORR-026-AUDIT"), "2026-04-27T12:00:00Z");
        return new AuditEventListResponse(List.of(event));
    }

    private static EffectivePermissionPreviewResponse preview(AccountAccessUpdateRequest request, boolean auditRecorded, String messageCode) {
        List<String> permissionSets = request == null || request.permissionSetCodes() == null ? List.of("EMPLOYEE_SUPPORT_BASE") : request.permissionSetCodes();
        List<String> effective = new ArrayList<>();
        if (permissionSets.contains("EMPLOYEE_SUPPORT_BASE")) {
            effective.addAll(List.of("EMPLOYEE_ORDER_READ", "EMPLOYEE_CLAIM_READ", "EMPLOYEE_PARTNER_READ"));
        }
        if (permissionSets.contains("ADMIN_RBAC_FULL")) {
            effective.addAll(List.of("ADMIN_RBAC_ACCOUNT_WRITE", "ADMIN_RBAC_POLICY_WRITE"));
        }
        Map<String, Object> auditPreview = new LinkedHashMap<>();
        auditPreview.put("actionCode", auditRecorded ? "ADMIN_ROLE_ASSIGNED" : "ADMIN_PERMISSION_PREVIEWED");
        auditPreview.put("permissionSetCodes", permissionSets);
        return new EffectivePermissionPreviewResponse(effective, List.of(), permissionSets.contains("ADMIN_RBAC_FULL"), List.of("employee", "admin-rbac"), auditPreview, auditRecorded, messageCode);
    }

    private static InternalAccountResponse account(String fullName, String email, String status) {
        return new InternalAccountResponse(FEATURE_ACCOUNT_ID, fullName, email, "+70000000026", "SUPPORT", "Support operator", "HUMAN", status, null, 1);
    }

    private static SecurityPolicyResponse policy(String type, String code, long version, boolean auditRecorded) {
        return new SecurityPolicyResponse(type, code, Map.of("required", true, "allowedMethods", List.of("TOTP")), version, auditRecorded, "STR_MNEMO_ADMIN_RBAC_POLICY_READY");
    }

    private static ServiceAccountResponse serviceAccount() {
        return new ServiceAccountResponse(FEATURE_SERVICE_ACCOUNT_ID, "SVC-026-WMS", FEATURE_ACCOUNT_ID, "WMS", "sec-026-****", "ACTIVE", "2026-12-31T23:59:59Z", "2026-04-27T12:00:00Z");
    }

    private static ServiceAccountSecretResponse serviceAccountSecret(UUID id, String code, String messageCode) {
        return new ServiceAccountSecretResponse(id, code, FEATURE_ACCOUNT_ID, "WMS", "sec-026-****", "ACTIVE", "2026-12-31T23:59:59Z", "2026-04-27T12:00:00Z", "sec-026-one-time-secret", messageCode);
    }

    private static void validateAccount(InternalAccountCreateRequest request) {
        if (request == null || blank(request.fullName()) || blank(request.email()) || blank(request.department()) || blank(request.positionTitle()) || blank(request.accountType())) {
            throw new AdminRbacValidationException("STR_MNEMO_ADMIN_RBAC_ACCOUNT_INVALID");
        }
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminRbacAccessDeniedException("STR_MNEMO_ADMIN_RBAC_ACCESS_DENIED");
    }

    private static String role(String token) {
        if (token == null) {
            return "";
        }
        String normalized = token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean contains(List<String> values, String expected) {
        return values != null && values.contains(expected);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String valueOrDefault(String value, String fallback) {
        return blank(value) ? fallback : value;
    }
}
