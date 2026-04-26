package com.bestorigin.monolith.catalog.impl.service;

import com.bestorigin.monolith.catalog.api.AvailabilityStatus;
import com.bestorigin.monolith.catalog.domain.CatalogProduct;
import com.bestorigin.monolith.catalog.domain.CatalogRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCatalogRepository implements CatalogRepository {

    private final List<CatalogProduct> products = List.of(
            new CatalogProduct(
                    "11111111-1111-1111-1111-111111111111",
                    "BOG-CREAM-001",
                    "hydrating-face-cream",
                    "catalog.product.hydratingFaceCream.name",
                    "catalog.product.hydratingFaceCream.description",
                    "face-care",
                    "/assets/catalog/hydrating-face-cream.jpg",
                    "Best Ori Gin",
                    "50 ml",
                    "2026-C07",
                    new BigDecimal("1290.00"),
                    new BigDecimal("990.00"),
                    "RUB",
                    AvailabilityStatus.IN_STOCK,
                    18,
                    1,
                    12,
                    true,
                    100,
                    OffsetDateTime.parse("2026-04-01T10:00:00+03:00"),
                    List.of("cream", "face-care", "hydration", "partner"),
                    List.of("new", "campaign-hit")
            ),
            new CatalogProduct(
                    "22222222-2222-2222-2222-222222222222",
                    "BOG-SERUM-002",
                    "vitamin-glow-serum",
                    "catalog.product.vitaminGlowSerum.name",
                    "catalog.product.vitaminGlowSerum.description",
                    "face-care",
                    "/assets/catalog/vitamin-glow-serum.jpg",
                    "Best Ori Gin",
                    "30 ml",
                    "2026-C07",
                    new BigDecimal("1590.00"),
                    new BigDecimal("1390.00"),
                    "RUB",
                    AvailabilityStatus.LOW_STOCK,
                    3,
                    1,
                    6,
                    true,
                    80,
                    OffsetDateTime.parse("2026-04-05T10:00:00+03:00"),
                    List.of("serum", "face-care", "glow"),
                    List.of("campaign-hit")
            ),
            new CatalogProduct(
                    "33333333-3333-3333-3333-333333333333",
                    "BOG-SOLDOUT-001",
                    "velvet-lipstick",
                    "catalog.product.velvetLipstick.name",
                    "catalog.product.velvetLipstick.description",
                    "makeup",
                    "/assets/catalog/velvet-lipstick.jpg",
                    "Best Ori Gin",
                    "4 g",
                    "2026-C07",
                    new BigDecimal("790.00"),
                    null,
                    "RUB",
                    AvailabilityStatus.OUT_OF_STOCK,
                    0,
                    1,
                    5,
                    true,
                    40,
                    OffsetDateTime.parse("2026-03-28T10:00:00+03:00"),
                    List.of("makeup", "lipstick"),
                    List.of("limited")
            )
    );

    private final Map<String, Map<String, Integer>> cartItems = new ConcurrentHashMap<>();

    @Override
    public List<CatalogProduct> findPublishedProducts() {
        return products.stream().filter(CatalogProduct::published).toList();
    }

    @Override
    public Optional<CatalogProduct> findProduct(String productId) {
        return products.stream().filter(product -> product.id().equals(productId)).findFirst();
    }

    @Override
    public Optional<CatalogProduct> findProductBySku(String sku) {
        return products.stream().filter(product -> product.sku().equalsIgnoreCase(sku)).findFirst();
    }

    @Override
    public CartSnapshot saveCartItem(String userContextId, String productId, int quantity, boolean partnerContext) {
        String context = userContextId == null || userContextId.isBlank() ? "anonymous-test-context" : userContextId;
        Map<String, Integer> userCart = cartItems.computeIfAbsent(context, ignored -> new LinkedHashMap<>());
        userCart.merge(productId + ":" + partnerContext, quantity, Integer::sum);
        int totalQuantity = new ArrayList<>(userCart.values()).stream().mapToInt(Integer::intValue).sum();
        return new CartSnapshot(userCart.size(), totalQuantity);
    }
}
