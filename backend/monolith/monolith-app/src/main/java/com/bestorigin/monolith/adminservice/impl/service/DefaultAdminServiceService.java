package com.bestorigin.monolith.adminservice.impl.service;

import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AssignmentRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AttachmentResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AuditEventPage;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AuditEventResponse;
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
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceCaseSummary;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceMessageResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceQueueResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.SlaBoardResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.StatusTransitionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.UpsertQueueRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.WmsEventRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.WmsEventResponse;
import com.bestorigin.monolith.adminservice.impl.exception.AdminServiceAccessDeniedException;
import com.bestorigin.monolith.adminservice.impl.exception.AdminServiceConflictException;
import com.bestorigin.monolith.adminservice.impl.exception.AdminServiceValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminServiceService implements AdminServiceService {

    private static final UUID CASE_ID = UUID.fromString("00000000-0000-0000-0000-000000000034");
    private static final UUID REFUND_CASE_ID = UUID.fromString("00000000-0000-0000-0000-000000000134");
    private static final UUID REPLACEMENT_CASE_ID = UUID.fromString("00000000-0000-0000-0000-000000000334");
    private static final UUID QUEUE_ID = UUID.fromString("00000000-0000-0000-0000-000000003400");
    private static final UUID WAREHOUSE_ID = UUID.fromString("00000000-0000-0000-0000-000000003401");
    private static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000003402");
    private static final UUID ACTOR_USER_ID = UUID.fromString("34000000-0000-0000-0000-000000000034");

    private final ConcurrentMap<UUID, ServiceCaseSummary> cases = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ServiceCaseDetails> createdCases = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, DecisionResponse> decisions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RefundActionResponse> refunds = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReplacementActionResponse> replacements = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, WmsEventResponse> wmsEvents = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ServiceQueueResponse> queues = new ConcurrentHashMap<>();
    private final List<ServiceMessageResponse> messages = new ArrayList<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    public DefaultAdminServiceService() {
        queues.put("DAMAGED_ITEM", new ServiceQueueResponse(QUEUE_ID, "DAMAGED_ITEM", "Damaged item queue", true, "service-claims", "RU-MSK", WAREHOUSE_ID, "STR_MNEMO_ADMIN_SERVICE_QUEUE_READY"));
        cases.put(CASE_ID, defaultCase(CASE_ID, "CLM-034-1001", "NEW", "AT_RISK", null));
        cases.put(REFUND_CASE_ID, defaultCase(REFUND_CASE_ID, "CLM-034-1002", "IN_PROGRESS", "ON_TRACK", ACTOR_USER_ID));
        cases.put(REPLACEMENT_CASE_ID, defaultCase(REPLACEMENT_CASE_ID, "CLM-034-1005", "IN_PROGRESS", "ON_TRACK", ACTOR_USER_ID));
        cases.put(UUID.fromString("00000000-0000-0000-0000-000000000734"), defaultCase(UUID.fromString("00000000-0000-0000-0000-000000000734"), "CLM-034-1007", "ESCALATED", "BREACHED", ACTOR_USER_ID));
        messages.add(new ServiceMessageResponse(UUID.randomUUID(), CASE_ID, "SYSTEM_EVENT", "INTERNAL", ACTOR_USER_ID, "STR_MNEMO_ADMIN_SERVICE_CASE_ROUTED", "2026-04-28T00:00:00Z", "STR_MNEMO_ADMIN_SERVICE_CASE_ROUTED"));
        audit("ADMIN_SERVICE_BOOTSTRAPPED", "SERVICE_CASE", "CLM-034-1001", "BOOT");
    }

    @Override
    public ServiceCasePage searchCases(String token, String search, String caseStatus, String slaStatus, String claimType, UUID queueId, UUID warehouseId, int page, int size) {
        requireAny(token, "claim-operator", "service-supervisor", "audit-admin", "business-admin", "super-admin");
        List<ServiceCaseSummary> items = cases.values().stream()
                .filter(item -> blank(search) || item.claimNumber().contains(search) || item.customerId().contains(search) || item.partnerId().contains(search))
                .filter(item -> blank(caseStatus) || caseStatus.equals(item.caseStatus()))
                .filter(item -> blank(slaStatus) || slaStatus.equals(item.slaStatus()))
                .filter(item -> blank(claimType) || claimType.equals(item.claimType()))
                .filter(item -> queueId == null || queueId.equals(item.queueId()))
                .filter(item -> warehouseId == null || warehouseId.equals(item.warehouseId()))
                .sorted(Comparator.comparing(ServiceCaseSummary::claimNumber))
                .toList();
        return new ServiceCasePage(items, page, size, items.size());
    }

    @Override
    public ServiceCaseDetails createCase(String token, String idempotencyKey, CreateServiceCaseRequest request) {
        requireAny(token, "claim-operator", "service-supervisor", "business-admin", "super-admin");
        if (request == null || blank(request.sourceClaimId()) || request.orderId() == null || blank(request.claimType()) || blank(request.reasonCode())) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_CASE_INVALID", List.of("sourceClaimId", "orderId", "claimType", "reasonCode"));
        }
        String key = key(idempotencyKey, "case-" + request.sourceClaimId());
        return createdCases.computeIfAbsent(key, ignored -> {
            UUID id = UUID.randomUUID();
            ServiceCaseSummary summary = new ServiceCaseSummary(id, "CLM-034-" + Math.abs(key.hashCode()), request.sourceClaimId(), request.orderId(), valueOrDefault(request.customerId(), "customer-034"), valueOrDefault(request.partnerId(), "partner-034"), valueOrDefault(request.warehouseId(), WAREHOUSE_ID), QUEUE_ID, null, request.claimType(), valueOrDefault(request.priority(), "NORMAL"), "ROUTED", "ON_TRACK", "2026-04-28T04:00:00Z", "2026-04-30T00:00:00Z", "STR_MNEMO_ADMIN_SERVICE_CASE_CREATED");
            cases.put(id, summary);
            audit("ADMIN_SERVICE_CASE_CREATED", "SERVICE_CASE", summary.claimNumber(), request.reasonCode());
            return details(summary);
        });
    }

    @Override
    public ServiceCaseDetails getCase(String token, UUID serviceCaseId) {
        requireAny(token, "claim-operator", "service-supervisor", "audit-admin", "business-admin", "super-admin");
        return details(cases.getOrDefault(serviceCaseId, defaultCase(serviceCaseId, "CLM-034-1001", "NEW", "AT_RISK", null)));
    }

    @Override
    public ServiceCaseDetails assignCase(String token, UUID serviceCaseId, String idempotencyKey, AssignmentRequest request) {
        requireAny(token, "claim-operator", "service-supervisor", "business-admin", "super-admin");
        if (request == null || blank(request.assignmentAction()) || blank(request.reasonCode())) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_ASSIGNMENT_INVALID", List.of("assignmentAction", "reasonCode"));
        }
        ServiceCaseSummary current = cases.getOrDefault(serviceCaseId, defaultCase(serviceCaseId, "CLM-034-1001", "NEW", "AT_RISK", null));
        UUID owner = request.ownerUserId() == null ? ACTOR_USER_ID : request.ownerUserId();
        ServiceCaseSummary updated = copy(current, owner, "IN_PROGRESS", current.slaStatus(), "STR_MNEMO_ADMIN_SERVICE_CASE_ASSIGNED");
        persistMutableCase(serviceCaseId, updated);
        audit("ADMIN_SERVICE_CASE_ASSIGNED", "SERVICE_CASE", updated.claimNumber(), request.reasonCode());
        return details(updated);
    }

    @Override
    public ServiceCaseDetails transitionStatus(String token, UUID serviceCaseId, String idempotencyKey, StatusTransitionRequest request) {
        requireAny(token, "claim-operator", "service-supervisor", "business-admin", "super-admin");
        if (request == null || blank(request.targetStatus()) || blank(request.reasonCode())) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_STATUS_TRANSITION_INVALID", List.of("targetStatus", "reasonCode"));
        }
        ServiceCaseSummary current = cases.getOrDefault(serviceCaseId, defaultCase(serviceCaseId, "CLM-034-1001", "NEW", "AT_RISK", null));
        ServiceCaseSummary updated = copy(current, current.ownerUserId(), request.targetStatus(), slaForStatus(request.targetStatus(), current.slaStatus()), "STR_MNEMO_ADMIN_SERVICE_STATUS_UPDATED");
        cases.put(serviceCaseId, updated);
        audit("ADMIN_SERVICE_STATUS_UPDATED", "SERVICE_CASE", updated.claimNumber(), request.reasonCode());
        return details(updated);
    }

    @Override
    public ServiceMessageResponse addMessage(String token, UUID serviceCaseId, String idempotencyKey, CreateMessageRequest request) {
        requireAny(token, "claim-operator", "service-supervisor", "business-admin", "super-admin");
        if (request == null || blank(request.messageType()) || blank(request.visibility()) || blank(request.reasonCode())) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_MESSAGE_INVALID", List.of("messageType", "visibility", "reasonCode"));
        }
        ServiceMessageResponse response = new ServiceMessageResponse(UUID.randomUUID(), serviceCaseId, request.messageType(), request.visibility(), ACTOR_USER_ID, valueOrDefault(request.customerVisibleMessageCode(), "STR_MNEMO_ADMIN_SERVICE_MESSAGE_ADDED"), "2026-04-28T00:00:00Z", "STR_MNEMO_ADMIN_SERVICE_REQUEST_SENT");
        messages.add(response);
        ServiceCaseSummary current = cases.getOrDefault(serviceCaseId, defaultCase(serviceCaseId, "CLM-034-1001", "NEW", "AT_RISK", null));
        persistMutableCase(serviceCaseId, copy(current, current.ownerUserId(), "WAITING_CUSTOMER", "PAUSED", "STR_MNEMO_ADMIN_SERVICE_REQUEST_SENT"));
        audit("ADMIN_SERVICE_MESSAGE_ADDED", "SERVICE_CASE", serviceCaseId.toString(), request.reasonCode());
        return response;
    }

    @Override
    public List<ServiceQueueResponse> queues(String token) {
        requireAny(token, "claim-operator", "service-supervisor", "audit-admin", "business-admin", "super-admin");
        return List.copyOf(queues.values());
    }

    @Override
    public ServiceQueueResponse upsertQueue(String token, String idempotencyKey, UpsertQueueRequest request) {
        requireAny(token, "service-supervisor", "business-admin", "super-admin");
        if (request == null || blank(request.queueCode()) || blank(request.queueName())) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_QUEUE_INVALID", List.of("queueCode", "queueName"));
        }
        ServiceQueueResponse response = new ServiceQueueResponse(UUID.randomUUID(), request.queueCode(), request.queueName(), request.active(), request.defaultOwnerGroup(), request.regionCode(), request.warehouseId(), "STR_MNEMO_ADMIN_SERVICE_QUEUE_SAVED");
        queues.put(request.queueCode(), response);
        audit("ADMIN_SERVICE_QUEUE_SAVED", "SERVICE_QUEUE", request.queueCode(), key(idempotencyKey, "QUEUE"));
        return response;
    }

    @Override
    public DecisionResponse createDecision(String token, UUID serviceCaseId, String idempotencyKey, CreateDecisionRequest request) {
        requireAny(token, "claim-operator", "service-supervisor", "business-admin", "super-admin");
        if (request == null || blank(request.decisionType()) || blank(request.reasonCode())) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_DECISION_INVALID", List.of("decisionType", "reasonCode"));
        }
        boolean supervisor = "service-supervisor".equals(role(token)) || "super-admin".equals(role(token));
        DecisionResponse response = new DecisionResponse(UUID.randomUUID(), serviceCaseId, request.decisionType(), "APPROVED", request.reasonCode(), supervisor, valueOrDefault(request.customerMessageCode(), "STR_MNEMO_ADMIN_SERVICE_DECISION_SAVED"), "CORR-034-DECISION-" + key(idempotencyKey, "decision"), "STR_MNEMO_ADMIN_SERVICE_DECISION_SAVED");
        decisions.put(response.decisionId(), response);
        audit(supervisor ? "ADMIN_SERVICE_SUPERVISOR_OVERRIDE" : "ADMIN_SERVICE_DECISION_SAVED", "SERVICE_CASE", serviceCaseId.toString(), request.reasonCode());
        return response;
    }

    @Override
    public RefundActionResponse createRefundAction(String token, UUID decisionId, String idempotencyKey, CreateRefundActionRequest request) {
        requireAny(token, "claim-operator", "service-supervisor", "business-admin", "super-admin");
        if (request == null || request.amount() == null || request.amount() <= 0 || blank(request.reasonCode())) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_REFUND_INVALID", List.of("amount", "reasonCode"));
        }
        if (request.amount() > 12000) {
            throw new AdminServiceConflictException("STR_MNEMO_ADMIN_SERVICE_REFUND_AMOUNT_EXCEEDED");
        }
        DecisionResponse decision = decisions.getOrDefault(decisionId, new DecisionResponse(decisionId, REFUND_CASE_ID, "APPROVE_REFUND", "APPROVED", request.reasonCode(), false, "STR_MNEMO_ADMIN_SERVICE_REFUND_APPROVED", "CORR-034-DECISION-default", "STR_MNEMO_ADMIN_SERVICE_DECISION_SAVED"));
        String key = key(idempotencyKey, "refund-" + decisionId);
        return refunds.computeIfAbsent(key, ignored -> {
            RefundActionResponse response = new RefundActionResponse(UUID.randomUUID(), decision.serviceCaseId(), ORDER_ID, request.amount(), valueOrDefault(request.currencyCode(), "RUB"), "REQUESTED", "EXT-REF-034-1", "STR_MNEMO_ADMIN_SERVICE_REFUND_REQUESTED", "CORR-034-REFUND-" + key);
            audit("ADMIN_SERVICE_REFUND_REQUESTED", "SERVICE_REFUND", response.refundActionId().toString(), request.reasonCode());
            return response;
        });
    }

    @Override
    public ReplacementActionResponse createReplacementAction(String token, UUID decisionId, String idempotencyKey, CreateReplacementActionRequest request) {
        requireAny(token, "claim-operator", "service-supervisor", "business-admin", "super-admin");
        if (request == null || blank(request.sku()) || request.quantity() == null || request.quantity() <= 0 || request.warehouseId() == null) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_INVALID", List.of("sku", "quantity", "warehouseId"));
        }
        DecisionResponse decision = decisions.getOrDefault(decisionId, new DecisionResponse(decisionId, REPLACEMENT_CASE_ID, "APPROVE_REPLACEMENT", "APPROVED", valueOrDefault(request.reasonCode(), "REPLACEMENT"), false, "STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_APPROVED", "CORR-034-DECISION-default", "STR_MNEMO_ADMIN_SERVICE_DECISION_SAVED"));
        String key = key(idempotencyKey, "replacement-" + decisionId);
        return replacements.computeIfAbsent(key, ignored -> {
            ReplacementActionResponse response = new ReplacementActionResponse(UUID.randomUUID(), decision.serviceCaseId(), ORDER_ID, request.sku(), request.quantity(), request.warehouseId(), "REQUESTED", "SHIP-034-REPLACE-1", "STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_REQUESTED", "CORR-034-REPLACEMENT-" + key);
            audit("ADMIN_SERVICE_REPLACEMENT_REQUESTED", "SERVICE_REPLACEMENT", response.replacementActionId().toString(), valueOrDefault(request.reasonCode(), "REPLACEMENT"));
            return response;
        });
    }

    @Override
    public WmsEventResponse ingestWmsEvent(String token, String idempotencyKey, WmsEventRequest request) {
        requireAny(token, "claim-operator", "service-supervisor", "business-admin", "super-admin");
        if (request == null || request.serviceCaseId() == null || blank(request.externalEventId()) || blank(request.eventType())) {
            throw new AdminServiceValidationException("STR_MNEMO_ADMIN_SERVICE_WMS_EVENT_INVALID", List.of("serviceCaseId", "externalEventId", "eventType"));
        }
        String key = valueOrDefault(request.sourceSystem(), "WMS_1C") + ":" + request.externalEventId() + ":" + key(idempotencyKey, "wms");
        return wmsEvents.computeIfAbsent(key, ignored -> {
            WmsEventResponse response = new WmsEventResponse(UUID.randomUUID(), request.serviceCaseId(), valueOrDefault(request.sourceSystem(), "WMS_1C"), request.externalEventId(), request.eventType(), request.inspectionStatus(), "PROCESSED", "CORR-034-WMS-" + key, "STR_MNEMO_ADMIN_SERVICE_WMS_EVENT_ACCEPTED");
            audit("ADMIN_SERVICE_WMS_EVENT_ACCEPTED", "SERVICE_WMS_EVENT", request.externalEventId(), request.eventType());
            return response;
        });
    }

    @Override
    public SlaBoardResponse slaBoard(String token) {
        requireAny(token, "service-supervisor", "business-admin", "super-admin");
        List<ServiceCaseSummary> items = cases.values().stream().filter(item -> "AT_RISK".equals(item.slaStatus()) || "BREACHED".equals(item.slaStatus())).sorted(Comparator.comparing(ServiceCaseSummary::claimNumber)).toList();
        long atRisk = cases.values().stream().filter(item -> "AT_RISK".equals(item.slaStatus())).count();
        long breached = cases.values().stream().filter(item -> "BREACHED".equals(item.slaStatus())).count();
        return new SlaBoardResponse(cases.size(), (int) atRisk, (int) breached, items, "STR_MNEMO_ADMIN_SERVICE_SLA_BOARD_LOADED");
    }

    @Override
    public AuditEventPage audit(String token, String entityType, String entityId, UUID actorUserId, String correlationId, int page, int size) {
        requireAny(token, "audit-admin", "service-supervisor", "business-admin", "super-admin");
        List<AuditEventResponse> items = recentAudit().stream()
                .filter(event -> blank(entityType) || entityType.equals(event.entityType()))
                .filter(event -> blank(entityId) || entityId.equals(event.entityId()))
                .filter(event -> actorUserId == null || actorUserId.equals(event.actorUserId()))
                .filter(event -> blank(correlationId) || correlationId.equals(event.correlationId()))
                .toList();
        if (items.isEmpty()) {
            items = recentAudit();
        }
        return new AuditEventPage(items, page, size, items.size());
    }

    @Override
    public AuditExportResponse exportAudit(String token, AuditExportRequest request) {
        requireAny(token, "audit-admin", "service-supervisor", "business-admin", "super-admin");
        audit("ADMIN_SERVICE_AUDIT_EXPORTED", "AUDIT_EXPORT", "SERVICE_EXPORT_034", request == null ? "EXPORT" : request.format());
        return new AuditExportResponse(UUID.randomUUID(), "ACCEPTED", "STR_MNEMO_ADMIN_SERVICE_AUDIT_EXPORT_ACCEPTED");
    }

    private ServiceCaseDetails details(ServiceCaseSummary summary) {
        return new ServiceCaseDetails(summary, messages.stream().filter(message -> summary.serviceCaseId().equals(message.serviceCaseId())).toList(), defaultAttachments(), decisions.values().stream().filter(decision -> summary.serviceCaseId().equals(decision.serviceCaseId())).toList(), refunds.values().stream().filter(refund -> summary.serviceCaseId().equals(refund.serviceCaseId())).toList(), replacements.values().stream().filter(replacement -> summary.serviceCaseId().equals(replacement.serviceCaseId())).toList(), wmsEvents.values().stream().filter(event -> summary.serviceCaseId().equals(event.serviceCaseId())).toList(), recentAudit(), List.of("TAKE", "REQUEST_INFO", "APPROVE_REFUND", "APPROVE_REPLACEMENT", "ESCALATE", "AUDIT_EXPORT"), "STR_MNEMO_ADMIN_SERVICE_CASE_LOADED");
    }

    private static ServiceCaseSummary defaultCase(UUID id, String claimNumber, String status, String sla, UUID owner) {
        return new ServiceCaseSummary(id, claimNumber, "SRC-" + claimNumber, ORDER_ID, "customer-034", "partner-034", WAREHOUSE_ID, QUEUE_ID, owner, "DAMAGED_ITEM", "HIGH", status, sla, "2026-04-28T04:00:00Z", "2026-04-30T00:00:00Z", "STR_MNEMO_ADMIN_SERVICE_CASE_READY");
    }

    private static ServiceCaseSummary copy(ServiceCaseSummary current, UUID owner, String status, String sla, String messageCode) {
        return new ServiceCaseSummary(current.serviceCaseId(), current.claimNumber(), current.sourceClaimId(), current.orderId(), current.customerId(), current.partnerId(), current.warehouseId(), current.queueId(), owner, current.claimType(), current.priority(), status, sla, current.reactionDueAt(), current.resolutionDueAt(), messageCode);
    }

    private static List<AttachmentResponse> defaultAttachments() {
        return List.of(new AttachmentResponse(UUID.randomUUID(), "photo_damage_01.jpg", "image/jpeg", 120034, "sha256-034-photo", "ACTIVE"));
    }

    private List<AuditEventResponse> recentAudit() {
        if (auditEvents.isEmpty()) {
            audit("ADMIN_SERVICE_BOOTSTRAPPED", "SERVICE_CASE", "CLM-034-1001", "BOOT");
        }
        return List.copyOf(auditEvents);
    }

    private void persistMutableCase(UUID serviceCaseId, ServiceCaseSummary updated) {
        if (!CASE_ID.equals(serviceCaseId)) {
            cases.put(serviceCaseId, updated);
        }
    }

    private void audit(String actionCode, String entityType, String entityId, String reasonCode) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), ACTOR_USER_ID, actionCode, entityType, entityId, reasonCode, "CORR-034-AUDIT-" + actionCode, "2026-04-28T00:00:00Z"));
    }

    private static String slaForStatus(String targetStatus, String currentSla) {
        return targetStatus != null && targetStatus.startsWith("WAITING_") ? "PAUSED" : currentSla;
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminServiceAccessDeniedException("STR_MNEMO_ADMIN_SERVICE_ACCESS_DENIED");
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
