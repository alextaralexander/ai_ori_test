package com.bestorigin.monolith.cart.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CartDtos {

    private CartDtos() {
    }

    public enum CartType {
        MAIN,
        SUPPLEMENTARY
    }

    public enum CartStatus {
        ACTIVE,
        BLOCKED,
        READY_FOR_CHECKOUT,
        ARCHIVED
    }

    public enum RoleSegment {
        CUSTOMER,
        PARTNER,
        SUPPORT
    }

    public enum AvailabilityStatus {
        AVAILABLE,
        LOW_STOCK,
        RESERVED,
        PARTIALLY_AVAILABLE,
        UNAVAILABLE,
        REMOVED_FROM_CAMPAIGN
    }

    public enum OfferType {
        BUNDLE,
        GIFT,
        CROSS_SELL,
        UPSELL,
        RETENTION,
        FREE_DELIVERY_PRODUCT
    }

    public enum OfferStatus {
        AVAILABLE,
        PENDING_CONDITION,
        APPLIED,
        UNAVAILABLE
    }

    public record AddCartItemRequest(
            String productCode,
            int quantity,
            String source,
            String campaignId
    ) {
    }

    public record ChangeQuantityRequest(
            int quantity,
            Integer expectedVersion
    ) {
    }

    public record CartResponse(
            UUID cartId,
            CartType cartType,
            String campaignId,
            RoleSegment roleSegment,
            String partnerContextId,
            CartStatus status,
            String currency,
            int version,
            List<CartLineResponse> lines,
            List<AppliedOfferResponse> appliedOffers,
            CartTotalsResponse totals,
            CartValidationResponse validation,
            String messageCode
    ) {
    }

    public record CartLineResponse(
            UUID lineId,
            String productCode,
            String name,
            String imageUrl,
            int quantity,
            CartLinePrice price,
            CartAvailability availability,
            String source
    ) {
    }

    public record CartLinePrice(
            BigDecimal unitPrice,
            BigDecimal promoUnitPrice,
            BigDecimal lineTotal
    ) {
    }

    public record CartAvailability(
            AvailabilityStatus status,
            Integer reservedQuantity,
            Integer maxAllowedQuantity,
            String messageCode
    ) {
    }

    public record AppliedOfferResponse(
            String offerId,
            OfferType offerType,
            String status,
            BigDecimal benefitAmount,
            String giftProductCode,
            String messageCode
    ) {
    }

    public record CartTotalsResponse(
            BigDecimal subtotal,
            BigDecimal discountTotal,
            BigDecimal benefitTotal,
            BigDecimal shippingThresholdRemaining,
            BigDecimal grandTotal
    ) {
    }

    public record ShoppingOffersResponse(
            UUID cartId,
            CartType cartType,
            List<ShoppingOfferResponse> offers
    ) {
    }

    public record ShoppingOfferResponse(
            String offerId,
            String titleKey,
            OfferType offerType,
            OfferStatus status,
            String requiredCondition,
            String remainingCondition,
            List<String> relatedProductCodes,
            BigDecimal benefitAmount,
            String messageCode
    ) {
    }

    public record CartValidationResponse(
            boolean valid,
            List<CartBlockingReason> blockingReasons,
            String checkoutRoute
    ) {
    }

    public record CartBlockingReason(
            UUID lineId,
            String code,
            String messageCode
    ) {
    }

    public record ErrorResponse(
            String code,
            String messageCode,
            Map<String, String> details
    ) {
    }
}
