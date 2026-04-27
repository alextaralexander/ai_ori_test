package com.bestorigin.monolith.adminpricing.impl.controller;

import com.bestorigin.monolith.adminpricing.api.AdminPricingDtos.AdminPricingErrorResponse;
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
import com.bestorigin.monolith.adminpricing.impl.service.AdminPricingService;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pricing")
public class AdminPricingController {

    private final AdminPricingService service;

    public AdminPricingController(AdminPricingService service) {
        this.service = service;
    }

    @GetMapping("/price-lists")
    public PriceListPage priceLists(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String campaignId, @RequestParam(required = false) String status, @RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchPriceLists(token(headers), campaignId, status, search, page, size);
    }

    @PostMapping("/price-lists")
    public ResponseEntity<PriceListResponse> createPriceList(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody PriceListCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPriceList(token(headers), idempotencyKey, request));
    }

    @GetMapping("/price-lists/{priceListId}")
    public PriceListResponse getPriceList(@RequestHeader HttpHeaders headers, @PathVariable UUID priceListId) {
        return service.getPriceList(token(headers), priceListId);
    }

    @PatchMapping("/price-lists/{priceListId}")
    public PriceListResponse updatePriceList(@RequestHeader HttpHeaders headers, @PathVariable UUID priceListId, @RequestBody PriceListCreateRequest request) {
        return service.updatePriceList(token(headers), priceListId, request);
    }

    @PostMapping("/price-lists/{priceListId}/prices")
    public ResponseEntity<BasePriceResponse> addBasePrice(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID priceListId, @RequestBody BasePriceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addBasePrice(token(headers), idempotencyKey, priceListId, request));
    }

    @PostMapping("/price-lists/{priceListId}/promo-prices")
    public ResponseEntity<PromoPriceResponse> addPromoPrice(@RequestHeader HttpHeaders headers, @PathVariable UUID priceListId, @RequestBody PromoPriceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addPromoPrice(token(headers), priceListId, request));
    }

    @PostMapping("/segment-rules")
    public ResponseEntity<SegmentRuleResponse> createSegmentRule(@RequestHeader HttpHeaders headers, @RequestBody SegmentRuleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSegmentRule(token(headers), request));
    }

    @GetMapping("/promotions")
    public PromotionPage promotions(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String campaignId, @RequestParam(required = false) String status, @RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchPromotions(token(headers), campaignId, status, search, page, size);
    }

    @PostMapping("/promotions")
    public ResponseEntity<PromotionResponse> createPromotion(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody PromotionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPromotion(token(headers), idempotencyKey, request));
    }

    @PostMapping("/promotions/{promotionId}/offers")
    public ResponseEntity<ShoppingOfferResponse> createOffer(@RequestHeader HttpHeaders headers, @PathVariable UUID promotionId, @RequestBody ShoppingOfferCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createShoppingOffer(token(headers), promotionId, request));
    }

    @PostMapping("/promotions/{promotionId}/gift-rules")
    public ResponseEntity<GiftRuleResponse> createGiftRule(@RequestHeader HttpHeaders headers, @PathVariable UUID promotionId, @RequestBody GiftRuleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createGiftRule(token(headers), promotionId, request));
    }

    @PostMapping("/publish")
    public PublishPricingResponse publish(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody PublishPricingRequest request) {
        return service.publish(token(headers), idempotencyKey, request);
    }

    @PostMapping("/offers/{offerId}/pause")
    public ShoppingOfferResponse pauseOffer(@RequestHeader HttpHeaders headers, @PathVariable UUID offerId, @RequestBody PauseOfferRequest request) {
        return service.pauseOffer(token(headers), offerId, request);
    }

    @PostMapping("/imports")
    public ResponseEntity<ImportJobResponse> createImport(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ImportJobCreateRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.createImportJob(token(headers), idempotencyKey, request));
    }

    @GetMapping("/audit-events")
    public AuditEventPage audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String entityType, @RequestParam(required = false) String entityId, @RequestParam(required = false) String correlationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.audit(token(headers), entityType, entityId, correlationId, page, size);
    }

    @ExceptionHandler(AdminPricingAccessDeniedException.class)
    public ResponseEntity<AdminPricingErrorResponse> handleForbidden(AdminPricingAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminPricingConflictException.class)
    public ResponseEntity<AdminPricingErrorResponse> handleConflict(AdminPricingConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminPricingValidationException.class)
    public ResponseEntity<AdminPricingErrorResponse> handleValidation(AdminPricingValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminPricingErrorResponse error(String messageCode, java.util.List<String> details) {
        return new AdminPricingErrorResponse(messageCode, "CORR-031-ERROR", details == null ? java.util.List.of() : details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
