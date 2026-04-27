package com.bestorigin.monolith.adminpricing.impl.service;

import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.AuditEventPage;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.BasePriceCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.BasePriceResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.GiftRuleCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.GiftRuleResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.ImportJobCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.ImportJobResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PriceListCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PriceListPage;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PriceListResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromoPriceCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromoPriceResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromotionCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromotionPage;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PromotionResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PublishPricingRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PublishPricingResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.SegmentRuleCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.SegmentRuleResponse;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.PauseOfferRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.ShoppingOfferCreateRequest;
import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.ShoppingOfferResponse;
import java.util.UUID;

public interface AdminPricingService {

    PriceListPage searchPriceLists(String token, String campaignId, String status, String search, int page, int size);

    PriceListResponse createPriceList(String token, String idempotencyKey, PriceListCreateRequest request);

    PriceListResponse getPriceList(String token, UUID priceListId);

    PriceListResponse updatePriceList(String token, UUID priceListId, PriceListCreateRequest request);

    BasePriceResponse addBasePrice(String token, String idempotencyKey, UUID priceListId, BasePriceCreateRequest request);

    PromoPriceResponse addPromoPrice(String token, UUID priceListId, PromoPriceCreateRequest request);

    SegmentRuleResponse createSegmentRule(String token, SegmentRuleCreateRequest request);

    PromotionPage searchPromotions(String token, String campaignId, String status, String search, int page, int size);

    PromotionResponse createPromotion(String token, String idempotencyKey, PromotionCreateRequest request);

    ShoppingOfferResponse createShoppingOffer(String token, UUID promotionId, ShoppingOfferCreateRequest request);

    GiftRuleResponse createGiftRule(String token, UUID promotionId, GiftRuleCreateRequest request);

    PublishPricingResponse publish(String token, String idempotencyKey, PublishPricingRequest request);

    ShoppingOfferResponse pauseOffer(String token, UUID offerId, PauseOfferRequest request);

    ImportJobResponse createImportJob(String token, String idempotencyKey, ImportJobCreateRequest request);

    AuditEventPage audit(String token, String entityType, String entityId, String correlationId, int page, int size);
}
