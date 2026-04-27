package com.bestorigin.monolith.adminservice.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminServiceDtos {

    private AdminServiceDtos() {
    }

    public record AdminServiceErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record ServiceCasePage(List<ServiceCaseSummary> items, int page, int size, long total) {
    }

    public record ServiceCaseSummary(UUID serviceCaseId, String claimNumber, String sourceClaimId, UUID orderId, String customerId, String partnerId, UUID warehouseId, UUID queueId, UUID ownerUserId, String claimType, String priority, String caseStatus, String slaStatus, String reactionDueAt, String resolutionDueAt, String messageCode) {
    }

    public record ServiceCaseDetails(ServiceCaseSummary summary, List<ServiceMessageResponse> messages, List<AttachmentResponse> attachments, List<DecisionResponse> decisions, List<RefundActionResponse> refundActions, List<ReplacementActionResponse> replacementActions, List<WmsEventResponse> wmsEvents, List<AuditEventResponse> auditEvents, List<String> allowedActions, String messageCode) {
    }

    public record CreateServiceCaseRequest(String sourceClaimId, UUID orderId, String customerId, String partnerId, UUID warehouseId, String claimType, String priority, String reasonCode) {
    }

    public record AssignmentRequest(String assignmentAction, UUID ownerUserId, String reasonCode) {
    }

    public record StatusTransitionRequest(String targetStatus, String reasonCode, String comment, Long version) {
    }

    public record CreateMessageRequest(String messageType, String visibility, String customerVisibleMessageCode, String body, String reasonCode) {
    }

    public record ServiceMessageResponse(UUID messageId, UUID serviceCaseId, String messageType, String visibility, UUID authorUserId, String customerVisibleMessageCode, String createdAt, String messageCode) {
    }

    public record AttachmentResponse(UUID attachmentId, String fileName, String mimeType, long sizeBytes, String checksum, String attachmentStatus) {
    }

    public record UpsertQueueRequest(String queueCode, String queueName, boolean active, String defaultOwnerGroup, String regionCode, UUID warehouseId) {
    }

    public record ServiceQueueResponse(UUID queueId, String queueCode, String queueName, boolean active, String defaultOwnerGroup, String regionCode, UUID warehouseId, String messageCode) {
    }

    public record CreateDecisionRequest(String decisionType, String reasonCode, String customerMessageCode, String comment) {
    }

    public record DecisionResponse(UUID decisionId, UUID serviceCaseId, String decisionType, String decisionStatus, String reasonCode, boolean supervisorOverride, String customerMessageCode, String correlationId, String messageCode) {
    }

    public record CreateRefundActionRequest(Double amount, String currencyCode, String paymentReference, String reasonCode) {
    }

    public record RefundActionResponse(UUID refundActionId, UUID serviceCaseId, UUID orderId, double amount, String currencyCode, String refundStatus, String externalRefundId, String messageCode, String correlationId) {
    }

    public record CreateReplacementActionRequest(String sku, Double quantity, UUID warehouseId, String reasonCode) {
    }

    public record ReplacementActionResponse(UUID replacementActionId, UUID serviceCaseId, UUID orderId, String sku, double quantity, UUID warehouseId, String replacementStatus, String shipmentReference, String messageCode, String correlationId) {
    }

    public record WmsEventRequest(UUID serviceCaseId, String sourceSystem, String externalEventId, String eventType, String inspectionStatus, String payloadChecksum) {
    }

    public record WmsEventResponse(UUID wmsEventId, UUID serviceCaseId, String sourceSystem, String externalEventId, String eventType, String inspectionStatus, String retryStatus, String correlationId, String messageCode) {
    }

    public record SlaBoardResponse(int activeCases, int atRiskCases, int breachedCases, List<ServiceCaseSummary> items, String messageCode) {
    }

    public record AuditEventPage(List<AuditEventResponse> items, int page, int size, long total) {
    }

    public record AuditEventResponse(UUID auditEventId, UUID actorUserId, String actionCode, String entityType, String entityId, String reasonCode, String correlationId, String occurredAt) {
    }

    public record AuditExportRequest(Map<String, Object> filters, String format) {
    }

    public record AuditExportResponse(UUID exportJobId, String status, String messageCode) {
    }
}
