package com.bestorigin.monolith.order.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OrderDtos {

    private OrderDtos() {
    }

    public enum CheckoutType {
        MAIN,
        SUPPLEMENTARY
    }

    public enum CheckoutStatus {
        DRAFT,
        VALIDATION_REQUIRED,
        READY_TO_CONFIRM,
        CONFIRMED,
        BLOCKED,
        EXPIRED
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED,
        EXPIRED,
        CANCELLED
    }

    public enum NextAction {
        PAYMENT_REDIRECT,
        WAIT_PAYMENT,
        ORDER_DETAILS,
        FIX_CHECKOUT
    }

    public enum RepeatOrderStatus {
        COMPLETED,
        PARTIAL,
        REJECTED
    }

    public record StartCheckoutRequest(
            String cartId,
            CheckoutType checkoutType,
            Boolean vipMode,
            Boolean superOrderMode
    ) {
    }

    public record RecipientRequest(
            String recipientType,
            String fullName,
            String phone,
            String email
    ) {
    }

    public record AddressRequest(
            String deliveryTargetType,
            String addressId,
            String pickupPointId,
            String country,
            String region,
            String city,
            String street,
            String house,
            String apartment,
            String postalCode
    ) {
    }

    public record DeliverySelectionRequest(String deliveryMethodCode) {
    }

    public record PaymentSelectionRequest(String paymentMethodCode) {
    }

    public record BenefitApplyRequest(
            BigDecimal walletAmount,
            BigDecimal cashbackAmount,
            List<String> benefitCodes
    ) {
    }

    public record ConfirmCheckoutRequest(long checkoutVersion) {
    }

    public record CheckoutDraftResponse(
            UUID id,
            CheckoutType checkoutType,
            String cartId,
            String campaignId,
            CheckoutStatus status,
            long version,
            RecipientRequest recipient,
            AddressRequest address,
            List<DeliveryOptionResponse> deliveryOptions,
            DeliveryOptionResponse selectedDelivery,
            PaymentResponse selectedPayment,
            List<BenefitResponse> benefits,
            List<CheckoutItemResponse> items,
            CheckoutTotalsResponse totals,
            CheckoutValidationResponse validation
    ) {
    }

    public record DeliveryOptionResponse(
            String code,
            String name,
            boolean available,
            BigDecimal price,
            String estimatedInterval,
            String reasonMnemo
    ) {
    }

    public record PaymentResponse(
            String paymentMethodCode,
            String paymentSessionId,
            PaymentStatus paymentStatus,
            BigDecimal amountToPay
    ) {
    }

    public record BenefitResponse(
            String benefitType,
            String benefitCode,
            BigDecimal appliedAmount,
            String status,
            String reasonMnemo
    ) {
    }

    public record CheckoutItemResponse(
            String productCode,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            String availabilityStatus,
            String reserveStatus,
            String blockingReasonMnemo
    ) {
    }

    public record CheckoutTotalsResponse(
            BigDecimal subtotalAmount,
            BigDecimal deliveryAmount,
            BigDecimal discountAmount,
            BigDecimal walletAmount,
            BigDecimal cashbackAmount,
            BigDecimal grandTotalAmount
    ) {
    }

    public record CheckoutValidationResponse(
            boolean valid,
            List<ValidationReasonResponse> reasons
    ) {
    }

    public record ValidationReasonResponse(
            String code,
            String severity,
            String target
    ) {
    }

    public record OrderConfirmationResponse(
            String orderNumber,
            CheckoutType orderType,
            String status,
            PaymentStatus paymentStatus,
            String deliveryStatus,
            String paymentSessionId,
            CheckoutTotalsResponse totals,
            NextAction nextAction,
            List<ValidationReasonResponse> reasons
    ) {
    }

    public record OrderHistoryPageResponse(
            List<OrderHistoryItemResponse> items,
            int page,
            int size,
            long totalElements,
            boolean hasNext
    ) {
    }

    public record OrderHistoryItemResponse(
            String orderNumber,
            CheckoutType orderType,
            String campaignId,
            String createdAt,
            String orderStatus,
            PaymentStatus paymentStatus,
            String deliveryStatus,
            BigDecimal grandTotalAmount,
            String currencyCode,
            List<OrderHistoryLineResponse> summaryItems,
            List<ValidationReasonResponse> warnings
    ) {
    }

    public record OrderDetailsResponse(
            String orderNumber,
            CheckoutType orderType,
            String campaignId,
            String createdAt,
            String orderStatus,
            PaymentStatus paymentStatus,
            String deliveryStatus,
            BigDecimal grandTotalAmount,
            String currencyCode,
            List<OrderHistoryLineResponse> items,
            CheckoutTotalsResponse totals,
            OrderDeliveryResponse delivery,
            OrderPaymentResponse payment,
            List<OrderHistoryEventResponse> events,
            List<ValidationReasonResponse> warnings,
            OrderActionsResponse actions,
            Boolean auditRecorded,
            BigDecimal businessVolume
    ) {
    }

    public record OrderHistoryLineResponse(
            String productCode,
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal discountAmount,
            BigDecimal totalPrice,
            boolean gift,
            boolean repeatAvailable,
            boolean claimAvailable,
            String limitationReasonMnemo
    ) {
    }

    public record OrderDeliveryResponse(
            String deliveryTargetType,
            String maskedRecipientName,
            String maskedPhone,
            String city,
            String addressLine,
            String expectedInterval,
            String trackingNumber
    ) {
    }

    public record OrderPaymentResponse(
            String paymentMethodCode,
            PaymentStatus paymentStatus,
            BigDecimal amountToPay,
            BigDecimal paidAmount,
            boolean paymentActionAvailable
    ) {
    }

    public record OrderHistoryEventResponse(
            String eventType,
            String publicStatus,
            String sourceSystem,
            String descriptionMnemo,
            String occurredAt
    ) {
    }

    public record OrderActionsResponse(
            boolean paymentAvailable,
            boolean repeatOrderAvailable,
            boolean claimAvailable
    ) {
    }

    public record RepeatOrderResponse(
            RepeatOrderStatus status,
            CheckoutType targetCartType,
            List<OrderHistoryLineResponse> addedItems,
            List<OrderHistoryLineResponse> rejectedItems,
            String reasonMnemo
    ) {
    }

    public record ErrorResponse(
            String code,
            List<ValidationReasonResponse> details,
            Map<String, String> metadata
    ) {
    }
}
