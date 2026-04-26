package com.bestorigin.monolith.catalog.impl.service;

import com.bestorigin.monolith.catalog.api.AddToCartRequest;
import com.bestorigin.monolith.catalog.api.Audience;
import com.bestorigin.monolith.catalog.api.CartSummaryResponse;
import com.bestorigin.monolith.catalog.api.CatalogProductCardResponse;
import com.bestorigin.monolith.catalog.api.CatalogSearchResponse;
import com.bestorigin.monolith.catalog.api.CatalogSort;
import java.math.BigDecimal;
import java.util.List;

public interface CatalogService {

    CatalogSearchResponse search(
            Audience audience,
            String query,
            String category,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String availability,
            List<String> tags,
            Boolean promo,
            CatalogSort sort,
            int page,
            int size
    );

    CatalogProductCardResponse getProductCard(String productCode, Audience audience, String campaignCode);

    CartSummaryResponse addToCart(AddToCartRequest request);
}
