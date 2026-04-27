package com.bestorigin.monolith.adminrbac.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminRbacDtos {

    private AdminRbacDtos() {
    }

    public record AdminRbacErrorResponse(String code, String correlationId) {
    }

    public record InternalAccountCreateRequest(
            String fullName,
            String email,
            String phone,
            String department,
            String positionTitle,
            String accountType,
            String status,
            String accessExpiresAt
    ) {
    }

    public record InternalAccountUpdateRequest(
            String fullName,
            String email,
            String phone,
            String department,
            String positionTitle,
            String accountType,
            String status,
            String accessExpiresAt,
            long version
    ) {
    }

    public record InternalAccountResponse(
            UUID id,
            String fullName,
            String email,
            String phone,
            String department,
            String positionTitle,
            String accountType,
            String status,
            String accessExpiresAt,
            long version
    ) {
    }

    public record InternalAccountListResponse(List<InternalAccountResponse> items) {
    }

    public record AccountAccessUpdateRequest(
            List<String> roleCodes,
            List<String> permissionSetCodes,
            List<ResponsibilityScopeRequest> responsibilityScopes
    ) {
    }

    public record ResponsibilityScopeRequest(
            UUID regionId,
            UUID warehouseId,
            UUID pickupPointId,
            UUID catalogId,
            UUID productCategoryId,
            String departmentId,
            UUID partnerStructureSegmentId
    ) {
    }

    public record EffectivePermissionPreviewResponse(
            List<String> effectivePermissions,
            List<String> conflicts,
            boolean requiredMfa,
            List<String> affectedModules,
            Map<String, Object> auditPreview,
            boolean auditRecorded,
            String messageCode
    ) {
    }

    public record RoleCatalogResponse(List<RoleResponse> roles, List<PermissionSetResponse> permissionSets) {
    }

    public record RoleResponse(String code, String name, String moduleAccess, boolean active, String riskLevel) {
    }

    public record PermissionSetResponse(String code, String name, List<String> permissions, String riskLevel, boolean elevatedRequired, boolean active) {
    }

    public record SecurityPolicyUpdateRequest(String policyType, String policyCode, Map<String, Object> settings, long version) {
    }

    public record SecurityPolicyResponse(String policyType, String policyCode, Map<String, Object> settings, long version, boolean auditRecorded, String messageCode) {
    }

    public record SecurityPolicyListResponse(List<SecurityPolicyResponse> items) {
    }

    public record ServiceAccountCreateRequest(
            String code,
            UUID ownerUserId,
            String integrationType,
            List<String> permissionScopes,
            List<String> allowedIpRanges,
            String expiresAt
    ) {
    }

    public record ServiceAccountResponse(
            UUID id,
            String code,
            UUID ownerUserId,
            String integrationType,
            String maskedSecretHint,
            String status,
            String expiresAt,
            String lastUsedAt
    ) {
    }

    public record ServiceAccountSecretResponse(
            UUID id,
            String code,
            UUID ownerUserId,
            String integrationType,
            String maskedSecretHint,
            String status,
            String expiresAt,
            String lastUsedAt,
            String oneTimeSecret,
            String messageCode
    ) {
    }

    public record ServiceAccountListResponse(List<ServiceAccountResponse> items) {
    }

    public record EmergencyDeactivationRequest(String targetType, String targetCode, String reasonCode) {
    }

    public record AuditEventListResponse(List<AuditEventResponse> items) {
    }

    public record AuditEventResponse(
            UUID eventId,
            UUID actorUserId,
            UUID targetUserId,
            UUID serviceAccountId,
            String actionCode,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String sourceIp,
            String correlationId,
            String occurredAt
    ) {
    }
}
