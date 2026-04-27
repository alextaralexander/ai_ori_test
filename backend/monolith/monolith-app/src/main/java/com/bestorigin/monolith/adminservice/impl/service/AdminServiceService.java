package com.bestorigin.monolith.adminservice.impl.service;

import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AssignmentRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AuditEventPage;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AuditExportRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AuditExportResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateDecisionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateMessageRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateRefundActionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateReplacementActionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateServiceCaseRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.DecisionResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.RefundActionResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ReplacementActionResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceCaseDetails;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceCasePage;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceMessageResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceQueueResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.SlaBoardResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.StatusTransitionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.UpsertQueueRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.WmsEventRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.WmsEventResponse;
import java.util.List;
import java.util.UUID;

public interface AdminServiceService {

    ServiceCasePage searchCases(String token, String search, String caseStatus, String slaStatus, String claimType, UUID queueId, UUID warehouseId, int page, int size);

    ServiceCaseDetails createCase(String token, String idempotencyKey, CreateServiceCaseRequest request);

    ServiceCaseDetails getCase(String token, UUID serviceCaseId);

    ServiceCaseDetails assignCase(String token, UUID serviceCaseId, String idempotencyKey, AssignmentRequest request);

    ServiceCaseDetails transitionStatus(String token, UUID serviceCaseId, String idempotencyKey, StatusTransitionRequest request);

    ServiceMessageResponse addMessage(String token, UUID serviceCaseId, String idempotencyKey, CreateMessageRequest request);

    List<ServiceQueueResponse> queues(String token);

    ServiceQueueResponse upsertQueue(String token, String idempotencyKey, UpsertQueueRequest request);

    DecisionResponse createDecision(String token, UUID serviceCaseId, String idempotencyKey, CreateDecisionRequest request);

    RefundActionResponse createRefundAction(String token, UUID decisionId, String idempotencyKey, CreateRefundActionRequest request);

    ReplacementActionResponse createReplacementAction(String token, UUID decisionId, String idempotencyKey, CreateReplacementActionRequest request);

    WmsEventResponse ingestWmsEvent(String token, String idempotencyKey, WmsEventRequest request);

    SlaBoardResponse slaBoard(String token);

    AuditEventPage audit(String token, String entityType, String entityId, UUID actorUserId, String correlationId, int page, int size);

    AuditExportResponse exportAudit(String token, AuditExportRequest request);
}
