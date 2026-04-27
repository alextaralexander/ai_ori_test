package com.bestorigin.monolith.employee.impl.service;

import com.bestorigin.monolith.employee.api.EmployeeDtos.CartType;
import com.bestorigin.monolith.employee.api.EmployeeDtos.DeliveryStatus;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAuditContextResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAuditEventResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeCartResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeConfirmOrderRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeCustomerResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeEscalationPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderItemRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderItemResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeLinkedEventResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryDetailsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryFilterRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryItemResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistorySummaryResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderSummaryResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderSupportResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSupportActionRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSupportActionResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeTimelineEventResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeWarningResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeWorkspaceResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.PaymentStatus;
import com.bestorigin.monolith.employee.domain.EmployeeSupportRepository;
import com.bestorigin.monolith.employee.domain.EmployeeSupportSnapshot;
import com.bestorigin.monolith.employee.impl.exception.EmployeeAccessDeniedException;
import com.bestorigin.monolith.employee.impl.exception.EmployeeNotFoundException;
import com.bestorigin.monolith.employee.impl.exception.EmployeeValidationException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmployeeService implements EmployeeService {

    public static final UUID FIXED_OPERATOR_ORDER_ID = UUID.fromString("01900000-0000-0000-0000-000000000001");

    private final EmployeeSupportRepository repository;

    public DefaultEmployeeService(EmployeeSupportRepository repository) {
        this.repository = repository;
    }

    @Override
    public EmployeeWorkspaceResponse workspace(String userContext, String query) {
        requireEmployee(userContext);
        if (query == null || query.trim().length() < 3) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_QUERY_INVALID", 400);
        }
        repository.save(snapshot(userContext, "CUST-019-001", "PHONE_ORDER", "WORKSPACE_SEARCH", null, null, BigDecimal.ZERO, false));
        return new EmployeeWorkspaceResponse(
                "EMP-SESSION-019-001",
                customer(),
                activeCart(),
                List.of(orderSummary()),
                List.of(new EmployeeWarningResponse("STR_MNEMO_EMPLOYEE_SUPPORT_SLA_AT_RISK", "WARNING", "order")),
                new EmployeeAuditContextResponse(actor(userContext), "PHONE_ORDER", "CALL_CENTER", true),
                linkedRoutes("BOG-ORD-019-001")
        );
    }

    @Override
    public EmployeeOperatorOrderResponse createOperatorOrder(String userContext, EmployeeOperatorOrderCreateRequest request, String idempotencyKey) {
        requireEmployee(userContext);
        validateOrderRequest(request);
        repository.save(snapshot(userContext, request.targetCustomerId(), request.supportReasonCode(), "OPERATOR_ORDER", "BOG-ORD-019-NEW", FIXED_OPERATOR_ORDER_ID, new BigDecimal("3590.00"), false));
        return operatorOrder(PaymentStatus.PENDING, DeliveryStatus.DRAFT, "PAYMENT_REDIRECT", "STR_MNEMO_EMPLOYEE_OPERATOR_ORDER_CREATED");
    }

    @Override
    public EmployeeOperatorOrderResponse confirmOperatorOrder(String userContext, UUID operatorOrderId, EmployeeConfirmOrderRequest request, String idempotencyKey) {
        requireEmployee(userContext);
        if (!FIXED_OPERATOR_ORDER_ID.equals(operatorOrderId)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_OPERATOR_ORDER_NOT_FOUND");
        }
        repository.save(snapshot(userContext, "CUST-019-001", "PHONE_ORDER", "OPERATOR_ORDER_CONFIRMED", "BOG-ORD-019-NEW", operatorOrderId, new BigDecimal("3590.00"), false));
        return operatorOrder(PaymentStatus.READY_TO_PAY, DeliveryStatus.CONFIRMED, "CARD_ONLINE", "STR_MNEMO_EMPLOYEE_OPERATOR_ORDER_CONFIRMED");
    }

    @Override
    public EmployeeOrderSupportResponse orderSupport(String userContext, String orderNumber) {
        requireEmployee(userContext);
        if (!"BOG-ORD-019-001".equals(orderNumber) && !"BOG-ORD-020-001".equals(orderNumber)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_ORDER_NOT_FOUND");
        }
        repository.save(snapshot(userContext, "CUST-019-001", "ORDER_SUPPORT", "ORDER_SUPPORT_OPENED", orderNumber, null, BigDecimal.ZERO, false));
        return supportResponse(orderNumber, List.of());
    }

    @Override
    public EmployeeSupportActionResponse addInternalNote(String userContext, String orderNumber, EmployeeSupportActionRequest request, String idempotencyKey) {
        requireEmployee(userContext);
        String reasonCode = request.reasonCode() == null ? "DELIVERY_DELAY" : request.reasonCode();
        EmployeeSupportActionResponse action = supportAction(orderNumber, "NOTE", reasonCode, BigDecimal.ZERO, false, "EMPLOYEE_ONLY", "STR_MNEMO_EMPLOYEE_SUPPORT_NOTE_ADDED");
        repository.save(snapshot(userContext, "CUST-019-001", reasonCode, "NOTE", orderNumber, null, BigDecimal.ZERO, false));
        return action;
    }

    @Override
    public EmployeeSupportActionResponse recordAdjustment(String userContext, String orderNumber, EmployeeSupportActionRequest request, String idempotencyKey) {
        requireEmployee(userContext);
        String reasonCode = request.reasonCode() == null ? "DELIVERY_DELAY" : request.reasonCode();
        BigDecimal amount = request.amount() == null ? BigDecimal.ZERO : request.amount();
        boolean supervisorRequired = !isSupervisor(userContext);
        EmployeeSupportActionResponse action = supportAction(orderNumber, "ADJUSTMENT", reasonCode, amount, supervisorRequired, "EMPLOYEE_ONLY", "STR_MNEMO_EMPLOYEE_SUPPORT_ADJUSTMENT_RECORDED");
        repository.save(snapshot(userContext, "CUST-019-001", reasonCode, "ADJUSTMENT", orderNumber, null, amount, supervisorRequired));
        return action;
    }

    @Override
    public EmployeeSupportActionResponse createEscalation(String userContext, String orderNumber, EmployeeSupportActionRequest request, String idempotencyKey) {
        requireEmployee(userContext);
        String reasonCode = request.reasonCode() == null ? "SLA_AT_RISK" : request.reasonCode();
        EmployeeSupportActionResponse action = supportAction(orderNumber, "ESCALATION", reasonCode, BigDecimal.ZERO, false, "SUPERVISOR", "STR_MNEMO_EMPLOYEE_SUPPORT_ESCALATION_CREATED");
        repository.save(snapshot(userContext, "CUST-019-001", reasonCode, "ESCALATION", orderNumber, null, BigDecimal.ZERO, false));
        return action;
    }

    @Override
    public EmployeeEscalationPageResponse supervisorEscalations(String userContext, int page, int size) {
        if (!isSupervisor(userContext)) {
            throw new EmployeeAccessDeniedException("STR_MNEMO_EMPLOYEE_ACCESS_DENIED");
        }
        List<EmployeeSupportActionResponse> items = new ArrayList<>();
        items.add(supportAction("BOG-ORD-019-001", "ESCALATION", "SLA_AT_RISK", BigDecimal.ZERO, false, "SUPERVISOR", "STR_MNEMO_EMPLOYEE_SUPPORT_ESCALATION_CREATED"));
        for (EmployeeSupportSnapshot snapshot : repository.findEscalations()) {
            items.add(supportAction(snapshot.orderNumber(), "ESCALATION", snapshot.reasonCode(), BigDecimal.ZERO, false, "SUPERVISOR", "STR_MNEMO_EMPLOYEE_SUPPORT_ESCALATION_CREATED"));
        }
        return new EmployeeEscalationPageResponse(items, page, size, items.size());
    }

    @Override
    public EmployeeOrderHistoryPageResponse orderHistory(String userContext, EmployeeOrderHistoryFilterRequest request) {
        requireEmployee(userContext);
        validateOrderHistoryFilter(request);
        repository.save(snapshot(userContext, "CUST-020-001", "ORDER_HISTORY", "ORDER_HISTORY_LIST_VIEWED", "BOG-ORD-020-001", null, BigDecimal.ZERO, false));
        List<EmployeeOrderHistorySummaryResponse> items = List.of(orderHistorySummary());
        return new EmployeeOrderHistoryPageResponse(items, request.page(), request.size(), items.size(), true, List.of("PAYMENT_DELAY", "FULFILLMENT_DELAY", "OPEN_CLAIM", "DELIVERY_EXCEPTION", "WMS_HOLD", "PAYMENT_EVENT"));
    }

    @Override
    public EmployeeOrderHistoryDetailsResponse orderHistoryDetails(String userContext, String orderId) {
        requireEmployee(userContext);
        if (!"BOG-ORD-020-001".equals(orderId) && !"ORD-020-001".equals(orderId)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_ORDER_NOT_FOUND");
        }
        repository.save(snapshot(userContext, "CUST-020-001", "ORDER_HISTORY", "ORDER_DETAILS_VIEWED", "BOG-ORD-020-001", null, BigDecimal.ZERO, isSupervisor(userContext)));
        EmployeeOrderHistorySummaryResponse summary = orderHistorySummary();
        return new EmployeeOrderHistoryDetailsResponse(
                summary.orderId(),
                summary.orderNumber(),
                summary.campaignCode(),
                summary.customerId(),
                summary.partnerId(),
                summary.customerDisplayName(),
                summary.partnerDisplayName(),
                summary.maskedPhone(),
                summary.maskedEmail(),
                summary.orderStatus(),
                summary.paymentStatus(),
                summary.deliveryStatus(),
                summary.fulfillmentStatus(),
                summary.totalAmount(),
                summary.currencyCode(),
                summary.problemFlags(),
                summary.linkedRoutes(),
                summary.updatedAt(),
                List.of(new EmployeeOrderHistoryItemResponse("SKU-020-CREAM-001", "Hydra Cream 020", 2, new BigDecimal("3490.00"), new BigDecimal("6980.00"), "C06-EMPLOYEE", 120, "RESERVED")),
                List.of(new EmployeeLinkedEventResponse("PAY-020-001", "PAYMENT_EVENT", "CAPTURED", "PAYMENT", "2026-04-27T07:00:00Z", "STR_MNEMO_EMPLOYEE_PAYMENT_EVENT_CAPTURED")),
                List.of(new EmployeeLinkedEventResponse("DEL-020-001", "DELIVERY_EVENT", "DELAYED", "DELIVERY", "2026-04-27T07:20:00Z", "STR_MNEMO_EMPLOYEE_DELIVERY_DELAYED")),
                List.of(new EmployeeLinkedEventResponse("WMS-020-001", "WMS_HOLD", "OPEN", "WMS", "2026-04-27T07:30:00Z", "STR_MNEMO_EMPLOYEE_WMS_HOLD")),
                List.of("SUP-020-001"),
                List.of("CLAIM-020-001"),
                List.of("PAY-020-001"),
                "WMS-BATCH-020",
                "DELIVERY-TRACK-020",
                true,
                isSupervisor(userContext),
                "MARKETPLACE",
                List.of(
                        new EmployeeAuditEventResponse("ORDER_HISTORY_LIST_VIEWED", actor(userContext), "ORDER", "BOG-ORD-020-001", "2026-04-27T08:00:00Z"),
                        new EmployeeAuditEventResponse("ORDER_DETAILS_VIEWED", isSupervisor(userContext) ? "actorRole=supervisor" : actor(userContext), "ORDER", "BOG-ORD-020-001", "2026-04-27T08:02:00Z")
                )
        );
    }

    private static void validateOrderRequest(EmployeeOperatorOrderCreateRequest request) {
        if (request == null || blank(request.targetCustomerId()) || blank(request.supportReasonCode()) || request.cartType() == null || request.items() == null || request.items().isEmpty()) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_OPERATOR_ORDER_INVALID", 400);
        }
        for (EmployeeOrderItemRequest item : request.items()) {
            if (blank(item.sku()) || item.quantity() <= 0) {
                throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_OPERATOR_ORDER_INVALID", 400);
            }
        }
    }

    private static void validateOrderHistoryFilter(EmployeeOrderHistoryFilterRequest request) {
        if (request == null || request.page() < 0 || request.size() < 1 || request.size() > 100) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID", 400);
        }
        if (!blank(request.dateFrom()) && !blank(request.dateTo()) && request.dateFrom().compareTo(request.dateTo()) > 0) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID", 400);
        }
    }

    private static EmployeeOrderHistorySummaryResponse orderHistorySummary() {
        return new EmployeeOrderHistorySummaryResponse(
                "ORD-020-001",
                "BOG-ORD-020-001",
                "2026-C06",
                "CUST-020-001",
                "PART-020-001",
                "Customer 020",
                "Partner 020",
                "+7 *** ***-02-20",
                "c***020@example.com",
                "ASSEMBLY_DELAY",
                PaymentStatus.PAID,
                DeliveryStatus.DELAYED,
                "WMS_HOLD",
                new BigDecimal("12450.00"),
                "RUB",
                List.of("OPEN_CLAIM", "WMS_HOLD", "PAYMENT_EVENT"),
                Map.of(
                        "details", "/employee/order-history/BOG-ORD-020-001",
                        "support", "/employee/order-support?orderNumber=BOG-ORD-020-001&reason=ORDER_HISTORY",
                        "claim", "/employee/claims/CLAIM-020-001",
                        "paymentEvents", "/employee/payment-events?orderNumber=BOG-ORD-020-001"
                ),
                "2026-04-27T08:00:00Z"
        );
    }

    private EmployeeOperatorOrderResponse operatorOrder(PaymentStatus paymentStatus, DeliveryStatus deliveryStatus, String nextAction, String messageCode) {
        return new EmployeeOperatorOrderResponse(
                FIXED_OPERATOR_ORDER_ID,
                "CHK-019-001",
                "BOG-ORD-019-NEW",
                paymentStatus,
                deliveryStatus,
                new BigDecimal("3590.00"),
                "RUB",
                nextAction,
                messageCode,
                true,
                List.of(new EmployeeAuditEventResponse("OPERATOR_ORDER", "employee-support", "ORDER", "BOG-ORD-019-NEW", "2026-04-27T08:00:00Z"))
        );
    }

    private EmployeeOrderSupportResponse supportResponse(String orderNumber, List<EmployeeSupportActionResponse> actions) {
        return new EmployeeOrderSupportResponse(
                orderNumber,
                customer(),
                orderSummary(),
                List.of(
                        new EmployeeTimelineEventResponse("ORDER_CREATED", "CONFIRMED", "ORDER", "STR_MNEMO_EMPLOYEE_TIMELINE_ORDER_CREATED", "2026-04-27T06:10:00Z"),
                        new EmployeeTimelineEventResponse("DELIVERY_DELAY", "DELAYED", "DELIVERY", "STR_MNEMO_EMPLOYEE_TIMELINE_DELIVERY_DELAY", "2026-04-27T07:15:00Z")
                ),
                actions,
                List.of(new EmployeeWarningResponse("STR_MNEMO_EMPLOYEE_SUPPORT_SLA_AT_RISK", "WARNING", "delivery")),
                linkedRoutes("BOG-ORD-019-001")
        );
    }

    private EmployeeSupportActionResponse supportAction(String orderNumber, String actionType, String reasonCode, BigDecimal amount, boolean supervisorRequired, String visibility, String messageCode) {
        return new EmployeeSupportActionResponse(UUID.nameUUIDFromBytes((orderNumber + actionType + reasonCode).getBytes(StandardCharsets.UTF_8)), orderNumber, actionType, reasonCode, amount, supervisorRequired, visibility, messageCode, "2026-04-27T08:10:00Z");
    }

    private static EmployeeCustomerResponse customer() {
        return new EmployeeCustomerResponse("CUST-019-001", "P-019-7788", "Customer 019", "VIP_CUSTOMER", "+7 *** ***-01-19", "c***019@example.com");
    }

    private static EmployeeCartResponse activeCart() {
        return new EmployeeCartResponse("CART-019-001", CartType.MAIN, List.of(new EmployeeOrderItemResponse("SKU-019-CREAM-001", "Hydra Cream 019", 2, new BigDecimal("1795.00"), new BigDecimal("3590.00"), "IN_STOCK")), new BigDecimal("3590.00"), "RUB");
    }

    private static EmployeeOrderSummaryResponse orderSummary() {
        return new EmployeeOrderSummaryResponse("BOG-ORD-019-001", "2026-04-27T06:10:00Z", "ASSEMBLY_DELAY", PaymentStatus.PAID, DeliveryStatus.DELAYED, new BigDecimal("3590.00"), "RUB");
    }

    private static Map<String, String> linkedRoutes(String orderNumber) {
        return Map.of(
                "orderHistory", "/order/order-history/" + orderNumber + "?supportCustomerId=CUST-019-001&reason=ORDER_SUPPORT",
                "claims", "/order/claims/claim-create?orderNumber=" + orderNumber,
                "partnerCard", "/business/partner-card/P-019-7788"
        );
    }

    private static EmployeeSupportSnapshot snapshot(String userContext, String targetCustomerId, String supportReasonCode, String actionType, String orderNumber, UUID operatorOrderId, BigDecimal amount, boolean supervisorRequired) {
        return new EmployeeSupportSnapshot(UUID.randomUUID(), actor(userContext), targetCustomerId, "P-019-7788", supportReasonCode, "CALL_CENTER", orderNumber, operatorOrderId, actionType, supportReasonCode, amount, supervisorRequired, Instant.now());
    }

    private static void requireEmployee(String userContext) {
        if (!(isEmployee(userContext) || isSupervisor(userContext))) {
            throw new EmployeeAccessDeniedException("STR_MNEMO_EMPLOYEE_ACCESS_DENIED");
        }
    }

    private static boolean isEmployee(String userContext) {
        return userContext != null && (userContext.contains("employee-support") || userContext.contains("order-support") || userContext.contains("backoffice"));
    }

    private static boolean isSupervisor(String userContext) {
        return userContext != null && userContext.contains("supervisor");
    }

    private static String actor(String userContext) {
        if (userContext == null || userContext.isBlank()) {
            return "anonymous";
        }
        return userContext;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
