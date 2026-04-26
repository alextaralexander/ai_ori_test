package com.bestorigin.monolith.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository {

    List<CatalogProduct> findPublishedProducts();

    Optional<CatalogProduct> findProduct(String productId);

    CartSnapshot saveCartItem(String userContextId, String productId, int quantity, boolean partnerContext);

    record CartSnapshot(int itemsCount, int totalQuantity) {
    }
}
