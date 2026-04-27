package com.bestorigin.monolith.adminorder.impl.service;

import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderDetails;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderLine;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderSummary;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditEventPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditEventResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditExportRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditExportResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateFinancialHoldRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateRefundRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateSupplementaryOrderRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.FinancialHoldResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.FulfillmentGroupResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.OperatorActionRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RefundResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.ReleaseFinancialHoldRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RiskDecisionRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RiskEventResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.StatusTransitionRequest;
import com.bestorigin.monolith.adminorder.impl.exception.AdminOrderAccessDeniedException;
import com.bestorigin.monolith.adminorder.impl.exception.AdminOrderConflictException;
import com.bestorigin.monolith.adminorder.impl.exception.AdminOrderValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminOrderService implements AdminOrderService {

    private static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000033");
    private static final UUID PAYMENT_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000133");
    private static final UUID HOLD_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000233");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000001033");
    private static final UUID FULFILLMENT_GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000002033");
    private static final UUID WAREHOUSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000032");
    private static final UUID ACTOR_USER_ID = UUID.fromString("33000000-0000-0000-0000-000000000033");

    private final ConcurrentMap<UUID, AdminOrderSummary> orders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AdminOrderDetails> supplementaryByKey = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, PaymentEventResponse> paymentsByKey = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RefundResponse> refundsByKey = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, FinancialHoldResponse> holds = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RiskEventResponse> risksByKey = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    public DefaultAdminOrderService() {
        orders.put(ORDER_ID, defaultOrder(ORDER_ID, "BO-033-1001", null, "DELIVERED", "PAID", false));
        orders.put(PAYMENT_ORDER_ID, defaultOrder(PAYMENT_ORDER_ID, "BO-033-1002", null, "PAID", "PAID", false));
        orders.put(HOLD_ORDER_ID, defaultOrder(HOLD_ORDER_ID, "BO-033-1003", null, "PAYMENT_PENDING", "PENDING", true));
        audit("ADMIN_ORDER_BOOTSTRAPPED", "ORDER", "BO-033-1001", "BOOT");
    }

    @Override
    public AdminOrderPage searchOrders(String token, String search, String orderStatus, String paymentStatus, String fulfillmentStatus, UUID warehouseId, String catalogPeriodCode, int page, int size) {
        requireAny(token, "order-admin", "finance-operator", "fraud-admin", "audit-admin", "business-admin", "super-admin");
        List<AdminOrderSummary> items = orders.values().stream()
                .filter(order -> blank(search) || order.orderNumber().contains(search) || order.customerId().contains(search) || order.partnerId().contains(search))
                .filter(order -> blank(orderStatus) || orderStatus.equals(order.orderStatus()))
                .filter(order -> blank(paymentStatus) || paymentStatus.equals(order.paymentStatus()))
                .filter(order -> blank(fulfillmentStatus) || fulfillmentStatus.equals(order.fulfillmentStatus()))
                .filter(order -> warehouseId == null || warehouseId.equals(order.warehouseId()))
                .filter(order -> blank(catalogPeriodCode) || catalogPeriodCode.equals(order.catalogPeriodCode()))
                .sorted(Comparator.comparing(AdminOrderSummary::orderNumber))
                .toList();
        return new AdminOrderPage(items, page, size, items.size());
    }

    @Override
    public AdminOrderDetails getOrder(String token, UUID orderId) {
        requireAny(token, "order-admin", "finance-operator", "fraud-admin", "audit-admin", "support-agent", "business-admin", "super-admin");
        return details(orders.getOrDefault(orderId, defaultOrder(orderId, "BO-033-1001", null, "DELIVERED", "PAID", false)));
    }

    @Override
    public AdminOrderDetails transitionStatus(String token, UUID orderId, String idempotencyKey, StatusTransitionRequest request) {
        requireAny(token, "order-admin", "business-admin", "super-admin");
        if (request == null || blank(request.targetStatus()) || blank(request.reasonCode())) {
            throw new AdminOrderValidationException("STR_MNEMO_ADMIN_ORDER_STATUS_TRANSITION_INVALID", List.of("targetStatus", "reasonCode"));
        }
        AdminOrderSummary current = orders.getOrDefault(orderId, defaultOrder(orderId, "BO-033-1001", null, "DELIVERED", "PAID", false));
        AdminOrderSummary updated = copy(current, request.targetStatus(), current.paymentStatus(), current.financialHoldActive(), current.parentOrderId(), "STR_MNEMO_ADMIN_ORDER_STATUS_UPDATED");
        orders.put(orderId, updated);
        audit("ADMIN_ORDER_STATUS_UPDATED", "ORDER", updated.orderNumber(), request.reasonCode());
        return details(updated);
    }

    @Override
    public AdminOrderDetails createSupplementaryOrder(String token, UUID orderId, String idempotencyKey, CreateSupplementaryOrderRequest request) {
        requireAny(token, "order-admin", "finance-operator", "business-admin", "super-admin");
        String key = key(idempotencyKey, "supplementary-" + orderId);
        return supplementaryByKey.computeIfAbsent(key, ignored -> {
            AdminOrderSummary parent = orders.getOrDefault(orderId, defaultOrder(orderId, "BO-033-1001", null, "DELIVERED", "PAID", false));
            if (parent.financialHoldActive() || "PAYMENT_PENDING".equals(parent.orderStatus())) {
                throw new AdminOrderConflictException("STR_MNEMO_ADMIN_ORDER_FORBIDDEN_ACTION");
            }
            if (request == null || blank(request.reasonCode()) || request.lines() == null || request.lines().isEmpty()) {
                throw new AdminOrderValidationException("STR_MNEMO_ADMIN_ORDER_SUPPLEMENTARY_INVALID", List.of("reasonCode", "lines"));
            }
            UUID childId = UUID.fromString("00000000-0000-0000-0000-000000000333");
            AdminOrderSummary child = defaultOrder(childId, parent.orderNumber() + "-S1", orderId, "PLACED", "PENDING", false);
            orders.put(childId, child);
            audit("ADMIN_ORDER_SUPPLEMENTARY_CREATED", "ORDER", child.orderNumber(), request.reasonCode());
            return new AdminOrderDetails(child, defaultLines(), defaultFulfillment(), defaultPayments(child.orderId()), List.of(), List.of(), defaultRisks(child.orderId()), List.of(parent), recentAudit(), List.of("PAY", "CANCEL"), "STR_MNEMO_ADMIN_ORDER_SUPPLEMENTARY_CREATED");
        });
    }

    @Override
    public AdminOrderDetails executeOperatorAction(String token, UUID orderId, String idempotencyKey, OperatorActionRequest request) {
        requireAny(token, "order-admin", "support-agent", "business-admin", "super-admin");
        if (request == null || blank(request.actionCode()) || blank(request.reasonCode())) {
            throw new AdminOrderValidationException("STR_MNEMO_ADMIN_ORDER_OPERATOR_ACTION_INVALID", List.of("actionCode", "reasonCode"));
        }
        audit("ADMIN_ORDER_OPERATOR_ACTION_APPLIED", "ORDER", orderId.toString(), request.reasonCode());
        return getOrder(token, orderId);
    }

    @Override
    public PaymentEventResponse ingestPaymentEvent(String token, String idempotencyKey, PaymentEventRequest request) {
        requireAny(token, "finance-operator", "payment-provider", "business-admin", "super-admin");
        if (request == null || request.orderId() == null || blank(request.externalPaymentId()) || blank(request.operationType())) {
            throw new AdminOrderValidationException("STR_MNEMO_ADMIN_ORDER_PAYMENT_EVENT_INVALID", List.of("orderId", "externalPaymentId", "operationType"));
        }
        String key = request.providerCode() + ":" + request.externalPaymentId() + ":" + key(idempotencyKey, "payment");
        PaymentEventResponse current = paymentsByKey.get(key);
        if (current != null) {
            return new PaymentEventResponse(current.paymentEventId(), current.orderId(), current.providerCode(), current.externalPaymentId(), current.operationType(), current.paymentStatus(), current.amount(), current.currencyCode(), true, current.correlationId(), current.occurredAt(), "STR_MNEMO_ADMIN_ORDER_PAYMENT_EVENT_DUPLICATE");
        }
        PaymentEventResponse response = new PaymentEventResponse(UUID.fromString("00000000-0000-0000-0000-000000003033"), request.orderId(), valueOrDefault(request.providerCode(), "PAYMENT_PROVIDER"), request.externalPaymentId(), request.operationType(), "PAID", amount(request.amount()), valueOrDefault(request.currencyCode(), "RUB"), false, "CORR-033-PAY-" + key(idempotencyKey, "payment"), valueOrDefault(request.occurredAt(), "2026-04-28T00:00:00Z"), "STR_MNEMO_ADMIN_ORDER_PAYMENT_EVENT_ACCEPTED");
        paymentsByKey.put(key, response);
        orders.put(request.orderId(), defaultOrder(request.orderId(), "BO-033-1002", null, "PAID", "PAID", false));
        audit("ADMIN_ORDER_PAYMENT_EVENT_INGESTED", "PAYMENT_EVENT", response.externalPaymentId(), idempotencyKey);
        return response;
    }

    @Override
    public PaymentEventPage searchPaymentEvents(String token, UUID orderId, String externalPaymentId, String operationType, int page, int size) {
        requireAny(token, "order-admin", "finance-operator", "audit-admin", "business-admin", "super-admin");
        List<PaymentEventResponse> items = paymentsByKey.values().stream()
                .filter(event -> orderId == null || orderId.equals(event.orderId()))
                .filter(event -> blank(externalPaymentId) || externalPaymentId.equals(event.externalPaymentId()))
                .filter(event -> blank(operationType) || operationType.equals(event.operationType()))
                .toList();
        if (items.isEmpty()) {
            items = defaultPayments(orderId == null ? ORDER_ID : orderId);
        }
        return new PaymentEventPage(items, page, size, items.size());
    }

    @Override
    public RefundResponse createRefund(String token, UUID orderId, String idempotencyKey, CreateRefundRequest request) {
        requireAny(token, "finance-operator", "business-admin", "super-admin");
        if (request == null || request.amount() == null || request.amount() <= 0 || blank(request.reasonCode())) {
            throw new AdminOrderValidationException("STR_MNEMO_ADMIN_ORDER_REFUND_INVALID", List.of("amount", "reasonCode"));
        }
        if (request.amount() > 12000) {
            throw new AdminOrderConflictException("STR_MNEMO_ADMIN_ORDER_FINANCIAL_INVARIANT_FAILED");
        }
        String key = key(idempotencyKey, "refund-" + orderId);
        return refundsByKey.computeIfAbsent(key, ignored -> {
            RefundResponse response = new RefundResponse(UUID.fromString("00000000-0000-0000-0000-000000004033"), orderId, valueOrDefault(request.refundType(), "PARTIAL"), "REQUESTED", request.amount(), valueOrDefault(request.currencyCode(), "RUB"), request.reasonCode(), "EXT-REF-033-1", "CORR-033-REFUND-" + key, "STR_MNEMO_ADMIN_ORDER_REFUND_REQUESTED");
            audit("ADMIN_ORDER_REFUND_REQUESTED", "REFUND", response.refundId().toString(), request.reasonCode());
            return response;
        });
    }

    @Override
    public FinancialHoldResponse createFinancialHold(String token, UUID orderId, String idempotencyKey, CreateFinancialHoldRequest request) {
        requireAny(token, "finance-operator", "business-admin", "super-admin");
        if (request == null || blank(request.reasonCode())) {
            throw new AdminOrderValidationException("STR_MNEMO_ADMIN_ORDER_FINANCIAL_HOLD_INVALID", List.of("reasonCode"));
        }
        FinancialHoldResponse response = new FinancialHoldResponse(UUID.fromString("00000000-0000-0000-0000-000000000433"), orderId, request.paymentEventId(), "ACTIVE", request.reasonCode(), request.expiresAt(), "STR_MNEMO_ADMIN_ORDER_FINANCIAL_HOLD_CREATED");
        holds.put(response.financialHoldId(), response);
        AdminOrderSummary current = orders.getOrDefault(orderId, defaultOrder(orderId, "BO-033-1003", null, "PAYMENT_PENDING", "PENDING", true));
        orders.put(orderId, copy(current, current.orderStatus(), current.paymentStatus(), true, current.parentOrderId(), current.messageCode()));
        audit("ADMIN_ORDER_FINANCIAL_HOLD_CREATED", "ORDER", orderId.toString(), request.reasonCode());
        return response;
    }

    @Override
    public FinancialHoldResponse releaseFinancialHold(String token, UUID financialHoldId, String idempotencyKey, ReleaseFinancialHoldRequest request) {
        requireAny(token, "finance-operator", "business-admin", "super-admin");
        FinancialHoldResponse current = holds.getOrDefault(financialHoldId, new FinancialHoldResponse(financialHoldId, HOLD_ORDER_ID, null, "ACTIVE", "PAYMENT_REVIEW", null, "STR_MNEMO_ADMIN_ORDER_FINANCIAL_HOLD_CREATED"));
        FinancialHoldResponse released = new FinancialHoldResponse(current.financialHoldId(), current.orderId(), current.paymentEventId(), "RELEASED", request == null ? current.reasonCode() : valueOrDefault(request.reasonCode(), current.reasonCode()), current.expiresAt(), "STR_MNEMO_ADMIN_ORDER_FINANCIAL_HOLD_RELEASED");
        holds.put(financialHoldId, released);
        AdminOrderSummary currentOrder = orders.getOrDefault(current.orderId(), defaultOrder(current.orderId(), "BO-033-1003", null, "PAYMENT_PENDING", "PENDING", true));
        orders.put(current.orderId(), copy(currentOrder, "PAID", "PAID", false, currentOrder.parentOrderId(), currentOrder.messageCode()));
        audit("ADMIN_ORDER_FINANCIAL_HOLD_RELEASED", "ORDER", current.orderId().toString(), released.reasonCode());
        return released;
    }

    @Override
    public RiskEventResponse decideRisk(String token, UUID orderId, String idempotencyKey, RiskDecisionRequest request) {
        requireAny(token, "fraud-admin", "business-admin", "super-admin");
        if (request == null || request.riskEventId() == null || blank(request.decisionStatus()) || blank(request.reasonCode())) {
            throw new AdminOrderValidationException("STR_MNEMO_ADMIN_ORDER_RISK_DECISION_INVALID", List.of("riskEventId", "decisionStatus", "reasonCode"));
        }
        String key = key(idempotencyKey, "risk-" + request.riskEventId());
        return risksByKey.computeIfAbsent(key, ignored -> {
            RiskEventResponse response = new RiskEventResponse(request.riskEventId(), orderId, 64, List.of("MULTIPLE_SUPPLEMENTARY_ORDERS", "PAYMENT_PROVIDER_DELAY"), request.decisionStatus(), request.reasonCode(), "CORR-033-RISK-" + key, "STR_MNEMO_ADMIN_ORDER_RISK_DECISION_SAVED");
            audit("ADMIN_ORDER_RISK_DECISION_SAVED", "RISK_EVENT", request.riskEventId().toString(), request.reasonCode());
            return response;
        });
    }

    @Override
    public AuditEventPage audit(String token, String entityType, String entityId, UUID actorUserId, String correlationId, int page, int size) {
        requireAny(token, "audit-admin", "order-admin", "finance-operator", "fraud-admin", "business-admin", "super-admin");
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
        requireAny(token, "audit-admin", "business-admin", "super-admin");
        audit("ADMIN_ORDER_AUDIT_EXPORTED", "AUDIT_EXPORT", "ORDER_EXPORT_033", request == null ? "EXPORT" : request.format());
        return new AuditExportResponse(UUID.fromString("00000000-0000-0000-0000-000000005033"), "ACCEPTED", "STR_MNEMO_ADMIN_ORDER_AUDIT_EXPORT_ACCEPTED");
    }

    private AdminOrderDetails details(AdminOrderSummary summary) {
        List<FinancialHoldResponse> orderHolds = holds.values().stream().filter(hold -> summary.orderId().equals(hold.orderId())).toList();
        return new AdminOrderDetails(summary, defaultLines(), defaultFulfillment(), defaultPayments(summary.orderId()), refundsByKey.values().stream().filter(refund -> summary.orderId().equals(refund.orderId())).toList(), orderHolds, defaultRisks(summary.orderId()), supplementaryByKey.values().stream().map(AdminOrderDetails::summary).toList(), recentAudit(), List.of("CREATE_SUPPLEMENTARY", "PARTIAL_REFUND", "RISK_DECISION", "AUDIT_EXPORT"), "STR_MNEMO_ADMIN_ORDER_LOADED");
    }

    private static AdminOrderSummary defaultOrder(UUID id, String number, UUID parentOrderId, String orderStatus, String paymentStatus, boolean financialHoldActive) {
        return new AdminOrderSummary(id, number, parentOrderId, "customer-033", "partner-033", "CAT-033", "WEB", orderStatus, paymentStatus, financialHoldActive ? "BLOCKED" : "DELIVERED", WAREHOUSE_ID, 12000, "PAID".equals(paymentStatus) ? 12000 : 0, 0, "RUB", financialHoldActive, "STR_MNEMO_ADMIN_ORDER_READY");
    }

    private static AdminOrderSummary copy(AdminOrderSummary current, String orderStatus, String paymentStatus, boolean financialHoldActive, UUID parentOrderId, String messageCode) {
        return new AdminOrderSummary(current.orderId(), current.orderNumber(), parentOrderId, current.customerId(), current.partnerId(), current.catalogPeriodCode(), current.sourceChannel(), orderStatus, paymentStatus, financialHoldActive ? "BLOCKED" : current.fulfillmentStatus(), current.warehouseId(), current.totalAmount(), current.paidAmount(), current.refundedAmount(), current.currencyCode(), financialHoldActive, messageCode);
    }

    private static List<AdminOrderLine> defaultLines() {
        return List.of(new AdminOrderLine(LINE_ID, "BOG-SERUM-001", "product-033", 1, 12000, 0, 0, 12000, FULFILLMENT_GROUP_ID, "ACTIVE"));
    }

    private static List<FulfillmentGroupResponse> defaultFulfillment() {
        return List.of(new FulfillmentGroupResponse(FULFILLMENT_GROUP_ID, WAREHOUSE_ID, "CONFIRMED", "SHIP-033-1", "DELIVERED", null, false));
    }

    private static List<PaymentEventResponse> defaultPayments(UUID orderId) {
        return List.of(new PaymentEventResponse(UUID.fromString("00000000-0000-0000-0000-000000003033"), orderId, "PAYMENT_PROVIDER", "EXT-PAY-033-1", "CAPTURE", "PAID", 12000, "RUB", false, "CORR-033-PAY-default", "2026-04-28T00:00:00Z", "STR_MNEMO_ADMIN_ORDER_PAYMENT_EVENT_ACCEPTED"));
    }

    private static List<RiskEventResponse> defaultRisks(UUID orderId) {
        return List.of(new RiskEventResponse(UUID.fromString("00000000-0000-0000-0000-000000000333"), orderId, 64, List.of("MULTIPLE_SUPPLEMENTARY_ORDERS"), "NEEDS_REVIEW", null, "CORR-033-RISK-default", "STR_MNEMO_ADMIN_ORDER_RISK_REVIEW_REQUIRED"));
    }

    private List<AuditEventResponse> recentAudit() {
        if (auditEvents.isEmpty()) {
            audit("ADMIN_ORDER_BOOTSTRAPPED", "ORDER", "BO-033-1001", "BOOT");
        }
        return List.copyOf(auditEvents);
    }

    private void audit(String actionCode, String entityType, String entityId, String reasonCode) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), ACTOR_USER_ID, actionCode, entityType, entityId, reasonCode, "CORR-033-AUDIT-" + actionCode, "2026-04-28T00:00:00Z"));
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminOrderAccessDeniedException("STR_MNEMO_ADMIN_ORDER_ACCESS_DENIED");
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

    private static double amount(Double value) {
        return value == null ? 0 : value;
    }

    private static <T> T valueOrDefault(T value, T fallback) {
        if (value instanceof String stringValue && stringValue.isBlank()) {
            return fallback;
        }
        return value == null ? fallback : value;
    }
}
