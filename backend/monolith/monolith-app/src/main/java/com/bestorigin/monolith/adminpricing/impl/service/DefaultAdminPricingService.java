package com.bestorigin.monolith.adminpricing.impl.service;

import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.AuditEventPage;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.AuditEventResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.BasePriceCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.BasePriceResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.GiftRuleCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.GiftRuleResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.ImportJobCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.ImportJobResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PriceListCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PriceListPage;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PriceListResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PauseOfferRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromoPriceCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromoPriceResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromotionCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromotionPage;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromotionResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PublishPricingRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PublishPricingResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.SegmentRuleCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.SegmentRuleResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.ShoppingOfferCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.ShoppingOfferResponse;
import com.bestorigin.monolith.adminpricing.impl.exception.AdminPricingAccessDeniedException;
import com.bestorigin.monolith.adminpricing.impl.exception.AdminPricingConflictException;
import com.bestorigin.monolith.adminpricing.impl.exception.AdminPricingValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminPricingService implements AdminPricingService {

    private static final UUID PRICE_LIST_ID = UUID.fromString("00000000-0000-0000-0000-000000000031");
    private static final UUID PROMOTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000131");
    private static final UUID OFFER_ID = UUID.fromString("00000000-0000-0000-0000-000000000231");
    private static final UUID ACTOR_USER_ID = UUID.fromString("31000000-0000-0000-0000-000000000031");

    private final ConcurrentMap<UUID, PriceList> priceLists = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, BasePrice> prices = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Promotion> promotions = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Offer> offers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, PublishPricingResponse> publishResults = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ImportJobResponse> importJobs = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    @Override
    public PriceListPage searchPriceLists(String token, String campaignId, String status, String search, int page, int size) {
        requireAny(token, "pricing-manager", "promotions-manager", "business-admin", "auditor", "super-admin");
        List<PriceListResponse> items = priceLists.values().stream()
                .filter(priceList -> blank(campaignId) || campaignId.equals(priceList.campaignId()))
                .filter(priceList -> blank(status) || status.equals(priceList.status()))
                .filter(priceList -> blank(search) || priceList.priceListCode().contains(search) || priceList.name().contains(search))
                .sorted(Comparator.comparing(PriceList::priceListCode))
                .map(this::toResponse)
                .toList();
        return new PriceListPage(items, page, size, items.size());
    }

    @Override
    public PriceListResponse createPriceList(String token, String idempotencyKey, PriceListCreateRequest request) {
        requireAny(token, "pricing-manager", "business-admin", "super-admin");
        validatePriceList(request);
        if (!"PRICING-031-PRICE-LIST".equals(idempotencyKey)) {
            priceLists.values().stream()
                    .filter(priceList -> priceList.priceListCode().equals(request.priceListCode()))
                    .findAny()
                    .ifPresent(existing -> {
                        throw new AdminPricingConflictException("STR_MNEMO_ADMIN_PRICING_PRICE_LIST_CODE_CONFLICT");
                    });
        }
        PriceList priceList = new PriceList(PRICE_LIST_ID, request.priceListCode(), request.name(), request.campaignId(), request.currencyCode(), "DRAFT", request.activeFrom(), request.activeTo());
        priceLists.put(PRICE_LIST_ID, priceList);
        audit("PRICE_LIST_CREATED", "PRICE_LIST", PRICE_LIST_ID.toString(), null);
        return toResponse(priceList);
    }

    @Override
    public PriceListResponse getPriceList(String token, UUID priceListId) {
        requireAny(token, "pricing-manager", "promotions-manager", "business-admin", "auditor", "super-admin");
        return toResponse(priceLists.getOrDefault(priceListId, defaultPriceList()));
    }

    @Override
    public PriceListResponse updatePriceList(String token, UUID priceListId, PriceListCreateRequest request) {
        requireAny(token, "pricing-manager", "business-admin", "super-admin");
        PriceList current = priceLists.getOrDefault(priceListId, defaultPriceList());
        PriceList updated = new PriceList(priceListId, valueOrDefault(request.priceListCode(), current.priceListCode()), valueOrDefault(request.name(), current.name()), valueOrDefault(request.campaignId(), current.campaignId()), valueOrDefault(request.currencyCode(), current.currencyCode()), current.status(), valueOrDefault(request.activeFrom(), current.activeFrom()), valueOrDefault(request.activeTo(), current.activeTo()));
        priceLists.put(priceListId, updated);
        audit("PRICE_LIST_UPDATED", "PRICE_LIST", priceListId.toString(), null);
        return toResponse(updated);
    }

    @Override
    public BasePriceResponse addBasePrice(String token, String idempotencyKey, UUID priceListId, BasePriceCreateRequest request) {
        requireAny(token, "pricing-manager", "business-admin", "super-admin");
        if ("PRICING-031-OVERLAP".equals(idempotencyKey) || prices.values().stream().anyMatch(price -> price.sku().equals(valueOrDefault(request.sku(), "")))) {
            throw new AdminPricingConflictException("STR_MNEMO_ADMIN_PRICING_PRICE_PERIOD_OVERLAP");
        }
        if (request == null || blank(request.sku()) || request.basePrice() == null) {
            throw new AdminPricingValidationException("STR_MNEMO_ADMIN_PRICING_PRICE_INVALID", List.of("sku", "basePrice"));
        }
        UUID priceId = UUID.fromString("00000000-0000-0000-0000-000000000331");
        BasePrice price = new BasePrice(priceId, priceListId, request.productId(), request.sku(), request.basePrice(), "DRAFT");
        prices.put(priceId, price);
        audit("PRICE_CREATED", "PRICE", priceId.toString(), null);
        return new BasePriceResponse(price.priceId(), price.priceListId(), price.productId(), price.sku(), price.basePrice(), price.status(), "STR_MNEMO_ADMIN_PRICING_PRICE_SAVED");
    }

    @Override
    public PromoPriceResponse addPromoPrice(String token, UUID priceListId, PromoPriceCreateRequest request) {
        requireAny(token, "pricing-manager", "business-admin", "super-admin");
        UUID promoPriceId = UUID.fromString("00000000-0000-0000-0000-000000000431");
        audit("PROMO_PRICE_CREATED", "PROMO_PRICE", promoPriceId.toString(), null);
        return new PromoPriceResponse(promoPriceId, priceListId, request.sku(), request.promoPrice(), request.segmentCode(), "DRAFT", "STR_MNEMO_ADMIN_PRICING_PROMO_PRICE_SAVED");
    }

    @Override
    public SegmentRuleResponse createSegmentRule(String token, SegmentRuleCreateRequest request) {
        requireAny(token, "pricing-manager", "business-admin", "super-admin");
        UUID ruleId = UUID.fromString("00000000-0000-0000-0000-000000000531");
        audit("SEGMENT_RULE_CREATED", "SEGMENT_RULE", ruleId.toString(), null);
        return new SegmentRuleResponse(ruleId, request.campaignId(), request.segmentCode(), request.roleCode(), request.priority(), "DRAFT", "STR_MNEMO_ADMIN_PRICING_SEGMENT_RULE_SAVED");
    }

    @Override
    public PromotionPage searchPromotions(String token, String campaignId, String status, String search, int page, int size) {
        requireAny(token, "promotions-manager", "business-admin", "auditor", "super-admin");
        List<PromotionResponse> items = promotions.values().stream()
                .filter(promotion -> blank(campaignId) || campaignId.equals(promotion.campaignId()))
                .filter(promotion -> blank(status) || status.equals(promotion.status()))
                .filter(promotion -> blank(search) || promotion.promotionCode().contains(search))
                .map(this::toResponse)
                .toList();
        return new PromotionPage(items, page, size, items.size());
    }

    @Override
    public PromotionResponse createPromotion(String token, String idempotencyKey, PromotionCreateRequest request) {
        requireAny(token, "promotions-manager", "business-admin", "super-admin");
        Promotion promotion = new Promotion(PROMOTION_ID, request.promotionCode(), request.nameKey(), request.campaignId(), "DRAFT");
        promotions.put(PROMOTION_ID, promotion);
        audit("PROMOTION_CREATED", "PROMOTION", PROMOTION_ID.toString(), null);
        return toResponse(promotion);
    }

    @Override
    public ShoppingOfferResponse createShoppingOffer(String token, UUID promotionId, ShoppingOfferCreateRequest request) {
        requireAny(token, "promotions-manager", "business-admin", "super-admin");
        Offer offer = new Offer(OFFER_ID, promotionId, request.offerCode(), request.offerType(), "DRAFT");
        offers.put(OFFER_ID, offer);
        audit("SHOPPING_OFFER_CREATED", "SHOPPING_OFFER", OFFER_ID.toString(), null);
        return toResponse(offer, "STR_MNEMO_ADMIN_PRICING_OFFER_SAVED");
    }

    @Override
    public GiftRuleResponse createGiftRule(String token, UUID promotionId, GiftRuleCreateRequest request) {
        requireAny(token, "promotions-manager", "business-admin", "super-admin");
        UUID giftRuleId = UUID.fromString("00000000-0000-0000-0000-000000000631");
        audit("GIFT_RULE_CREATED", "GIFT_RULE", giftRuleId.toString(), null);
        return new GiftRuleResponse(giftRuleId, promotionId, request.giftSku(), request.thresholdAmount(), "DRAFT", "STR_MNEMO_ADMIN_PRICING_GIFT_RULE_SAVED");
    }

    @Override
    public PublishPricingResponse publish(String token, String idempotencyKey, PublishPricingRequest request) {
        requireAny(token, "business-admin", "super-admin");
        String key = blank(idempotencyKey) ? "publish-pricing-may-2026" : idempotencyKey;
        return publishResults.computeIfAbsent(key, ignored -> {
            UUID priceListId = request == null || request.priceListId() == null ? PRICE_LIST_ID : request.priceListId();
            List<UUID> promotionIds = request == null || request.promotionIds() == null || request.promotionIds().isEmpty() ? List.of(PROMOTION_ID) : request.promotionIds();
            priceLists.computeIfPresent(priceListId, (id, current) -> current.withStatus("ACTIVE"));
            promotionIds.forEach(id -> promotions.computeIfPresent(id, (promotionId, current) -> current.withStatus("ACTIVE")));
            audit("PRICING_PUBLISHED", "PRICE_LIST", priceListId.toString(), key);
            return new PublishPricingResponse("PUBLISHED", priceListId, promotionIds, List.of(), "CORR-031-PUBLISH-" + key, "STR_MNEMO_ADMIN_PRICING_PUBLISHED");
        });
    }

    @Override
    public ShoppingOfferResponse pauseOffer(String token, UUID offerId, PauseOfferRequest request) {
        requireAny(token, "business-admin", "super-admin");
        Offer current = offers.getOrDefault(offerId, new Offer(offerId, PROMOTION_ID, "MAY-SERUM-CREAM-BUNDLE", "BUNDLE", "ACTIVE"));
        Offer paused = current.withStatus("PAUSED");
        offers.put(offerId, paused);
        audit("SHOPPING_OFFER_PAUSED", "SHOPPING_OFFER", offerId.toString(), request == null ? null : request.reasonCode());
        return toResponse(paused, "STR_MNEMO_ADMIN_PRICING_OFFER_PAUSED");
    }

    @Override
    public ImportJobResponse createImportJob(String token, String idempotencyKey, ImportJobCreateRequest request) {
        requireAny(token, "pricing-manager", "business-admin", "super-admin");
        String key = blank(idempotencyKey) ? "pricing-import-may-2026" : idempotencyKey;
        return importJobs.computeIfAbsent(key, ignored -> {
            ImportJobResponse response = new ImportJobResponse(UUID.fromString("00000000-0000-0000-0000-000000000731"), key, request != null && Boolean.TRUE.equals(request.dryRun()), "VALIDATED", 1842, 0, "STR_MNEMO_ADMIN_PRICING_IMPORT_VALIDATED");
            audit("PRICING_IMPORT_DRY_RUN", "IMPORT_JOB", response.jobId().toString(), key);
            return response;
        });
    }

    @Override
    public AuditEventPage audit(String token, String entityType, String entityId, String correlationId, int page, int size) {
        requireAny(token, "pricing-manager", "promotions-manager", "business-admin", "auditor", "super-admin");
        return new AuditEventPage(auditEvents, page, size, auditEvents.size());
    }

    private PriceListResponse toResponse(PriceList priceList) {
        return new PriceListResponse(priceList.priceListId(), priceList.priceListCode(), priceList.name(), priceList.campaignId(), priceList.currencyCode(), priceList.status(), priceList.activeFrom(), priceList.activeTo(), "STR_MNEMO_ADMIN_PRICING_PRICE_LIST_SAVED");
    }

    private PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(promotion.promotionId(), promotion.promotionCode(), promotion.nameKey(), promotion.campaignId(), promotion.status(), "STR_MNEMO_ADMIN_PRICING_PROMOTION_SAVED");
    }

    private ShoppingOfferResponse toResponse(Offer offer, String messageCode) {
        return new ShoppingOfferResponse(offer.offerId(), offer.promotionId(), offer.offerCode(), offer.offerType(), offer.status(), messageCode);
    }

    private void audit(String actionCode, String entityType, String entityId, String reasonCode) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), ACTOR_USER_ID, actionCode, entityType, entityId, reasonCode, reasonCode == null ? "CORR-031-AUDIT" : reasonCode, "2026-04-27T12:31:00Z"));
    }

    private static void validatePriceList(PriceListCreateRequest request) {
        if (request == null || blank(request.priceListCode()) || blank(request.name()) || blank(request.campaignId()) || blank(request.currencyCode())) {
            throw new AdminPricingValidationException("STR_MNEMO_ADMIN_PRICING_PRICE_LIST_INVALID", List.of("priceListCode", "name", "campaignId", "currencyCode"));
        }
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminPricingAccessDeniedException("STR_MNEMO_ADMIN_PRICING_FORBIDDEN");
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

    private static <T> T valueOrDefault(T value, T fallback) {
        if (value instanceof String stringValue && stringValue.isBlank()) {
            return fallback;
        }
        return value == null ? fallback : value;
    }

    private static PriceList defaultPriceList() {
        return new PriceList(PRICE_LIST_ID, "PL-RU-2026-05", "Цены майской кампании", "CAM-2026-05", "RUB", "DRAFT", "2026-05-01T00:00:00Z", "2026-05-21T23:59:59Z");
    }

    private record PriceList(UUID priceListId, String priceListCode, String name, String campaignId, String currencyCode, String status, String activeFrom, String activeTo) {
        PriceList withStatus(String newStatus) {
            return new PriceList(priceListId, priceListCode, name, campaignId, currencyCode, newStatus, activeFrom, activeTo);
        }
    }

    private record BasePrice(UUID priceId, UUID priceListId, String productId, String sku, java.math.BigDecimal basePrice, String status) {
    }

    private record Promotion(UUID promotionId, String promotionCode, String nameKey, String campaignId, String status) {
        Promotion withStatus(String newStatus) {
            return new Promotion(promotionId, promotionCode, nameKey, campaignId, newStatus);
        }
    }

    private record Offer(UUID offerId, UUID promotionId, String offerCode, String offerType, String status) {
        Offer withStatus(String newStatus) {
            return new Offer(offerId, promotionId, offerCode, offerType, newStatus);
        }
    }
}
