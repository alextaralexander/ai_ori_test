package com.bestorigin.monolith.employee.impl.service;

import com.bestorigin.monolith.employee.api.EmployeeDtos.CartType;
import com.bestorigin.monolith.employee.api.EmployeeDtos.DeliveryStatus;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAuditContextResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAuditEventResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeCartResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimAttachmentResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimAuditEventResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimDetailsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimItemRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimItemResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimRouteTaskResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimSummaryResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimTransitionRequest;
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
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerCardResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerKpiResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerOrderReportResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerOrderSummaryResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerReportAggregateResponse;
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

    @Override
    public EmployeeClaimDetailsResponse submitClaim(String userContext, EmployeeClaimCreateRequest request, String idempotencyKey) {
        requireEmployee(userContext);
        validateClaimCreateRequest(request);
        repository.save(snapshot(userContext, request.customerId() == null ? "CUST-021-001" : request.customerId(), request.supportReasonCode(), "EMPLOYEE_CLAIM_CREATED", request.orderNumber(), null, new BigDecimal("1250.00"), false));
        return claimDetailsResponse(userContext, "BOG-CLM-021-001", request.supportReasonCode(), List.of(routeTask("WAREHOUSE-021-001", "WAREHOUSE", "OPEN", "warehouse", null, "2026-04-28T08:00:00Z", null, "WAREHOUSE_REVIEW_REQUESTED")), "STR_MNEMO_EMPLOYEE_CLAIM_CREATED", false);
    }

    @Override
    public EmployeeClaimPageResponse claims(String userContext, String claimStatus, String dateFrom, String dateTo, String slaState, String responsibleRole, String assigneeId, String resolutionType, String sourceChannel, String warehouseCode, String financeStatus, String query, int page, int size, String sort) {
        requireEmployee(userContext);
        if (page < 0 || size < 1 || size > 100 || (!blank(dateFrom) && !blank(dateTo) && dateFrom.compareTo(dateTo) > 0)) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_CLAIM_FILTER_INVALID", 400);
        }
        repository.save(snapshot(userContext, "CUST-021-001", "EMPLOYEE_CLAIM_LIST", "EMPLOYEE_CLAIM_LIST_VIEWED", "BOG-ORD-021-001", null, BigDecimal.ZERO, false));
        List<EmployeeClaimSummaryResponse> items = List.of(claimSummary("BOG-CLM-021-001", "IN_REVIEW", "AT_RISK", false));
        return new EmployeeClaimPageResponse(items, page, size, items.size(), true, List.of("claimStatus", "slaState", "responsibleRole", "resolutionType", "financeStatus"));
    }

    @Override
    public EmployeeClaimDetailsResponse claimDetails(String userContext, String claimId, String supportReasonCode) {
        requireEmployee(userContext);
        if (!"BOG-CLM-021-001".equals(claimId) && !"BOG-CLM-021-002".equals(claimId)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_CLAIM_NOT_FOUND");
        }
        repository.save(snapshot(userContext, "CUST-021-001", supportReasonCode, "EMPLOYEE_CLAIM_DETAILS_VIEWED", "BOG-ORD-021-001", null, BigDecimal.ZERO, isSupervisor(userContext)));
        return claimDetailsResponse(userContext, claimId, supportReasonCode, List.of(routeTask("WAREHOUSE-021-001", "WAREHOUSE", "OPEN", "warehouse", null, "2026-04-28T08:00:00Z", null, "WAREHOUSE_REVIEW_REQUESTED")), "STR_MNEMO_EMPLOYEE_CLAIM_CREATED", "BOG-CLM-021-002".equals(claimId));
    }

    @Override
    public EmployeeClaimDetailsResponse transitionClaim(String userContext, String claimId, EmployeeClaimTransitionRequest request, String idempotencyKey) {
        requireEmployee(userContext);
        if (!"BOG-CLM-021-001".equals(claimId) && !"BOG-CLM-021-002".equals(claimId)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_CLAIM_NOT_FOUND");
        }
        if (request == null || blank(request.transitionCode()) || blank(request.supportReasonCode())) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_CLAIM_TRANSITION_INVALID", 400);
        }
        if ("APPROVE_COMPENSATION".equals(request.transitionCode()) && !isSupervisor(userContext)) {
            throw new EmployeeAccessDeniedException("STR_MNEMO_EMPLOYEE_ACCESS_DENIED");
        }
        List<EmployeeClaimRouteTaskResponse> tasks;
        String publicReason;
        if ("SEND_TO_FINANCE_REFUND".equals(request.transitionCode())) {
            tasks = List.of(
                    routeTask("WAREHOUSE-021-001", "WAREHOUSE", "COMPLETED", "warehouse", "warehouse-operator", "2026-04-28T08:00:00Z", "2026-04-27T10:20:00Z", "WAREHOUSE_CONFIRMED"),
                    routeTask("FINANCE-021-001", "FINANCE", "OPEN", "finance", null, "2026-04-28T12:00:00Z", null, "STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_FINANCE")
            );
            publicReason = "STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_FINANCE";
        } else if ("APPROVE_COMPENSATION".equals(request.transitionCode())) {
            tasks = List.of(routeTask("FINANCE-021-002", "FINANCE", "OPEN", "finance", null, "2026-04-28T12:00:00Z", null, "EMPLOYEE_CLAIM_SUPERVISOR_APPROVED"));
            publicReason = "STR_MNEMO_EMPLOYEE_CLAIM_SUPERVISOR_REQUIRED";
        } else {
            tasks = List.of(routeTask("WAREHOUSE-021-001", "WAREHOUSE", "OPEN", "warehouse", null, "2026-04-28T08:00:00Z", null, "STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_WAREHOUSE"));
            publicReason = "STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_WAREHOUSE";
        }
        repository.save(snapshot(userContext, "CUST-021-001", request.supportReasonCode(), "EMPLOYEE_CLAIM_TRANSITION_APPLIED", "BOG-ORD-021-001", null, request.approvedCompensationAmount() == null ? new BigDecimal("1250.00") : request.approvedCompensationAmount(), "APPROVE_COMPENSATION".equals(request.transitionCode())));
        return claimDetailsResponse(userContext, claimId, request.supportReasonCode(), tasks, publicReason, "BOG-CLM-021-002".equals(claimId));
    }

    @Override
    public EmployeePartnerCardResponse partnerCard(String userContext, String query, String supportReasonCode, String regionCode) {
        requireEmployee(userContext);
        if (blank(query) || query.trim().length() < 3) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_PARTNER_QUERY_INVALID", 400);
        }
        if (!matchesPartner(query)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_PARTNER_NOT_FOUND");
        }
        validatePartnerScope(userContext, regionCode);
        repository.save(snapshot(userContext, "CUST-022-001", supportReasonCode, "EMPLOYEE_PARTNER_CARD_VIEWED", "BOG-ORD-022-001", null, BigDecimal.ZERO, false));
        return partnerCardResponse(userContext, supportReasonCode);
    }

    @Override
    public EmployeePartnerCardResponse partnerCardById(String userContext, String partnerId, String supportReasonCode) {
        requireEmployee(userContext);
        if (!"PART-022-001".equals(partnerId)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_PARTNER_NOT_FOUND");
        }
        repository.save(snapshot(userContext, "CUST-022-001", supportReasonCode, "EMPLOYEE_PARTNER_CARD_VIEWED", "BOG-ORD-022-001", null, BigDecimal.ZERO, false));
        return partnerCardResponse(userContext, supportReasonCode);
    }

    @Override
    public EmployeePartnerOrderReportResponse partnerOrderReport(String userContext, String partnerId, String personNumber, String dateFrom, String dateTo, String campaignCode, String orderStatus, String paymentStatus, String deliveryStatus, boolean problemOnly, String regionCode, int page, int size, String sort) {
        requireEmployee(userContext);
        if ((blank(partnerId) && blank(personNumber)) || page < 0 || size < 1 || size > 100 || (!blank(dateFrom) && !blank(dateTo) && dateFrom.compareTo(dateTo) > 0)) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_PARTNER_REPORT_FILTER_INVALID", 400);
        }
        if (!blank(partnerId) && !"PART-022-001".equals(partnerId)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_PARTNER_NOT_FOUND");
        }
        if (!blank(personNumber) && !"P-022-7788".equals(personNumber)) {
            throw new EmployeeNotFoundException("STR_MNEMO_EMPLOYEE_PARTNER_NOT_FOUND");
        }
        validatePartnerScope(userContext, regionCode);
        repository.save(snapshot(userContext, "CUST-022-001", "EMPLOYEE_PARTNER_REPORT_VIEW", "EMPLOYEE_PARTNER_REPORT_VIEWED", "BOG-ORD-022-001", null, BigDecimal.ZERO, false));
        Map<String, String> filters = new java.util.LinkedHashMap<>();
        filters.put("partnerId", blank(partnerId) ? "PART-022-001" : partnerId);
        filters.put("personNumber", blank(personNumber) ? "P-022-7788" : personNumber);
        filters.put("campaignCode", blank(campaignCode) ? "2026-C06" : campaignCode);
        filters.put("problemOnly", Boolean.toString(problemOnly));
        filters.put("sort", blank(sort) ? "updatedAt,desc" : sort);
        List<EmployeePartnerOrderSummaryResponse> items = problemOnly
                ? List.of(partnerOrderSummary())
                : List.of(partnerOrderSummary(), partnerOrderSummaryDelivered());
        return new EmployeePartnerOrderReportResponse(items, partnerReportAggregates(), page, size, items.size(), true, filters);
    }

    private static EmployeePartnerCardResponse partnerCardResponse(String userContext, String supportReasonCode) {
        return new EmployeePartnerCardResponse(
                "PART-022-001",
                "P-022-7788",
                "Partner 022",
                "ACTIVE",
                "GROWING",
                "Business Partner",
                "RU-MOW",
                "P-016-1000",
                "+7 *** ***-22-22",
                "p***022@example.com",
                "2024-10-15",
                "2026-04-27",
                partnerKpi(),
                List.of(partnerOrderSummary(), partnerOrderSummaryDelivered()),
                List.of("OPEN_CLAIM", "DELIVERY_DELAY", "WMS_HOLD"),
                new EmployeeAuditContextResponse(actor(userContext), supportReasonCode, "BACKOFFICE", true),
                Map.of(
                        "orderHistory", "/employee/report/order-history?partnerId=PART-022-001&supportReasonCode=EMPLOYEE_PARTNER_CARD_VIEW",
                        "orderDetails", "/employee/order-history/BOG-ORD-022-001?partnerId=PART-022-001",
                        "claim", "/employee/claims-history/BOG-CLM-021-001?partnerId=PART-022-001",
                        "support", "/employee/order-support?orderNumber=BOG-ORD-022-001&partnerId=PART-022-001&supportReasonCode=EMPLOYEE_PARTNER_CARD_VIEW",
                        "bonusWallet", "/profile/transactions/finance/PART-022-001"
                )
        );
    }

    private static EmployeePartnerKpiResponse partnerKpi() {
        return new EmployeePartnerKpiResponse(
                new BigDecimal("88450.00"),
                new BigDecimal("342900.00"),
                12,
                new BigDecimal("10345.00"),
                new BigDecimal("15670.00"),
                28,
                1,
                2,
                new BigDecimal("3.20"),
                "2026-C06",
                "RUB"
        );
    }

    private static EmployeePartnerOrderSummaryResponse partnerOrderSummary() {
        return new EmployeePartnerOrderSummaryResponse(
                "ORD-022-001",
                "BOG-ORD-022-001",
                "2026-C06",
                "Customer 022",
                "ASSEMBLY_DELAY",
                PaymentStatus.PAID,
                DeliveryStatus.DELAYED,
                "WMS_HOLD",
                new BigDecimal("12450.00"),
                new BigDecimal("88.00"),
                "RUB",
                List.of("OPEN_CLAIM", "WMS_HOLD", "DELIVERY_DELAY"),
                Map.of(
                        "details", "/employee/order-history/BOG-ORD-022-001",
                        "claim", "/employee/claims-history/BOG-CLM-021-001",
                        "support", "/employee/order-support?orderNumber=BOG-ORD-022-001&partnerId=PART-022-001"
                ),
                "2026-04-27T10:00:00Z"
        );
    }

    private static EmployeePartnerOrderSummaryResponse partnerOrderSummaryDelivered() {
        return new EmployeePartnerOrderSummaryResponse(
                "ORD-022-002",
                "BOG-ORD-022-002",
                "2026-C05",
                "Customer 022",
                "DELIVERED",
                PaymentStatus.PAID,
                DeliveryStatus.CONFIRMED,
                "CLOSED",
                new BigDecimal("8390.00"),
                new BigDecimal("59.00"),
                "RUB",
                List.of(),
                Map.of(
                        "details", "/employee/order-history/BOG-ORD-022-002",
                        "support", "/employee/order-support?orderNumber=BOG-ORD-022-002&partnerId=PART-022-001"
                ),
                "2026-04-20T10:00:00Z"
        );
    }

    private static EmployeePartnerReportAggregateResponse partnerReportAggregates() {
        return new EmployeePartnerReportAggregateResponse(
                2,
                new BigDecimal("20840.00"),
                new BigDecimal("20840.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("10420.00"),
                new BigDecimal("88450.00"),
                new BigDecimal("342900.00"),
                1,
                1,
                "RUB"
        );
    }

    private static boolean matchesPartner(String query) {
        String normalized = query.trim().toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("p-022-7788")
                || normalized.contains("part-022-001")
                || normalized.contains("partner 022")
                || normalized.contains("022");
    }

    private static void validatePartnerScope(String userContext, String regionCode) {
        if (isSupervisor(userContext)) {
            return;
        }
        if (userContext != null && userContext.contains("regional-manager") && !blank(regionCode) && !"RU-MOW".equals(regionCode)) {
            throw new EmployeeAccessDeniedException("STR_MNEMO_EMPLOYEE_ACCESS_DENIED");
        }
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

    private static void validateClaimCreateRequest(EmployeeClaimCreateRequest request) {
        if (request == null || blank(request.supportReasonCode()) || blank(request.orderNumber()) || blank(request.requestedResolution()) || request.items() == null || request.items().isEmpty()) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED", 400);
        }
        if (blank(request.customerId()) && blank(request.partnerId())) {
            throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED", 400);
        }
        for (EmployeeClaimItemRequest item : request.items()) {
            if (blank(item.sku()) || blank(item.productCode()) || item.quantity() <= 0 || blank(item.problemType()) || blank(item.requestedResolution())) {
                throw new EmployeeValidationException("STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED", 400);
            }
        }
    }

    private static EmployeeClaimDetailsResponse claimDetailsResponse(String userContext, String claimId, String supportReasonCode, List<EmployeeClaimRouteTaskResponse> routeTasks, String publicReasonMnemonic, boolean supervisorRequired) {
        return new EmployeeClaimDetailsResponse(
                claimId,
                claimId,
                "BOG-ORD-021-001",
                "CUST-021-001",
                "PART-021-001",
                supervisorRequired ? "SUPERVISOR_APPROVAL" : "IN_REVIEW",
                "AT_RISK",
                "2026-04-28T08:00:00Z",
                "REFUND",
                "REFUND",
                supervisorRequired ? new BigDecimal("2500.00") : new BigDecimal("1250.00"),
                "RUB",
                publicReasonMnemonic,
                supervisorRequired,
                List.of(new EmployeeClaimItemResponse("SKU-021-001", "PRD-021-001", "Hydra Cream 021", 1, "DAMAGED_ITEM", "REFUND", "REFUND", supervisorRequired ? new BigDecimal("2500.00") : new BigDecimal("1250.00"))),
                List.of(new EmployeeClaimAttachmentResponse("ATT-021-001", "claim-photo.jpg", "image/jpeg", 512000L, actor(userContext), "2026-04-27T09:10:00Z", "INTERNAL")),
                routeTasks,
                List.of(
                        claimAudit("AUD-021-001", userContext, "EMPLOYEE_CLAIM_CREATED", supportReasonCode, "/employee/submit-claim"),
                        claimAudit("AUD-021-002", userContext, "EMPLOYEE_CLAIM_DETAILS_VIEWED", supportReasonCode, "/employee/claims-history/" + claimId),
                        claimAudit("AUD-021-003", userContext, supervisorRequired ? "EMPLOYEE_CLAIM_SUPERVISOR_APPROVED" : "EMPLOYEE_CLAIM_TRANSITION_APPLIED", supportReasonCode, "/employee/claims-history/" + claimId)
                ),
                List.of("SEND_TO_WAREHOUSE_REVIEW", "SEND_TO_FINANCE_REFUND", "SEND_TO_CUSTOMER_SUPPORT", "REQUEST_SUPERVISOR_APPROVAL")
        );
    }

    private static EmployeeClaimSummaryResponse claimSummary(String claimId, String status, String slaState, boolean supervisorRequired) {
        return new EmployeeClaimSummaryResponse(
                claimId,
                claimId,
                "BOG-ORD-021-001",
                "Customer 021 / Partner 021",
                "masked +7 *** ***-21-21",
                status,
                slaState,
                "2026-04-28T08:00:00Z",
                "REFUND",
                supervisorRequired ? new BigDecimal("2500.00") : new BigDecimal("1250.00"),
                "RUB",
                "employee-support",
                supervisorRequired ? "supervisor" : "employee-support",
                "2026-04-27T09:10:00Z",
                List.of("OPEN", "ROUTE", "APPROVE")
        );
    }

    private static EmployeeClaimRouteTaskResponse routeTask(String taskId, String taskType, String status, String assigneeRole, String assigneeId, String dueAt, String completedAt, String resultCode) {
        return new EmployeeClaimRouteTaskResponse(taskId, taskType, status, assigneeRole, assigneeId, dueAt, completedAt, resultCode);
    }

    private static EmployeeClaimAuditEventResponse claimAudit(String id, String userContext, String actionType, String supportReasonCode, String sourceRoute) {
        return new EmployeeClaimAuditEventResponse(id, actor(userContext), isSupervisor(userContext) ? "supervisor" : "employee-support", actionType, supportReasonCode, sourceRoute, "CORR-021", "2026-04-27T09:10:00Z");
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
        return userContext != null && (userContext.contains("employee-support") || userContext.contains("order-support") || userContext.contains("backoffice") || userContext.contains("regional-manager"));
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
