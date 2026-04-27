package com.bestorigin.monolith.adminrbac.impl.service;

import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.AccountAccessUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.AuditEventListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.EffectivePermissionPreviewResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.EmergencyDeactivationRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountCreateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.RoleCatalogResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountCreateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountSecretResponse;
import java.util.UUID;

public interface AdminRbacService {

    InternalAccountListResponse searchAccounts(String token, String query, String status, String department);

    InternalAccountResponse createAccount(String token, String elevatedSessionId, InternalAccountCreateRequest request);

    InternalAccountResponse getAccount(String token, UUID accountId);

    InternalAccountResponse updateAccount(String token, UUID accountId, InternalAccountUpdateRequest request);

    EffectivePermissionPreviewResponse updateAccess(String token, String elevatedSessionId, UUID accountId, AccountAccessUpdateRequest request);

    EffectivePermissionPreviewResponse previewAccess(String token, UUID accountId, AccountAccessUpdateRequest request);

    void blockAccount(String token, String elevatedSessionId, UUID accountId);

    void deactivateAccount(String token, String elevatedSessionId, UUID accountId);

    RoleCatalogResponse roleCatalog(String token);

    SecurityPolicyListResponse securityPolicies(String token);

    SecurityPolicyResponse updateSecurityPolicy(String token, String elevatedSessionId, SecurityPolicyUpdateRequest request);

    ServiceAccountListResponse serviceAccounts(String token);

    ServiceAccountSecretResponse createServiceAccount(String token, String elevatedSessionId, ServiceAccountCreateRequest request);

    ServiceAccountSecretResponse rotateServiceAccount(String token, String elevatedSessionId, UUID serviceAccountId);

    void emergencyDeactivate(String token, String elevatedSessionId, EmergencyDeactivationRequest request);

    AuditEventListResponse auditEvents(String token, UUID actorUserId, UUID targetUserId, String actionCode, String dateFrom, String dateTo, String correlationId);
}
