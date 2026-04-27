package com.bestorigin.monolith.adminorder.impl.service;

import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderDetails;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditEventPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditExportRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditExportResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateFinancialHoldRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateRefundRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateSupplementaryOrderRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.FinancialHoldResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.OperatorActionRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RefundResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.ReleaseFinancialHoldRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RiskDecisionRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RiskEventResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.StatusTransitionRequest;
import java.util.UUID;

public interface AdminOrderService {

    AdminOrderPage searchOrders(String token, String search, String orderStatus, String paymentStatus, String fulfillmentStatus, UUID warehouseId, String catalogPeriodCode, int page, int size);

    AdminOrderDetails getOrder(String token, UUID orderId);

    AdminOrderDetails transitionStatus(String token, UUID orderId, String idempotencyKey, StatusTransitionRequest request);

    AdminOrderDetails createSupplementaryOrder(String token, UUID orderId, String idempotencyKey, CreateSupplementaryOrderRequest request);

    AdminOrderDetails executeOperatorAction(String token, UUID orderId, String idempotencyKey, OperatorActionRequest request);

    PaymentEventResponse ingestPaymentEvent(String token, String idempotencyKey, PaymentEventRequest request);

    PaymentEventPage searchPaymentEvents(String token, UUID orderId, String externalPaymentId, String operationType, int page, int size);

    RefundResponse createRefund(String token, UUID orderId, String idempotencyKey, CreateRefundRequest request);

    FinancialHoldResponse createFinancialHold(String token, UUID orderId, String idempotencyKey, CreateFinancialHoldRequest request);

    FinancialHoldResponse releaseFinancialHold(String token, UUID financialHoldId, String idempotencyKey, ReleaseFinancialHoldRequest request);

    RiskEventResponse decideRisk(String token, UUID orderId, String idempotencyKey, RiskDecisionRequest request);

    AuditEventPage audit(String token, String entityType, String entityId, UUID actorUserId, String correlationId, int page, int size);

    AuditExportResponse exportAudit(String token, AuditExportRequest request);
}
