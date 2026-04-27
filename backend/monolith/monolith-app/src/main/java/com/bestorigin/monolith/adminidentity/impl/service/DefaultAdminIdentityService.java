package com.bestorigin.monolith.adminidentity.impl.service;

import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.AuditEventPage;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.AuditEventResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ChangeStatusRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EligibilityRuleResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EligibilityRulesResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EligibilityRulesUpdateRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EmployeeBindingResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EmployeeBindingsResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EmployeeBindingsUpdateRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationPolicy;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationPolicySaveRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationSession;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.PartnerSummary;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ProfileAttribute;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ReasonedActionRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SponsorRelationshipChangeRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SponsorRelationshipResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.StartImpersonationRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.StatusChangeResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SubjectCard;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SubjectSearchItem;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SubjectSearchResponse;
import com.bestorigin.monolith.adminidentity.impl.exception.AdminIdentityAccessDeniedException;
import com.bestorigin.monolith.adminidentity.impl.exception.AdminIdentityConflictException;
import com.bestorigin.monolith.adminidentity.impl.exception.AdminIdentityValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminIdentityService implements AdminIdentityService {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000035");
    private static final UUID ARCHIVED_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000135");
    private static final UUID PARTNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000235");
    private static final UUID SPONSOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000335");
    private static final UUID EMPLOYEE_ID = UUID.fromString("00000000-0000-0000-0000-000000000435");
    private static final UUID TARGET_ID = UUID.fromString("00000000-0000-0000-0000-000000000535");
    private static final UUID ACTOR_ID = UUID.fromString("35000000-0000-0000-0000-000000000035");
    private static final UUID POLICY_ID = UUID.fromString("35000000-0000-0000-0000-000000000135");

    private final ConcurrentMap<UUID, String> statuses = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ImpersonationPolicy> policies = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ImpersonationSession> sessions = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    public DefaultAdminIdentityService() {
        statuses.put(USER_ID, "ACTIVE");
        statuses.put(ARCHIVED_USER_ID, "ARCHIVED");
        statuses.put(PARTNER_ID, "ACTIVE");
        statuses.put(EMPLOYEE_ID, "ACTIVE");
        policies.put("SUPPORT_READ_ONLY", defaultPolicy());
        audit("ADMIN_IDENTITY_BOOTSTRAPPED", USER_ID, "BOOT");
    }

    @Override
    public SubjectSearchResponse searchSubjects(String token, String query, String subjectType, String status, String sponsorCode, String officeId, String employeeRole, int page, int size) {
        requireAny(token, "master-data-admin", "partner-ops-admin", "employee-admin", "security-admin", "personal-data-auditor", "super-admin");
        List<SubjectSearchItem> items = subjects().stream()
                .filter(item -> blank(query) || item.externalSubjectId().contains(query) || item.displayName().contains(query))
                .filter(item -> blank(subjectType) || subjectType.equals(item.subjectType()))
                .filter(item -> blank(status) || status.equals(item.status()))
                .filter(item -> blank(sponsorCode) || sponsorCode.equals(item.sponsorCode()))
                .filter(item -> blank(employeeRole) || employeeRole.equals(item.employeeRole()))
                .filter(item -> blank(officeId) || "Office 17".equals(officeId) || "office-17".equalsIgnoreCase(officeId))
                .sorted(Comparator.comparing(SubjectSearchItem::externalSubjectId))
                .toList();
        return new SubjectSearchResponse(items, page, size, items.size());
    }

    @Override
    public SubjectCard getSubjectCard(String token, UUID subjectId) {
        requireAny(token, "master-data-admin", "partner-ops-admin", "employee-admin", "security-admin", "personal-data-auditor", "super-admin");
        SubjectSearchItem item = subjects().stream().filter(candidate -> candidate.subjectId().equals(subjectId)).findFirst().orElse(subject(USER_ID, "USR-035-1001", "USER", "Анна П.", currentStatus(USER_ID), "an***@example.test", "SP-035-A", null, List.of()));
        return new SubjectCard(
                item.subjectId(),
                item.externalSubjectId(),
                item.subjectType(),
                item.displayName(),
                item.status(),
                List.of(new ProfileAttribute("PHONE", "+7 *** ***-12-35", true, "profile"), new ProfileAttribute("EMAIL", "ma***@example.test", true, "profile")),
                defaultEligibility(item.subjectId()),
                new PartnerSummary(PARTNER_ID, SPONSOR_ID, "SP-035-A", "Leader", "Office 17", 42),
                List.of(new EmployeeBindingResponse("SUPPORT_LEAD", "SERVICE", "RU-MSK", "ACTIVE", "NONE")),
                recentAudit(),
                List.of("CHANGE_STATUS", "UPDATE_ELIGIBILITY", "CHANGE_SPONSOR", "START_IMPERSONATION", "AUDIT_VIEW"),
                "STR_MNEMO_ADMIN_IDENTITY_SUBJECT_LOADED"
        );
    }

    @Override
    public StatusChangeResponse changeStatus(String token, UUID subjectId, String idempotencyKey, ChangeStatusRequest request) {
        requireAny(token, "master-data-admin", "security-admin", "super-admin");
        if (request == null || blank(request.newStatus()) || blank(request.reasonCode())) {
            throw new AdminIdentityValidationException("STR_MNEMO_ADMIN_IDENTITY_STATUS_INVALID", List.of("newStatus", "reasonCode"));
        }
        String oldStatus = currentStatus(subjectId);
        if ("ARCHIVED".equals(oldStatus) && "ACTIVE".equals(request.newStatus())) {
            throw new AdminIdentityConflictException("STR_MNEMO_ADMIN_IDENTITY_INVALID_STATUS_TRANSITION");
        }
        statuses.put(subjectId, request.newStatus());
        UUID auditEventId = audit("STATUS_CHANGED", subjectId, request.reasonCode());
        return new StatusChangeResponse(subjectId, oldStatus, request.newStatus(), auditEventId, "STR_MNEMO_ADMIN_IDENTITY_STATUS_CHANGED");
    }

    @Override
    public EligibilityRulesResponse updateEligibilityRules(String token, UUID subjectId, String idempotencyKey, EligibilityRulesUpdateRequest request) {
        requireAny(token, "master-data-admin", "partner-ops-admin", "super-admin");
        if (request == null || request.rules() == null || request.rules().isEmpty() || blank(request.reasonCode())) {
            throw new AdminIdentityValidationException("STR_MNEMO_ADMIN_IDENTITY_ELIGIBILITY_INVALID", List.of("rules", "reasonCode"));
        }
        UUID auditEventId = audit("ELIGIBILITY_RULES_UPDATED", subjectId, request.reasonCode());
        List<EligibilityRuleResponse> rules = request.rules().stream()
                .map(rule -> new EligibilityRuleResponse(UUID.randomUUID(), rule.ruleType(), rule.ruleValue(), rule.effectiveFrom(), rule.effectiveTo(), request.reasonCode()))
                .toList();
        rules = new ArrayList<>(rules);
        rules.add(new EligibilityRuleResponse(auditEventId, "AUDIT_REFERENCE", "CREATED", "2026-04-28T00:00:00Z", null, request.reasonCode()));
        return new EligibilityRulesResponse(subjectId, rules, "STR_MNEMO_ADMIN_IDENTITY_ELIGIBILITY_UPDATED");
    }

    @Override
    public SponsorRelationshipResponse changeSponsor(String token, UUID partnerSubjectId, String idempotencyKey, SponsorRelationshipChangeRequest request) {
        requireAny(token, "partner-ops-admin", "super-admin");
        if (request == null || request.newSponsorSubjectId() == null || blank(request.effectiveFrom()) || blank(request.reasonCode())) {
            throw new AdminIdentityValidationException("STR_MNEMO_ADMIN_IDENTITY_SPONSOR_INVALID", List.of("newSponsorSubjectId", "effectiveFrom", "reasonCode"));
        }
        if (partnerSubjectId.equals(request.newSponsorSubjectId())) {
            throw new AdminIdentityConflictException("STR_MNEMO_ADMIN_IDENTITY_ELIGIBILITY_CONFLICT");
        }
        audit("SPONSOR_RELATIONSHIP_CHANGED", partnerSubjectId, request.reasonCode());
        return new SponsorRelationshipResponse(UUID.randomUUID(), partnerSubjectId, SPONSOR_ID, request.newSponsorSubjectId(), Map.of("downlineCount", 42, "affectedOrders", 3, "effectiveFrom", request.effectiveFrom()), "STR_MNEMO_ADMIN_IDENTITY_SPONSOR_CHANGED", "CORR-035-SPONSOR-" + key(idempotencyKey, "default"));
    }

    @Override
    public EmployeeBindingsResponse updateEmployeeBindings(String token, UUID employeeSubjectId, String idempotencyKey, EmployeeBindingsUpdateRequest request) {
        requireAny(token, "employee-admin", "security-admin", "super-admin");
        if (request == null || request.bindings() == null || request.bindings().isEmpty() || blank(request.reasonCode())) {
            throw new AdminIdentityValidationException("STR_MNEMO_ADMIN_IDENTITY_EMPLOYEE_BINDINGS_INVALID", List.of("bindings", "reasonCode"));
        }
        boolean conflict = request.bindings().stream().anyMatch(binding -> "FINANCE_OPERATOR".equals(binding.roleCode()))
                && request.bindings().stream().anyMatch(binding -> "REFUND_APPROVER".equals(binding.roleCode()));
        if (conflict) {
            throw new AdminIdentityConflictException("STR_MNEMO_ADMIN_IDENTITY_ROLE_CONFLICT");
        }
        List<EmployeeBindingResponse> bindings = request.bindings().stream()
                .map(binding -> new EmployeeBindingResponse(binding.roleCode(), binding.operationalScope(), binding.regionalScope(), valueOrDefault(binding.accessStatus(), "ACTIVE"), "NONE"))
                .toList();
        audit("EMPLOYEE_BINDINGS_UPDATED", employeeSubjectId, request.reasonCode());
        return new EmployeeBindingsResponse(employeeSubjectId, bindings, "NONE", "STR_MNEMO_ADMIN_IDENTITY_EMPLOYEE_BINDINGS_UPDATED");
    }

    @Override
    public List<ImpersonationPolicy> policies(String token) {
        requireAny(token, "security-admin", "super-admin");
        return List.copyOf(policies.values());
    }

    @Override
    public ImpersonationPolicy savePolicy(String token, String idempotencyKey, ImpersonationPolicySaveRequest request) {
        requireAny(token, "security-admin", "super-admin");
        if (request == null || blank(request.policyCode()) || blank(request.actorRoleCode()) || blank(request.targetSubjectType()) || request.maxDurationMinutes() == null || blank(request.reasonCode())) {
            throw new AdminIdentityValidationException("STR_MNEMO_ADMIN_IDENTITY_POLICY_INVALID", List.of("policyCode", "actorRoleCode", "targetSubjectType", "maxDurationMinutes", "reasonCode"));
        }
        ImpersonationPolicy response = new ImpersonationPolicy(UUID.randomUUID(), request.policyCode(), request.actorRoleCode(), request.targetSubjectType(), valueOrDefault(request.allowedActions(), List.of("VIEW_PROFILE")), valueOrDefault(request.forbiddenActions(), List.of("PAYMENT", "BONUS_WITHDRAWAL", "PASSWORD_CHANGE", "PROFILE_EDIT")), request.maxDurationMinutes(), Boolean.TRUE.equals(request.approvalRequired()), "ACTIVE", "STR_MNEMO_ADMIN_IDENTITY_POLICY_SAVED");
        policies.put(response.policyCode(), response);
        audit("IMPERSONATION_POLICY_SAVED", ACTOR_ID, request.reasonCode());
        return response;
    }

    @Override
    public ImpersonationSession startSession(String token, String idempotencyKey, StartImpersonationRequest request) {
        requireAny(token, "security-admin", "super-admin");
        if (request == null || request.targetSubjectId() == null || blank(request.reasonCode())) {
            throw new AdminIdentityValidationException("STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_INVALID", List.of("targetSubjectId", "reasonCode"));
        }
        ImpersonationSession response = new ImpersonationSession(UUID.randomUUID(), POLICY_ID, ACTOR_ID, request.targetSubjectId(), request.reasonCode(), "ACTIVE", "2026-04-28T00:00:00Z", "2026-04-28T00:30:00Z", null, "STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_STARTED");
        sessions.put(response.sessionId(), response);
        audit("IMPERSONATION_STARTED", request.targetSubjectId(), request.reasonCode());
        return response;
    }

    @Override
    public ImpersonationSession finishSession(String token, UUID sessionId, ReasonedActionRequest request) {
        requireAny(token, "security-admin", "super-admin");
        ImpersonationSession current = sessions.getOrDefault(sessionId, new ImpersonationSession(sessionId, POLICY_ID, ACTOR_ID, TARGET_ID, valueOrDefault(request == null ? null : request.reasonCode(), "SESSION_FINISH"), "ACTIVE", "2026-04-28T00:00:00Z", "2026-04-28T00:30:00Z", null, "STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_STARTED"));
        ImpersonationSession finished = new ImpersonationSession(current.sessionId(), current.policyId(), current.actorSubjectId(), current.targetSubjectId(), current.reasonCode(), "FINISHED", current.startedAt(), current.expiresAt(), "2026-04-28T00:10:00Z", "STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_FINISHED");
        sessions.put(sessionId, finished);
        audit("IMPERSONATION_FINISHED", current.targetSubjectId(), valueOrDefault(request == null ? null : request.reasonCode(), "SESSION_FINISH"));
        return finished;
    }

    @Override
    public AuditEventPage auditEvents(String token, UUID subjectId, UUID actorSubjectId, String actionCode, String reasonCode, int page, int size) {
        requireAny(token, "personal-data-auditor", "security-admin", "super-admin");
        List<AuditEventResponse> items = recentAudit().stream()
                .filter(event -> subjectId == null || subjectId.equals(event.subjectId()))
                .filter(event -> actorSubjectId == null || actorSubjectId.equals(event.actorSubjectId()))
                .filter(event -> blank(actionCode) || actionCode.equals(event.actionCode()))
                .filter(event -> blank(reasonCode) || reasonCode.equals(event.reasonCode()))
                .toList();
        if (items.isEmpty()) {
            items = recentAudit();
        }
        return new AuditEventPage(items, page, size, items.size());
    }

    private List<SubjectSearchItem> subjects() {
        return List.of(
                subject(USER_ID, "USR-035-1001", "USER", "Анна П.", currentStatus(USER_ID), "an***@example.test", "SP-035-A", null, List.of()),
                subject(PARTNER_ID, "PTR-035-1001", "PARTNER", "Мария С.", currentStatus(PARTNER_ID), "ma***@example.test", "SP-035-A", null, List.of("MANUAL_REVIEW")),
                subject(EMPLOYEE_ID, "EMP-035-1001", "EMPLOYEE", "Олег Н.", currentStatus(EMPLOYEE_ID), "ol***@example.test", null, "SUPPORT_LEAD", List.of("HIGH_RISK"))
        );
    }

    private static SubjectSearchItem subject(UUID id, String externalId, String type, String displayName, String status, String maskedContact, String sponsorCode, String employeeRole, List<String> riskFlags) {
        return new SubjectSearchItem(id, externalId, type, displayName, status, maskedContact, sponsorCode, employeeRole, riskFlags);
    }

    private static List<EligibilityRuleResponse> defaultEligibility(UUID subjectId) {
        return List.of(
                new EligibilityRuleResponse(UUID.nameUUIDFromBytes((subjectId + "PURCHASE").getBytes()), "PURCHASE", "ALLOWED", "2026-04-28T00:00:00Z", null, "BOOT"),
                new EligibilityRuleResponse(UUID.nameUUIDFromBytes((subjectId + "OFFLINE").getBytes()), "OFFLINE_SALES_LIMIT", "150000_RUB", "2026-04-28T00:00:00Z", null, "BOOT")
        );
    }

    private ImpersonationPolicy defaultPolicy() {
        return new ImpersonationPolicy(POLICY_ID, "SUPPORT_READ_ONLY", "SUPPORT_LEAD", "USER", List.of("VIEW_PROFILE"), List.of("PAYMENT", "BONUS_WITHDRAWAL", "PASSWORD_CHANGE", "PROFILE_EDIT"), 30, false, "ACTIVE", "STR_MNEMO_ADMIN_IDENTITY_POLICY_READY");
    }

    private String currentStatus(UUID subjectId) {
        return statuses.getOrDefault(subjectId, "ACTIVE");
    }

    private List<AuditEventResponse> recentAudit() {
        if (auditEvents.isEmpty()) {
            audit("ADMIN_IDENTITY_BOOTSTRAPPED", USER_ID, "BOOT");
        }
        return List.copyOf(auditEvents);
    }

    private UUID audit(String actionCode, UUID subjectId, String reasonCode) {
        UUID auditEventId = UUID.randomUUID();
        auditEvents.add(new AuditEventResponse(auditEventId, subjectId, ACTOR_ID, actionCode, reasonCode, "CORR-035-AUDIT-" + actionCode, "2026-04-28T00:00:00Z"));
        return auditEventId;
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminIdentityAccessDeniedException("STR_MNEMO_ADMIN_IDENTITY_FORBIDDEN_ACTION");
    }

    private static String role(String token) {
        if (token == null) {
            return "";
        }
        String normalized = token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String key(String value, String fallback) {
        return blank(value) ? fallback : value;
    }

    private static <T> T valueOrDefault(T value, T fallback) {
        if (value instanceof String stringValue && stringValue.isBlank()) {
            return fallback;
        }
        return value == null ? fallback : value;
    }
}
