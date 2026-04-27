package com.bestorigin.monolith.adminidentity.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminIdentityDtos {

    private AdminIdentityDtos() {
    }

    public record AdminIdentityErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record SubjectSearchResponse(List<SubjectSearchItem> items, int page, int size, long total) {
    }

    public record SubjectSearchItem(UUID subjectId, String externalSubjectId, String subjectType, String displayName, String status, String maskedContact, String sponsorCode, String employeeRole, List<String> riskFlags) {
    }

    public record SubjectCard(UUID subjectId, String externalSubjectId, String subjectType, String displayName, String status, List<ProfileAttribute> profileAttributes, List<EligibilityRuleResponse> eligibilityRules, PartnerSummary partnerSummary, List<EmployeeBindingResponse> employeeBindings, List<AuditEventResponse> auditPreview, List<String> allowedActions, String messageCode) {
    }

    public record ProfileAttribute(String code, String maskedValue, boolean piiMasked, String sourceSystem) {
    }

    public record PartnerSummary(UUID partnerSubjectId, UUID sponsorSubjectId, String sponsorCode, String partnerLevel, String officeId, int downlineCount) {
    }

    public record ChangeStatusRequest(String newStatus, String reasonCode, String comment) {
    }

    public record StatusChangeResponse(UUID subjectId, String oldStatus, String newStatus, UUID auditEventId, String messageCode) {
    }

    public record EligibilityRuleRequest(String ruleType, String ruleValue, String effectiveFrom, String effectiveTo, String reasonCode) {
    }

    public record EligibilityRulesUpdateRequest(List<EligibilityRuleRequest> rules, String reasonCode) {
    }

    public record EligibilityRuleResponse(UUID ruleId, String ruleType, String ruleValue, String effectiveFrom, String effectiveTo, String reasonCode) {
    }

    public record EligibilityRulesResponse(UUID subjectId, List<EligibilityRuleResponse> rules, String messageCode) {
    }

    public record SponsorRelationshipChangeRequest(UUID newSponsorSubjectId, String effectiveFrom, String reasonCode, String comment) {
    }

    public record SponsorRelationshipResponse(UUID relationshipId, UUID partnerSubjectId, UUID oldSponsorSubjectId, UUID newSponsorSubjectId, Map<String, Object> impactPreview, String messageCode, String correlationId) {
    }

    public record EmployeeBindingRequest(String roleCode, String operationalScope, String regionalScope, String accessStatus) {
    }

    public record EmployeeBindingsUpdateRequest(List<EmployeeBindingRequest> bindings, String reasonCode) {
    }

    public record EmployeeBindingResponse(String roleCode, String operationalScope, String regionalScope, String accessStatus, String conflictState) {
    }

    public record EmployeeBindingsResponse(UUID employeeSubjectId, List<EmployeeBindingResponse> bindings, String conflictState, String messageCode) {
    }

    public record ImpersonationPolicy(UUID policyId, String policyCode, String actorRoleCode, String targetSubjectType, List<String> allowedActions, List<String> forbiddenActions, int maxDurationMinutes, boolean approvalRequired, String status, String messageCode) {
    }

    public record ImpersonationPolicySaveRequest(String policyCode, String actorRoleCode, String targetSubjectType, List<String> allowedActions, List<String> forbiddenActions, Integer maxDurationMinutes, Boolean approvalRequired, String reasonCode) {
    }

    public record StartImpersonationRequest(UUID targetSubjectId, String reasonCode, Integer requestedDurationMinutes) {
    }

    public record ReasonedActionRequest(String reasonCode, String comment) {
    }

    public record ImpersonationSession(UUID sessionId, UUID policyId, UUID actorSubjectId, UUID targetSubjectId, String reasonCode, String status, String startedAt, String expiresAt, String finishedAt, String messageCode) {
    }

    public record AuditEventPage(List<AuditEventResponse> items, int page, int size, long total) {
    }

    public record AuditEventResponse(UUID auditEventId, UUID subjectId, UUID actorSubjectId, String actionCode, String reasonCode, String correlationId, String occurredAt) {
    }
}
