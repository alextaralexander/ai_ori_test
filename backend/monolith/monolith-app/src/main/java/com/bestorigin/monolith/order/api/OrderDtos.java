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

    public enum ClaimStatus {
        DRAFT,
        SUBMITTED,
        IN_REVIEW,
        WAREHOUSE_CHECK,
        APPROVED,
        PARTIALLY_APPROVED,
        REJECTED,
        CLOSED
    }

    public enum ClaimResolution {
        REFUND,
        REPLACEMENT,
        MISSING_ITEM,
        SERVICE_REVIEW,
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

    public record OrderClaimItemRequest(
            String sku,
            int quantity
    ) {
    }

    public record OrderClaimCreateRequest(
            String orderNumber,
            String reasonCode,
            ClaimResolution requestedResolution,
            String comment,
            List<OrderClaimItemRequest> items
    ) {
    }

    public record OrderClaimCommentRequest(String comment) {
    }

    public record OrderClaimPageResponse(
            List<OrderClaimSummaryResponse> items,
            int page,
            int size,
            long totalElements,
            boolean hasNext
    ) {
    }

    public record OrderClaimSummaryResponse(
            String claimId,
            String orderNumber,
            ClaimStatus status,
            ClaimResolution requestedResolution,
            BigDecimal refundAmount,
            String currencyCode,
            String updatedAt,
            boolean partnerImpact
    ) {
    }

    public record OrderClaimDetailsResponse(
            String claimId,
            String orderNumber,
            ClaimStatus status,
            ClaimResolution requestedResolution,
            ClaimResolution approvedResolution,
            BigDecimal refundAmount,
            String currencyCode,
            String publicReasonMnemo,
            boolean partnerImpact,
            Boolean auditRecorded,
            BigDecimal businessVolumeDelta,
            List<OrderClaimLineResponse> items,
            List<OrderClaimEventResponse> events,
            List<OrderClaimCommentResponse> comments,
            List<OrderClaimAttachmentResponse> attachments,
            String nextAction
    ) {
    }

    public record OrderClaimLineResponse(
            String productCode,
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal refundAmount,
            ClaimResolution requestedResolution,
            ClaimResolution approvedResolution,
            String status,
            String reasonMnemo
    ) {
    }

    public record OrderClaimEventResponse(
            String eventType,
            String publicStatus,
            String sourceSystem,
            String descriptionMnemo,
            String occurredAt
    ) {
    }

    public record OrderClaimCommentResponse(
            String authorRole,
            String visibility,
            String messageMnemo,
            String createdAt
    ) {
    }

    public record OrderClaimAttachmentResponse(
            String attachmentId,
            String fileName,
            String contentType,
            long sizeBytes
    ) {
    }

    public record ErrorResponse(
            String code,
            List<ValidationReasonResponse> details,
            Map<String, String> metadata
    ) {
    }
}
