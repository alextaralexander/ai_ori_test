package com.bestorigin.monolith.employee.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    public enum CartType {
        MAIN,
        SUPPLEMENTARY
    }

    public enum PaymentStatus {
        PENDING,
        READY_TO_PAY,
        PAID,
        FAILED
    }

    public enum DeliveryStatus {
        DRAFT,
        CONFIRMED,
        IN_TRANSIT,
        DELAYED
    }

    public record EmployeeWorkspaceResponse(
            String sessionId,
            EmployeeCustomerResponse customer,
            EmployeeCartResponse activeCart,
            List<EmployeeOrderSummaryResponse> recentOrders,
            List<EmployeeWarningResponse> warnings,
            EmployeeAuditContextResponse auditContext,
            Map<String, String> linkedRoutes
    ) {
    }

    public record EmployeeCustomerResponse(
            String customerId,
            String partnerPersonNumber,
            String displayName,
            String segment,
            String maskedPhone,
            String maskedEmail
    ) {
    }

    public record EmployeeCartResponse(
            String cartId,
            CartType cartType,
            List<EmployeeOrderItemResponse> items,
            BigDecimal subtotalAmount,
            String currencyCode
    ) {
    }

    public record EmployeeOrderItemResponse(
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            String availabilityStatus
    ) {
    }

    public record EmployeeOrderSummaryResponse(
            String orderNumber,
            String createdAt,
            String orderStatus,
            PaymentStatus paymentStatus,
            DeliveryStatus deliveryStatus,
            BigDecimal grandTotalAmount,
            String currencyCode
    ) {
    }

    public record EmployeeOrderHistoryFilterRequest(
            String partnerId,
            String customerId,
            String dateFrom,
            String dateTo,
            String orderStatus,
            PaymentStatus paymentStatus,
            DeliveryStatus deliveryStatus,
            boolean problemOnly,
            String query,
            int page,
            int size,
            String sort
    ) {
    }

    public record EmployeeOrderHistoryPageResponse(
            List<EmployeeOrderHistorySummaryResponse> items,
            int page,
            int size,
            long totalElements,
            boolean auditRecorded,
            List<String> availableProblemFilters
    ) {
    }

    public record EmployeeOrderHistorySummaryResponse(
            String orderId,
            String orderNumber,
            String campaignCode,
            String customerId,
            String partnerId,
            String customerDisplayName,
            String partnerDisplayName,
            String maskedPhone,
            String maskedEmail,
            String orderStatus,
            PaymentStatus paymentStatus,
            DeliveryStatus deliveryStatus,
            String fulfillmentStatus,
            BigDecimal totalAmount,
            String currencyCode,
            List<String> problemFlags,
            Map<String, String> linkedRoutes,
            String updatedAt
    ) {
    }

    public record EmployeeOrderHistoryDetailsResponse(
            String orderId,
            String orderNumber,
            String campaignCode,
            String customerId,
            String partnerId,
            String customerDisplayName,
            String partnerDisplayName,
            String maskedPhone,
            String maskedEmail,
            String orderStatus,
            PaymentStatus paymentStatus,
            DeliveryStatus deliveryStatus,
            String fulfillmentStatus,
            BigDecimal totalAmount,
            String currencyCode,
            List<String> problemFlags,
            Map<String, String> linkedRoutes,
            String updatedAt,
            List<EmployeeOrderHistoryItemResponse> items,
            List<EmployeeLinkedEventResponse> paymentEvents,
            List<EmployeeLinkedEventResponse> deliveryEvents,
            List<EmployeeLinkedEventResponse> wmsEvents,
            List<String> supportCaseIds,
            List<String> claimIds,
            List<String> paymentEventIds,
            String wmsBatchId,
            String deliveryTrackingId,
            boolean manualAdjustmentPresent,
            boolean supervisorRequired,
            String sourceChannel,
            List<EmployeeAuditEventResponse> auditEvents
    ) {
    }

    public record EmployeeOrderHistoryItemResponse(
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            String promoCode,
            int bonusPoints,
            String reserveStatus
    ) {
    }

    public record EmployeeLinkedEventResponse(
            String eventId,
            String eventType,
            String status,
            String sourceSystem,
            String occurredAt,
            String messageCode
    ) {
    }

    public record EmployeeWarningResponse(String code, String severity, String target) {
    }

    public record EmployeeAuditContextResponse(
            String actorUserId,
            String supportReasonCode,
            String sourceChannel,
            boolean auditRecorded
    ) {
    }

    public record EmployeeOperatorOrderCreateRequest(
            String targetCustomerId,
            String supportReasonCode,
            CartType cartType,
            List<EmployeeOrderItemRequest> items
    ) {
    }

    public record EmployeeOrderItemRequest(String sku, int quantity) {
    }

    public record EmployeeOperatorOrderResponse(
            UUID operatorOrderId,
            String checkoutId,
            String orderNumber,
            PaymentStatus paymentStatus,
            DeliveryStatus deliveryStatus,
            BigDecimal grandTotalAmount,
            String currencyCode,
            String nextAction,
            String messageCode,
            boolean auditRecorded,
            List<EmployeeAuditEventResponse> auditEvents
    ) {
    }

    public record EmployeeConfirmOrderRequest(long checkoutVersion) {
    }

    public record EmployeeOrderSupportResponse(
            String orderNumber,
            EmployeeCustomerResponse customer,
            EmployeeOrderSummaryResponse order,
            List<EmployeeTimelineEventResponse> timeline,
            List<EmployeeSupportActionResponse> supportActions,
            List<EmployeeWarningResponse> warnings,
            Map<String, String> linkedRoutes
    ) {
    }

    public record EmployeeTimelineEventResponse(
            String eventType,
            String publicStatus,
            String sourceSystem,
            String descriptionCode,
            String occurredAt
    ) {
    }

    public record EmployeeSupportActionRequest(
            String reasonCode,
            String note,
            BigDecimal amount,
            String ownerRole,
            String dueAt
    ) {
    }

    public record EmployeeSupportActionResponse(
            UUID actionId,
            String orderNumber,
            String actionType,
            String reasonCode,
            BigDecimal amount,
            boolean supervisorRequired,
            String visibility,
            String messageCode,
            String createdAt
    ) {
    }

    public record EmployeeEscalationPageResponse(
            List<EmployeeSupportActionResponse> items,
            int page,
            int size,
            long totalElements
    ) {
    }

    public record EmployeeAuditEventResponse(
            String eventType,
            String actorUserId,
            String targetEntityType,
            String targetEntityId,
            String occurredAt
    ) {
    }

    public record EmployeeClaimCreateRequest(
            String customerId,
            String partnerId,
            String orderId,
            String orderNumber,
            String sourceChannel,
            String supportReasonCode,
            String requestedResolution,
            String comment,
            List<EmployeeClaimItemRequest> items,
            List<EmployeeClaimAttachmentRequest> attachments
    ) {
    }

    public record EmployeeClaimItemRequest(
            String sku,
            String productCode,
            int quantity,
            String problemType,
            String requestedResolution,
            String internalComment
    ) {
    }

    public record EmployeeClaimAttachmentRequest(
            String fileId,
            String filename,
            String mimeType,
            long sizeBytes,
            String accessPolicy
    ) {
    }

    public record EmployeeClaimTransitionRequest(
            String transitionCode,
            String supportReasonCode,
            String assigneeId,
            String resultCode,
            String comment,
            BigDecimal approvedCompensationAmount
    ) {
    }

    public record EmployeeClaimPageResponse(
            List<EmployeeClaimSummaryResponse> items,
            int page,
            int size,
            long totalElements,
            boolean auditRecorded,
            List<String> availableFilters
    ) {
    }

    public record EmployeeClaimSummaryResponse(
            String claimId,
            String claimNumber,
            String orderNumber,
            String customerOrPartnerLabel,
            String maskedContact,
            String status,
            String slaState,
            String slaDueAt,
            String resolutionType,
            BigDecimal compensationAmount,
            String currencyCode,
            String assignee,
            String responsibleRole,
            String updatedAt,
            List<String> availableActions
    ) {
    }

    public record EmployeeClaimDetailsResponse(
            String claimId,
            String claimNumber,
            String orderNumber,
            String customerId,
            String partnerId,
            String status,
            String slaState,
            String slaDueAt,
            String requestedResolution,
            String approvedResolution,
            BigDecimal compensationAmount,
            String currencyCode,
            String publicReasonMnemonic,
            boolean supervisorRequired,
            List<EmployeeClaimItemResponse> items,
            List<EmployeeClaimAttachmentResponse> attachments,
            List<EmployeeClaimRouteTaskResponse> routeTasks,
            List<EmployeeClaimAuditEventResponse> auditEvents,
            List<String> availableActions
    ) {
    }

    public record EmployeeClaimItemResponse(
            String sku,
            String productCode,
            String productName,
            int quantity,
            String problemType,
            String requestedResolution,
            String approvedResolution,
            BigDecimal compensationAmount
    ) {
    }

    public record EmployeeClaimAttachmentResponse(
            String attachmentId,
            String filename,
            String mimeType,
            long sizeBytes,
            String uploadedBy,
            String uploadedAt,
            String accessPolicy
    ) {
    }

    public record EmployeeClaimRouteTaskResponse(
            String taskId,
            String taskType,
            String status,
            String assigneeRole,
            String assigneeId,
            String dueAt,
            String completedAt,
            String resultCode
    ) {
    }

    public record EmployeeClaimAuditEventResponse(
            String auditEventId,
            String actorUserId,
            String actorRole,
            String actionType,
            String supportReasonCode,
            String sourceRoute,
            String correlationId,
            String occurredAt
    ) {
    }

    public record EmployeeErrorResponse(
            String code,
            List<EmployeeWarningResponse> details,
            Map<String, String> metadata
    ) {
    }
}
