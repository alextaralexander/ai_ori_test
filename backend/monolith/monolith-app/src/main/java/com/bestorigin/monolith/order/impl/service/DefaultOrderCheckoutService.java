package com.bestorigin.monolith.order.impl.service;

import com.bestorigin.monolith.order.api.OrderDtos.AddressRequest;
import com.bestorigin.monolith.order.api.OrderDtos.BenefitApplyRequest;
import com.bestorigin.monolith.order.api.OrderDtos.BenefitResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutDraftResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutItemResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutStatus;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutTotalsResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutType;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutValidationResponse;
import com.bestorigin.monolith.order.api.OrderDtos.ConfirmCheckoutRequest;
import com.bestorigin.monolith.order.api.OrderDtos.DeliveryOptionResponse;
import com.bestorigin.monolith.order.api.OrderDtos.DeliverySelectionRequest;
import com.bestorigin.monolith.order.api.OrderDtos.NextAction;
import com.bestorigin.monolith.order.api.OrderDtos.OrderConfirmationResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderActionsResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderDeliveryResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderDetailsResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderHistoryEventResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderHistoryItemResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderHistoryLineResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderHistoryPageResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderPaymentResponse;
import com.bestorigin.monolith.order.api.OrderDtos.PaymentResponse;
import com.bestorigin.monolith.order.api.OrderDtos.PaymentSelectionRequest;
import com.bestorigin.monolith.order.api.OrderDtos.PaymentStatus;
import com.bestorigin.monolith.order.api.OrderDtos.RecipientRequest;
import com.bestorigin.monolith.order.api.OrderDtos.RepeatOrderResponse;
import com.bestorigin.monolith.order.api.OrderDtos.RepeatOrderStatus;
import com.bestorigin.monolith.order.api.OrderDtos.StartCheckoutRequest;
import com.bestorigin.monolith.order.api.OrderDtos.ValidationReasonResponse;
import com.bestorigin.monolith.order.domain.OrderCheckoutRepository;
import com.bestorigin.monolith.order.domain.OrderCheckoutSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultOrderCheckoutService implements OrderCheckoutService {

    private static final String CAMPAIGN = "CMP-2026-05";
    private static final String FORBIDDEN = "STR_MNEMO_ORDER_CHECKOUT_FORBIDDEN";
    private static final String CART_INVALID = "STR_MNEMO_ORDER_CHECKOUT_CART_INVALID";
    private static final String CONTACT_INVALID = "STR_MNEMO_ORDER_CONTACT_INVALID";
    private static final String DELIVERY_UNAVAILABLE = "STR_MNEMO_ORDER_DELIVERY_UNAVAILABLE";
    private static final String PARTIAL_RESERVE = "STR_MNEMO_ORDER_PARTIAL_RESERVE";
    private static final String PAYMENT_FAILED = "STR_MNEMO_ORDER_PAYMENT_FAILED";
    private static final String VERSION_CONFLICT = "STR_MNEMO_ORDER_CHECKOUT_VERSION_CONFLICT";
    private static final String HISTORY_ACCESS_DENIED = "STR_MNEMO_ORDER_HISTORY_ACCESS_DENIED";
    private static final String PAYMENT_PENDING = "STR_MNEMO_ORDER_PAYMENT_PENDING";
    private static final String ORDER_CREATED = "STR_MNEMO_ORDER_CREATED";
    private static final String PAYMENT_PAID = "STR_MNEMO_PAYMENT_PAID";
    private static final String DELIVERY_IN_TRANSIT = "STR_MNEMO_DELIVERY_IN_TRANSIT";
    private static final String REPEAT_PARTIAL = "STR_MNEMO_ORDER_REPEAT_PARTIAL";

    private final OrderCheckoutRepository repository;
    private final ConcurrentMap<String, OrderConfirmationResponse> confirmationsByKey = new ConcurrentHashMap<>();

    public DefaultOrderCheckoutService(OrderCheckoutRepository repository) {
        this.repository = repository;
    }

    @Override
    public synchronized CheckoutDraftResponse start(String userContextId, StartCheckoutRequest request, String idempotencyKey) {
        if (request == null || request.cartId() == null || request.cartId().isBlank()) {
            throw new OrderCheckoutValidationException(CART_INVALID, 400);
        }
        CheckoutType type = request.checkoutType() == null ? CheckoutType.MAIN : request.checkoutType();
        if (request.cartId().contains("INVALID")) {
            throw new OrderCheckoutValidationException(CART_INVALID, 400);
        }
        if (type == CheckoutType.SUPPLEMENTARY && !role(userContextId).equals("partner")) {
            throw new OrderCheckoutValidationException("STR_MNEMO_SUPPLEMENTARY_ORDER_FORBIDDEN", 403);
        }
        OrderCheckoutSnapshot checkout = repository.findByContext(userContextId, request.cartId(), type.name())
                .orElseGet(() -> seedCheckout(userContextId, request.cartId(), type));
        checkout.status(CheckoutStatus.READY_TO_CONFIRM);
        repository.save(checkout);
        return response(checkout);
    }

    @Override
    public CheckoutDraftResponse get(String userContextId, UUID checkoutId) {
        return response(owned(userContextId, checkoutId));
    }

    @Override
    public synchronized CheckoutDraftResponse updateRecipient(String userContextId, UUID checkoutId, RecipientRequest request) {
        OrderCheckoutSnapshot checkout = owned(userContextId, checkoutId);
        if (request == null || blank(request.fullName()) || blank(request.phone()) || request.phone().length() < 8) {
            checkout.status(CheckoutStatus.VALIDATION_REQUIRED);
            throw new OrderCheckoutValidationException(CONTACT_INVALID, 400);
        }
        checkout.recipient(request);
        checkout.status(CheckoutStatus.READY_TO_CONFIRM);
        repository.save(checkout);
        return response(checkout);
    }

    @Override
    public synchronized CheckoutDraftResponse updateAddress(String userContextId, UUID checkoutId, AddressRequest request) {
        OrderCheckoutSnapshot checkout = owned(userContextId, checkoutId);
        checkout.address(request);
        checkout.deliveryOptions().clear();
        checkout.deliveryOptions().addAll(deliveryOptions(checkout.checkoutType()));
        repository.save(checkout);
        return response(checkout);
    }

    @Override
    public synchronized CheckoutDraftResponse selectDelivery(String userContextId, UUID checkoutId, DeliverySelectionRequest request) {
        OrderCheckoutSnapshot checkout = owned(userContextId, checkoutId);
        String code = request == null || blank(request.deliveryMethodCode()) ? "COURIER" : request.deliveryMethodCode();
        DeliveryOptionResponse selected = deliveryOptions(checkout.checkoutType()).stream()
                .filter(option -> option.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new OrderCheckoutValidationException(DELIVERY_UNAVAILABLE, 400));
        if (!selected.available()) {
            throw new OrderCheckoutValidationException(DELIVERY_UNAVAILABLE, 400);
        }
        checkout.selectedDelivery(selected);
        recalculate(checkout);
        repository.save(checkout);
        return response(checkout);
    }

    @Override
    public synchronized CheckoutDraftResponse selectPayment(String userContextId, UUID checkoutId, PaymentSelectionRequest request, String idempotencyKey) {
        OrderCheckoutSnapshot checkout = owned(userContextId, checkoutId);
        String method = request == null || blank(request.paymentMethodCode()) ? "ONLINE_CARD" : request.paymentMethodCode();
        PaymentStatus status = checkout.cartId().contains("PAYMENT-FAILED") ? PaymentStatus.FAILED : PaymentStatus.PENDING;
        checkout.selectedPayment(new PaymentResponse(method, "PAY-" + checkout.id(), status, checkout.totals().grandTotalAmount()));
        repository.save(checkout);
        return response(checkout);
    }

    @Override
    public synchronized CheckoutDraftResponse applyBenefits(String userContextId, UUID checkoutId, BenefitApplyRequest request, String idempotencyKey) {
        OrderCheckoutSnapshot checkout = owned(userContextId, checkoutId);
        checkout.benefits().clear();
        BigDecimal wallet = money(request == null || request.walletAmount() == null ? "0.00" : request.walletAmount().toPlainString());
        BigDecimal cashback = money(request == null || request.cashbackAmount() == null ? "0.00" : request.cashbackAmount().toPlainString());
        if (wallet.signum() > 0) {
            checkout.benefits().add(new BenefitResponse("WALLET", "WALLET", wallet.min(money("500.00")), "APPLIED", null));
        }
        if (cashback.signum() > 0) {
            checkout.benefits().add(new BenefitResponse("CASHBACK", "CASHBACK", cashback.min(money("100.00")), "APPLIED", null));
        }
        if (request != null && request.benefitCodes() != null) {
            request.benefitCodes().forEach(code -> checkout.benefits().add(new BenefitResponse("CATALOG_DISCOUNT", code, money("100.00"), "APPLIED", null)));
        }
        recalculate(checkout);
        repository.save(checkout);
        return response(checkout);
    }

    @Override
    public CheckoutValidationResponse validate(String userContextId, UUID checkoutId) {
        return validation(owned(userContextId, checkoutId));
    }

    @Override
    public synchronized OrderConfirmationResponse confirm(String userContextId, UUID checkoutId, ConfirmCheckoutRequest request, String idempotencyKey) {
        String key = idempotencyKey == null || idempotencyKey.isBlank() ? userContextId + "-" + checkoutId : idempotencyKey;
        OrderConfirmationResponse existing = confirmationsByKey.get(key);
        if (existing != null) {
            return existing;
        }
        OrderCheckoutSnapshot checkout = owned(userContextId, checkoutId);
        if (request != null && request.checkoutVersion() > checkout.version()) {
            throw new OrderCheckoutValidationException(VERSION_CONFLICT, 409);
        }
        if (checkout.cartId().contains("PARTIAL")) {
            throw new OrderCheckoutValidationException(PARTIAL_RESERVE, 409);
        }
        if (checkout.selectedPayment() == null) {
            checkout.selectedPayment(new PaymentResponse("ONLINE_CARD", "PAY-" + checkout.id(), checkout.cartId().contains("PAYMENT-FAILED") ? PaymentStatus.FAILED : PaymentStatus.PENDING, checkout.totals().grandTotalAmount()));
        }
        PaymentStatus paymentStatus = checkout.selectedPayment().paymentStatus();
        String orderNumber = checkout.orderNumber() == null ? "ORD-" + checkout.checkoutType().name().substring(0, 1) + "-" + Math.abs(checkout.id().hashCode()) : checkout.orderNumber();
        checkout.orderNumber(orderNumber);
        checkout.status(CheckoutStatus.CONFIRMED);
        OrderConfirmationResponse response = new OrderConfirmationResponse(
                orderNumber,
                checkout.checkoutType(),
                paymentStatus == PaymentStatus.FAILED ? "PAYMENT_FAILED" : "CREATED",
                paymentStatus,
                "RESERVED",
                checkout.selectedPayment().paymentSessionId(),
                checkout.totals(),
                paymentStatus == PaymentStatus.FAILED ? NextAction.FIX_CHECKOUT : NextAction.PAYMENT_REDIRECT,
                paymentStatus == PaymentStatus.FAILED ? List.of(reason(PAYMENT_FAILED, "BLOCKING", "payment")) : List.of()
        );
        confirmationsByKey.put(key, response);
        repository.save(checkout);
        return response;
    }

    @Override
    public OrderConfirmationResponse getOrder(String userContextId, String orderNumber) {
        return confirmationsByKey.values().stream()
                .filter(order -> order.orderNumber().equals(orderNumber))
                .findFirst()
                .orElseThrow(() -> new OrderCheckoutNotFoundException("STR_MNEMO_ORDER_NOT_FOUND"));
    }

    @Override
    public OrderHistoryPageResponse searchOrderHistory(String userContextId, String query, String campaignId, String orderType, int page, int size) {
        List<OrderHistoryItemResponse> orders = seededHistory(userContextId).stream()
                .filter(order -> blank(query) || order.orderNumber().contains(query) || order.summaryItems().stream().anyMatch(item -> item.productName().toLowerCase().contains(query.toLowerCase()) || item.sku().contains(query)))
                .filter(order -> blank(campaignId) || order.campaignId().equals(campaignId))
                .filter(order -> blank(orderType) || order.orderType().name().equals(orderType))
                .toList();
        return new OrderHistoryPageResponse(orders, Math.max(page, 0), Math.max(size, 1), orders.size(), false);
    }

    @Override
    public OrderDetailsResponse getOrderHistoryDetails(String userContextId, String orderNumber, String supportCustomerId, String reason) {
        if ("ORD-011-OTHER".equals(orderNumber) && !role(userContextId).equals("order-support")) {
            throw new OrderCheckoutAccessDeniedException(HISTORY_ACCESS_DENIED);
        }
        OrderHistoryItemResponse item = seededHistory(userContextId).stream()
                .filter(order -> order.orderNumber().equals(orderNumber))
                .findFirst()
                .orElseGet(() -> "ORD-011-PAY".equals(orderNumber) ? pendingPaymentOrder() : null);
        if (item == null && role(userContextId).equals("order-support") && "ORD-011-MAIN".equals(orderNumber)) {
            item = mainOrder();
        }
        if (item == null) {
            throw new OrderCheckoutNotFoundException("STR_MNEMO_ORDER_NOT_FOUND");
        }
        boolean support = role(userContextId).equals("order-support");
        boolean pending = item.paymentStatus() == PaymentStatus.PENDING;
        List<ValidationReasonResponse> warnings = pending ? List.of(reason(PAYMENT_PENDING, "WARNING", "payment")) : item.warnings();
        return new OrderDetailsResponse(
                item.orderNumber(),
                item.orderType(),
                item.campaignId(),
                item.createdAt(),
                item.orderStatus(),
                item.paymentStatus(),
                item.deliveryStatus(),
                item.grandTotalAmount(),
                item.currencyCode(),
                item.summaryItems(),
                totals(item.grandTotalAmount()),
                delivery(item.orderType()),
                new OrderPaymentResponse("ONLINE_CARD", item.paymentStatus(), pending ? item.grandTotalAmount() : BigDecimal.ZERO, pending ? BigDecimal.ZERO : item.grandTotalAmount(), pending),
                events(item.paymentStatus()),
                warnings,
                new OrderActionsResponse(pending, true, item.orderType() == CheckoutType.MAIN),
                support,
                item.orderType() == CheckoutType.SUPPLEMENTARY ? money("23.50") : null
        );
    }

    @Override
    public RepeatOrderResponse repeatOrder(String userContextId, String orderNumber, String idempotencyKey) {
        OrderDetailsResponse details = getOrderHistoryDetails(userContextId, orderNumber, null, null);
        List<OrderHistoryLineResponse> added = details.items().stream().filter(OrderHistoryLineResponse::repeatAvailable).toList();
        List<OrderHistoryLineResponse> rejected = details.items().stream().filter(item -> !item.repeatAvailable()).toList();
        return new RepeatOrderResponse(rejected.isEmpty() ? RepeatOrderStatus.COMPLETED : RepeatOrderStatus.PARTIAL, details.orderType(), added, rejected, rejected.isEmpty() ? null : REPEAT_PARTIAL);
    }

    private OrderCheckoutSnapshot owned(String userContextId, UUID checkoutId) {
        OrderCheckoutSnapshot checkout = repository.findById(checkoutId)
                .orElseThrow(() -> new OrderCheckoutAccessDeniedException(FORBIDDEN));
        if (!checkout.ownerUserId().equals(userContextId) && !role(userContextId).equals("order-support")) {
            throw new OrderCheckoutAccessDeniedException(FORBIDDEN);
        }
        return checkout;
    }

    private OrderCheckoutSnapshot seedCheckout(String userContextId, String cartId, CheckoutType type) {
        UUID id = UUID.nameUUIDFromBytes((userContextId + cartId + type).getBytes(StandardCharsets.UTF_8));
        OrderCheckoutSnapshot checkout = new OrderCheckoutSnapshot(id, userContextId, cartId, type, CAMPAIGN);
        checkout.items().addAll(items(cartId, type));
        checkout.deliveryOptions().addAll(deliveryOptions(type));
        checkout.recipient(new RecipientRequest("SELF", role(userContextId).equals("partner") ? "Partner 010" : "Customer 010", role(userContextId).equals("partner") ? "+79990000011" : "+79990000010", role(userContextId) + "010@example.com"));
        checkout.address(type == CheckoutType.SUPPLEMENTARY
                ? new AddressRequest("PICKUP_POINT", null, "PICKUP-010-01", null, null, null, null, null, null, null)
                : new AddressRequest("ADDRESS", "ADDR-010-MAIN", null, "RU", "Москва", "Москва", "Тверская", "10", null, "101000"));
        checkout.selectedDelivery(deliveryOptions(type).get(0));
        recalculate(checkout);
        return repository.save(checkout);
    }

    private static List<CheckoutItemResponse> items(String cartId, CheckoutType type) {
        if (cartId.contains("PARTIAL")) {
            return List.of(new CheckoutItemResponse("BOG-LIMITED-010", "Лимитированный крем Best Ori Gin", 5, money("1290.00"), money("6450.00"), "PARTIALLY_AVAILABLE", "PARTIAL", PARTIAL_RESERVE));
        }
        if (type == CheckoutType.SUPPLEMENTARY) {
            return List.of(new CheckoutItemResponse("BOG-SERUM-002", "Сыворотка Best Ori Gin", 3, money("1390.00"), money("4170.00"), "AVAILABLE", "RESERVED", null));
        }
        return List.of(new CheckoutItemResponse("BOG-CREAM-001", "Увлажняющий крем Best Ori Gin", 2, money("990.00"), money("1980.00"), "AVAILABLE", "RESERVED", null));
    }

    private static List<OrderHistoryItemResponse> seededHistory(String userContextId) {
        if (role(userContextId).equals("partner")) {
            return List.of(supplementaryOrder(), mainOrder());
        }
        return List.of(mainOrder(), pendingPaymentOrder());
    }

    private static OrderHistoryItemResponse mainOrder() {
        return new OrderHistoryItemResponse(
                "ORD-011-MAIN",
                CheckoutType.MAIN,
                CAMPAIGN,
                "2026-04-12T09:10:00Z",
                "CREATED",
                PaymentStatus.PAID,
                "IN_TRANSIT",
                money("8450.00"),
                "RUB",
                List.of(
                        new OrderHistoryLineResponse("BOG-CREAM-001", "100-011", "Увлажняющий крем Best Ori Gin", 2, money("3200.00"), money("0.00"), money("6400.00"), false, true, true, null),
                        new OrderHistoryLineResponse("BOG-GIFT-011", "GIFT-011", "Подарочный набор", 1, money("0.00"), money("0.00"), money("0.00"), true, false, false, REPEAT_PARTIAL)
                ),
                List.of(reason(REPEAT_PARTIAL, "WARNING", "repeat"))
        );
    }

    private static OrderHistoryItemResponse pendingPaymentOrder() {
        return new OrderHistoryItemResponse(
                "ORD-011-PAY",
                CheckoutType.MAIN,
                CAMPAIGN,
                "2026-04-13T11:00:00Z",
                "PAYMENT_PENDING",
                PaymentStatus.PENDING,
                "CREATED",
                money("4210.00"),
                "RUB",
                List.of(new OrderHistoryLineResponse("BOG-SERUM-002", "200-011", "Сыворотка Best Ori Gin", 1, money("4210.00"), money("0.00"), money("4210.00"), false, true, true, null)),
                List.of(reason(PAYMENT_PENDING, "WARNING", "payment"))
        );
    }

    private static OrderHistoryItemResponse supplementaryOrder() {
        return new OrderHistoryItemResponse(
                "ORD-011-SUPP",
                CheckoutType.SUPPLEMENTARY,
                CAMPAIGN,
                "2026-04-14T12:30:00Z",
                "ASSEMBLY_PENDING",
                PaymentStatus.PAID,
                "ASSEMBLY",
                money("11300.00"),
                "RUB",
                List.of(new OrderHistoryLineResponse("BOG-SERUM-002", "SUPP-011", "Партнерская сыворотка Best Ori Gin", 3, money("1390.00"), money("250.00"), money("3920.00"), false, true, false, null)),
                List.of()
        );
    }

    private static OrderDeliveryResponse delivery(CheckoutType type) {
        return type == CheckoutType.SUPPLEMENTARY
                ? new OrderDeliveryResponse("PICKUP_POINT", "Partner 011", "+7 *** *** 00 12", "Москва", "ПВЗ 011", "1-2 дня", "TRK-011-SUPP")
                : new OrderDeliveryResponse("ADDRESS", "Customer 011", "+7 *** *** 00 11", "Москва", "Москва, Тверская, 10", "2-4 дня", "TRK-011-MAIN");
    }

    private static List<OrderHistoryEventResponse> events(PaymentStatus paymentStatus) {
        List<OrderHistoryEventResponse> events = new ArrayList<>();
        events.add(new OrderHistoryEventResponse("ORDER_CREATED", "CREATED", "checkout", ORDER_CREATED, "2026-04-12T09:10:00Z"));
        if (paymentStatus == PaymentStatus.PAID) {
            events.add(new OrderHistoryEventResponse("PAYMENT_PAID", "PAID", "payment", PAYMENT_PAID, "2026-04-12T09:14:00Z"));
            events.add(new OrderHistoryEventResponse("DELIVERY_IN_TRANSIT", "IN_TRANSIT", "logistics", DELIVERY_IN_TRANSIT, "2026-04-14T10:20:00Z"));
        }
        return events;
    }

    private static CheckoutTotalsResponse totals(BigDecimal grandTotal) {
        return new CheckoutTotalsResponse(grandTotal, money("0.00"), money("0.00"), money("0.00"), money("0.00"), grandTotal);
    }

    private static List<DeliveryOptionResponse> deliveryOptions(CheckoutType type) {
        if (type == CheckoutType.SUPPLEMENTARY) {
            return List.of(
                    new DeliveryOptionResponse("PICKUP", "Пункт выдачи", true, money("0.00"), "1-2 дня", null),
                    new DeliveryOptionResponse("COURIER", "Курьер", true, money("390.00"), "2-4 дня", null)
            );
        }
        return List.of(
                new DeliveryOptionResponse("COURIER", "Курьер", true, money("390.00"), "2-4 дня", null),
                new DeliveryOptionResponse("PICKUP", "Пункт выдачи", true, money("0.00"), "1-2 дня", null)
        );
    }

    private CheckoutDraftResponse response(OrderCheckoutSnapshot checkout) {
        return new CheckoutDraftResponse(
                checkout.id(),
                checkout.checkoutType(),
                checkout.cartId(),
                checkout.campaignId(),
                checkout.status(),
                checkout.version(),
                checkout.recipient(),
                checkout.address(),
                List.copyOf(checkout.deliveryOptions()),
                checkout.selectedDelivery(),
                checkout.selectedPayment(),
                List.copyOf(checkout.benefits()),
                List.copyOf(checkout.items()),
                checkout.totals(),
                validation(checkout)
        );
    }

    private CheckoutValidationResponse validation(OrderCheckoutSnapshot checkout) {
        List<ValidationReasonResponse> reasons = new ArrayList<>();
        if (checkout.recipient() == null || blank(checkout.recipient().phone())) {
            reasons.add(reason(CONTACT_INVALID, "BLOCKING", "recipient"));
        }
        checkout.items().stream()
                .filter(item -> item.blockingReasonMnemo() != null)
                .map(item -> reason(item.blockingReasonMnemo(), "BLOCKING", item.productCode()))
                .forEach(reasons::add);
        return new CheckoutValidationResponse(reasons.isEmpty(), reasons);
    }

    private void recalculate(OrderCheckoutSnapshot checkout) {
        BigDecimal subtotal = checkout.items().stream().map(CheckoutItemResponse::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal delivery = checkout.selectedDelivery() == null ? BigDecimal.ZERO : checkout.selectedDelivery().price();
        BigDecimal wallet = checkout.benefits().stream().filter(b -> "WALLET".equals(b.benefitType())).map(BenefitResponse::appliedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cashback = checkout.benefits().stream().filter(b -> "CASHBACK".equals(b.benefitType())).map(BenefitResponse::appliedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = checkout.benefits().stream().filter(b -> !"WALLET".equals(b.benefitType()) && !"CASHBACK".equals(b.benefitType())).map(BenefitResponse::appliedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grand = subtotal.add(delivery).subtract(wallet).subtract(cashback).subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        checkout.totals(new CheckoutTotalsResponse(subtotal, delivery, discount, wallet, cashback, grand));
    }

    private static ValidationReasonResponse reason(String code, String severity, String target) {
        return new ValidationReasonResponse(code, severity, target);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String role(String userContextId) {
        String value = userContextId == null ? "" : userContextId.toLowerCase();
        if (value.contains("partner")) {
            return "partner";
        }
        if (value.contains("support")) {
            return "order-support";
        }
        return "customer";
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }
}
