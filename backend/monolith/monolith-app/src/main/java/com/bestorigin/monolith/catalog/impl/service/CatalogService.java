package com.bestorigin.monolith.catalog.impl.service;

import com.bestorigin.monolith.catalog.api.AddToCartRequest;
import com.bestorigin.monolith.catalog.api.Audience;
import com.bestorigin.monolith.catalog.api.CartSummaryResponse;
import com.bestorigin.monolith.catalog.api.CatalogProductCardResponse;
import com.bestorigin.monolith.catalog.api.CatalogSearchResponse;
import com.bestorigin.monolith.catalog.api.CatalogSort;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueIssueResponse;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueMaterialActionRequest;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueMaterialActionResponse;
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

    DigitalCatalogueIssueResponse getCurrentDigitalCatalogue(Audience audience);

    DigitalCatalogueIssueResponse getNextDigitalCatalogue(Audience audience, Boolean preview);

    DigitalCatalogueIssueResponse getDigitalCatalogueByCode(String issueCode, Audience audience);

    DigitalCatalogueMaterialActionResponse createMaterialDownload(String materialId, DigitalCatalogueMaterialActionRequest request);

    DigitalCatalogueMaterialActionResponse createMaterialShare(String materialId, DigitalCatalogueMaterialActionRequest request);
}
