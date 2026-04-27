package com.bestorigin.monolith.adminpricing.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class AdminPricingDtos {

    private AdminPricingDtos() {
    }

    public record AdminPricingErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record PriceListCreateRequest(String priceListCode, String name, String campaignId, String countryCode, String currencyCode, String channelCode, String activeFrom, String activeTo) {
    }

    public record PriceListResponse(UUID priceListId, String priceListCode, String name, String campaignId, String currencyCode, String status, String activeFrom, String activeTo, String messageCode) {
    }

    public record PriceListPage(List<PriceListResponse> items, int page, int size, long total) {
    }

    public record BasePriceCreateRequest(String productId, String sku, BigDecimal basePrice, String taxMode, String activeFrom, String activeTo) {
    }

    public record BasePriceResponse(UUID priceId, UUID priceListId, String productId, String sku, BigDecimal basePrice, String status, String messageCode) {
    }

    public record PromoPriceCreateRequest(String sku, BigDecimal promoPrice, String discountType, BigDecimal discountValue, String segmentCode, String reasonCode, String activeFrom, String activeTo) {
    }

    public record PromoPriceResponse(UUID promoPriceId, UUID priceListId, String sku, BigDecimal promoPrice, String segmentCode, String status, String messageCode) {
    }

    public record SegmentRuleCreateRequest(String campaignId, String segmentCode, String roleCode, String partnerLevel, String customerType, String regionCode, Integer priority, String activeFrom, String activeTo) {
    }

    public record SegmentRuleResponse(UUID ruleId, String campaignId, String segmentCode, String roleCode, Integer priority, String status, String messageCode) {
    }

    public record PromotionCreateRequest(String promotionCode, String nameKey, String campaignId, String audience, String channelCode, String activeFrom, String activeTo) {
    }

    public record PromotionResponse(UUID promotionId, String promotionCode, String nameKey, String campaignId, String status, String messageCode) {
    }

    public record PromotionPage(List<PromotionResponse> items, int page, int size, long total) {
    }

    public record OfferItemRequest(String sku, Integer quantity, String itemRole, Integer sortOrder) {
    }

    public record ShoppingOfferCreateRequest(String offerCode, String offerType, String titleKey, String descriptionKey, Integer priority, Boolean stackable, String mutuallyExclusiveGroup, List<OfferItemRequest> items) {
    }

    public record ShoppingOfferResponse(UUID offerId, UUID promotionId, String offerCode, String offerType, String status, String messageCode) {
    }

    public record GiftRuleCreateRequest(String giftSku, String triggerType, BigDecimal thresholdAmount, String triggerSku, Integer maxGiftQuantity, String activeFrom, String activeTo) {
    }

    public record GiftRuleResponse(UUID giftRuleId, UUID promotionId, String giftSku, BigDecimal thresholdAmount, String status, String messageCode) {
    }

    public record PublishPricingRequest(UUID priceListId, List<UUID> promotionIds, String comment) {
    }

    public record PublishPricingResponse(String status, UUID appliedPriceListId, List<UUID> appliedPromotionIds, List<String> warnings, String correlationId, String messageCode) {
    }

    public record PauseOfferRequest(String reasonCode, String comment) {
    }

    public record ImportJobCreateRequest(String sourceFileName, Boolean dryRun, String checksum, String importType) {
    }

    public record ImportJobResponse(UUID jobId, String idempotencyKey, boolean dryRun, String status, int rowCount, int errorCount, String messageCode) {
    }

    public record AuditEventResponse(UUID auditEventId, UUID actorUserId, String actionCode, String entityType, String entityId, String reasonCode, String correlationId, String occurredAt) {
    }

    public record AuditEventPage(List<AuditEventResponse> items, int page, int size, long total) {
    }
}
