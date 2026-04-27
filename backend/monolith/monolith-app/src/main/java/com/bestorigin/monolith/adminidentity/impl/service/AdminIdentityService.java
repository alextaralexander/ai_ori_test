package com.bestorigin.monolith.adminidentity.impl.service;

import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.AuditEventPage;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ChangeStatusRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EligibilityRulesResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EligibilityRulesUpdateRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EmployeeBindingsResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EmployeeBindingsUpdateRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationPolicy;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationPolicySaveRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationSession;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ReasonedActionRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SponsorRelationshipChangeRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SponsorRelationshipResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.StartImpersonationRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.StatusChangeResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SubjectCard;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SubjectSearchResponse;
import java.util.List;
import java.util.UUID;

public interface AdminIdentityService {

    SubjectSearchResponse searchSubjects(String token, String query, String subjectType, String status, String sponsorCode, String officeId, String employeeRole, int page, int size);

    SubjectCard getSubjectCard(String token, UUID subjectId);

    StatusChangeResponse changeStatus(String token, UUID subjectId, String idempotencyKey, ChangeStatusRequest request);

    EligibilityRulesResponse updateEligibilityRules(String token, UUID subjectId, String idempotencyKey, EligibilityRulesUpdateRequest request);

    SponsorRelationshipResponse changeSponsor(String token, UUID partnerSubjectId, String idempotencyKey, SponsorRelationshipChangeRequest request);

    EmployeeBindingsResponse updateEmployeeBindings(String token, UUID employeeSubjectId, String idempotencyKey, EmployeeBindingsUpdateRequest request);

    List<ImpersonationPolicy> policies(String token);

    ImpersonationPolicy savePolicy(String token, String idempotencyKey, ImpersonationPolicySaveRequest request);

    ImpersonationSession startSession(String token, String idempotencyKey, StartImpersonationRequest request);

    ImpersonationSession finishSession(String token, UUID sessionId, ReasonedActionRequest request);

    AuditEventPage auditEvents(String token, UUID subjectId, UUID actorSubjectId, String actionCode, String reasonCode, int page, int size);
}
