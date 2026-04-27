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

    public record ErrorResponse(
            String code,
            List<ValidationReasonResponse> details,
            Map<String, String> metadata
    ) {
    }
}
