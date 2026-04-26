package com.bestorigin.monolith.catalog.impl.controller;

import com.bestorigin.monolith.catalog.api.AddToCartRequest;
import com.bestorigin.monolith.catalog.api.Audience;
import com.bestorigin.monolith.catalog.api.CartSummaryResponse;
import com.bestorigin.monolith.catalog.api.CatalogErrorResponse;
import com.bestorigin.monolith.catalog.api.CatalogProductCardResponse;
import com.bestorigin.monolith.catalog.api.CatalogSearchResponse;
import com.bestorigin.monolith.catalog.api.CatalogSort;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueIssueResponse;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueMaterialActionRequest;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueMaterialActionResponse;
import com.bestorigin.monolith.catalog.impl.service.CatalogItemUnavailableException;
import com.bestorigin.monolith.catalog.impl.service.CatalogProductNotFoundException;
import com.bestorigin.monolith.catalog.impl.service.CatalogService;
import com.bestorigin.monolith.catalog.impl.service.DigitalCatalogueForbiddenException;
import com.bestorigin.monolith.catalog.impl.service.DigitalCatalogueNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public CatalogSearchResponse search(
            @RequestParam(defaultValue = "GUEST") Audience audience,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(defaultValue = "all") String availability,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Boolean promo,
            @RequestParam(defaultValue = "relevance") CatalogSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return service.search(audience, query, category, priceMin, priceMax, availability, tags, promo, sort, page, size);
    }

    @GetMapping("/products/{productCode}")
    public CatalogProductCardResponse getProductCard(
            @PathVariable String productCode,
            @RequestParam(defaultValue = "GUEST") Audience audience,
            @RequestParam(required = false) String campaignCode
    ) {
        return service.getProductCard(productCode, audience, campaignCode);
    }

    @PostMapping("/cart/items")
    public CartSummaryResponse addToCart(@RequestBody AddToCartRequest request) {
        return service.addToCart(request);
    }

    @GetMapping("/digital-catalogues/current")
    public DigitalCatalogueIssueResponse getCurrentDigitalCatalogue(
            @RequestParam(defaultValue = "GUEST") Audience audience
    ) {
        return service.getCurrentDigitalCatalogue(audience);
    }

    @GetMapping("/digital-catalogues/next")
    public DigitalCatalogueIssueResponse getNextDigitalCatalogue(
            @RequestParam(defaultValue = "GUEST") Audience audience,
            @RequestParam(required = false) Boolean preview
    ) {
        return service.getNextDigitalCatalogue(audience, preview);
    }

    @GetMapping("/digital-catalogues/{issueCode}")
    public DigitalCatalogueIssueResponse getDigitalCatalogueByCode(
            @PathVariable String issueCode,
            @RequestParam(defaultValue = "GUEST") Audience audience
    ) {
        return service.getDigitalCatalogueByCode(issueCode, audience);
    }

    @PostMapping("/digital-catalogues/materials/{materialId}/download")
    public DigitalCatalogueMaterialActionResponse downloadMaterial(
            @PathVariable String materialId,
            @RequestBody DigitalCatalogueMaterialActionRequest request
    ) {
        return service.createMaterialDownload(materialId, request);
    }

    @PostMapping("/digital-catalogues/materials/{materialId}/share")
    public DigitalCatalogueMaterialActionResponse shareMaterial(
            @PathVariable String materialId,
            @RequestBody DigitalCatalogueMaterialActionRequest request
    ) {
        return service.createMaterialShare(materialId, request);
    }

    @ExceptionHandler(CatalogItemUnavailableException.class)
    public ResponseEntity<CatalogErrorResponse> handleUnavailable(CatalogItemUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new CatalogErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CatalogProductNotFoundException.class)
    public ResponseEntity<CatalogErrorResponse> handleProductNotFound(CatalogProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CatalogErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DigitalCatalogueForbiddenException.class)
    public ResponseEntity<CatalogErrorResponse> handleDigitalCatalogueForbidden(DigitalCatalogueForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new CatalogErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DigitalCatalogueNotFoundException.class)
    public ResponseEntity<CatalogErrorResponse> handleDigitalCatalogueNotFound(DigitalCatalogueNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CatalogErrorResponse(ex.getMessage()));
    }
}
