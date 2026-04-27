package com.bestorigin.monolith.adminorder.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminOrderDtos {

    private AdminOrderDtos() {
    }

    public record AdminOrderErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record AdminOrderPage(List<AdminOrderSummary> items, int page, int size, long total) {
    }

    public record AdminOrderSummary(UUID orderId, String orderNumber, UUID parentOrderId, String customerId, String partnerId, String catalogPeriodCode, String sourceChannel, String orderStatus, String paymentStatus, String fulfillmentStatus, UUID warehouseId, double totalAmount, double paidAmount, double refundedAmount, String currencyCode, boolean financialHoldActive, String messageCode) {
    }

    public record AdminOrderDetails(AdminOrderSummary summary, List<AdminOrderLine> lines, List<FulfillmentGroupResponse> fulfillmentGroups, List<PaymentEventResponse> paymentEvents, List<RefundResponse> refunds, List<FinancialHoldResponse> financialHolds, List<RiskEventResponse> riskEvents, List<AdminOrderSummary> supplementaryOrders, List<AuditEventResponse> auditEvents, List<String> allowedActions, String messageCode) {
    }

    public record AdminOrderLine(UUID orderLineId, String sku, String productId, double quantity, double unitPrice, double discountAmount, double bonusAmount, double lineTotal, UUID fulfillmentGroupId, String lineStatus) {
    }

    public record FulfillmentGroupResponse(UUID fulfillmentGroupId, UUID warehouseId, String reserveStatus, String shipmentReference, String fulfillmentStatus, String blockedReasonCode, boolean partialAvailable) {
    }

    public record SupplementaryOrderLineRequest(String sku, Double quantity) {
    }

    public record CreateSupplementaryOrderRequest(String reasonCode, String comment, List<SupplementaryOrderLineRequest> lines) {
    }

    public record StatusTransitionRequest(String targetStatus, String reasonCode, String comment, Long version) {
    }

    public record OperatorActionRequest(String actionCode, String reasonCode, Map<String, Object> payload) {
    }

    public record PaymentEventRequest(UUID orderId, String providerCode, String externalPaymentId, String operationType, Double amount, String currencyCode, String payloadChecksum, String occurredAt) {
    }

    public record PaymentEventPage(List<PaymentEventResponse> items, int page, int size, long total) {
    }

    public record PaymentEventResponse(UUID paymentEventId, UUID orderId, String providerCode, String externalPaymentId, String operationType, String paymentStatus, double amount, String currencyCode, boolean duplicate, String correlationId, String occurredAt, String messageCode) {
    }

    public record CreateRefundRequest(String refundType, Double amount, String currencyCode, String reasonCode, String comment, List<UUID> orderLineIds) {
    }

    public record RefundResponse(UUID refundId, UUID orderId, String refundType, String refundStatus, double amount, String currencyCode, String reasonCode, String externalRefundId, String correlationId, String messageCode) {
    }

    public record CreateFinancialHoldRequest(UUID paymentEventId, String reasonCode, String comment, String expiresAt) {
    }

    public record ReleaseFinancialHoldRequest(String reasonCode, String comment) {
    }

    public record FinancialHoldResponse(UUID financialHoldId, UUID orderId, UUID paymentEventId, String holdStatus, String reasonCode, String expiresAt, String messageCode) {
    }

    public record RiskDecisionRequest(UUID riskEventId, String decisionStatus, String reasonCode, String comment) {
    }

    public record RiskEventResponse(UUID riskEventId, UUID orderId, double riskScore, List<String> ruleCodes, String decisionStatus, String decisionReasonCode, String correlationId, String messageCode) {
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
