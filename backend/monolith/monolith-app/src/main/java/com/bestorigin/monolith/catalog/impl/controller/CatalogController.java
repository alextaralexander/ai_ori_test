package com.bestorigin.monolith.catalog.impl.controller;

import com.bestorigin.monolith.catalog.api.AddToCartRequest;
import com.bestorigin.monolith.catalog.api.Audience;
import com.bestorigin.monolith.catalog.api.CartSummaryResponse;
import com.bestorigin.monolith.catalog.api.CatalogErrorResponse;
import com.bestorigin.monolith.catalog.api.CatalogSearchResponse;
import com.bestorigin.monolith.catalog.api.CatalogSort;
import com.bestorigin.monolith.catalog.impl.service.CatalogItemUnavailableException;
import com.bestorigin.monolith.catalog.impl.service.CatalogService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/cart/items")
    public CartSummaryResponse addToCart(@RequestBody AddToCartRequest request) {
        return service.addToCart(request);
    }

    @ExceptionHandler(CatalogItemUnavailableException.class)
    public ResponseEntity<CatalogErrorResponse> handleUnavailable(CatalogItemUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new CatalogErrorResponse(ex.getMessage()));
    }
}
