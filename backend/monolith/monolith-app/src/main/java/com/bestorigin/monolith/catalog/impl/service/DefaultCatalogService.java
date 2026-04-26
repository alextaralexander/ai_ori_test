package com.bestorigin.monolith.catalog.impl.service;

import com.bestorigin.monolith.catalog.api.AddToCartRequest;
import com.bestorigin.monolith.catalog.api.Audience;
import com.bestorigin.monolith.catalog.api.AvailabilityStatus;
import com.bestorigin.monolith.catalog.api.CartSummaryResponse;
import com.bestorigin.monolith.catalog.api.CatalogProductCardResponse;
import com.bestorigin.monolith.catalog.api.CatalogSearchResponse;
import com.bestorigin.monolith.catalog.api.CatalogSort;
import com.bestorigin.monolith.catalog.domain.CatalogProduct;
import com.bestorigin.monolith.catalog.domain.CatalogRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class DefaultCatalogService implements CatalogService {

    private static final String EMPTY_CODE = "STR_MNEMO_CATALOG_SEARCH_EMPTY";
    private static final String UNAVAILABLE_CODE = "STR_MNEMO_CATALOG_ITEM_UNAVAILABLE";
    private static final String ADDED_CODE = "STR_MNEMO_CATALOG_CART_ITEM_ADDED";

    private final CatalogRepository repository;

    public DefaultCatalogService(CatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public CatalogSearchResponse search(
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
    ) {
        Audience normalizedAudience = audience == null ? Audience.GUEST : audience;
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 60);
        List<CatalogProductCardResponse> filtered = repository.findPublishedProducts().stream()
                .filter(product -> matchesQuery(product, query))
                .filter(product -> category == null || category.isBlank() || product.categorySlug().equals(category))
                .filter(product -> priceMin == null || product.price().compareTo(priceMin.max(BigDecimal.ZERO)) >= 0)
                .filter(product -> priceMax == null || product.price().compareTo(priceMax) <= 0)
                .filter(product -> matchesAvailability(product, availability))
                .filter(product -> tags == null || tags.isEmpty() || product.tags().containsAll(tags))
                .filter(product -> promo == null || !promo || !product.promoBadges().isEmpty())
                .sorted(comparator(sort, query))
                .map(product -> toCard(product, normalizedAudience))
                .toList();

        int from = Math.min(safePage * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        List<CatalogProductCardResponse> items = filtered.subList(from, to);
        List<CatalogProductCardResponse> recommendations = recommendations(normalizedAudience);
        String messageCode = items.isEmpty() ? EMPTY_CODE : null;

        return new CatalogSearchResponse(
                items,
                recommendations,
                safePage,
                safeSize,
                filtered.size(),
                to < filtered.size(),
                messageCode
        );
    }

    @Override
    public CartSummaryResponse addToCart(AddToCartRequest request) {
        Audience audience = request.audience() == null ? Audience.GUEST : request.audience();
        if (audience == Audience.GUEST || request.quantity() <= 0) {
            throw new CatalogItemUnavailableException(UNAVAILABLE_CODE);
        }
        CatalogProduct product = repository.findProduct(request.productId())
                .filter(CatalogProduct::published)
                .filter(item -> item.availability() != AvailabilityStatus.OUT_OF_STOCK)
                .orElseThrow(() -> new CatalogItemUnavailableException(UNAVAILABLE_CODE));
        CatalogRepository.CartSnapshot snapshot = repository.saveCartItem(
                request.userContextId(),
                product.id(),
                request.quantity(),
                audience == Audience.PARTNER
        );
        return new CartSummaryResponse(snapshot.itemsCount(), snapshot.totalQuantity(), ADDED_CODE);
    }

    private List<CatalogProductCardResponse> recommendations(Audience audience) {
        return repository.findPublishedProducts().stream()
                .filter(product -> product.availability() != AvailabilityStatus.OUT_OF_STOCK)
                .sorted(Comparator.comparingInt(CatalogProduct::popularRank).reversed())
                .limit(4)
                .map(product -> toCard(product, audience))
                .toList();
    }

    private static CatalogProductCardResponse toCard(CatalogProduct product, Audience audience) {
        boolean canAdd = audience != Audience.GUEST && product.availability() != AvailabilityStatus.OUT_OF_STOCK;
        return new CatalogProductCardResponse(
                product.id(),
                product.sku(),
                product.slug(),
                product.nameKey(),
                product.descriptionKey(),
                product.categorySlug(),
                product.imageUrl(),
                product.price(),
                product.currency(),
                product.availability(),
                audience == Audience.PARTNER && !product.tags().contains("partner")
                        ? appendPartnerTag(product.tags())
                        : product.tags(),
                product.promoBadges(),
                canAdd,
                canAdd ? null : UNAVAILABLE_CODE
        );
    }

    private static List<String> appendPartnerTag(List<String> tags) {
        return java.util.stream.Stream.concat(tags.stream(), java.util.stream.Stream.of("partner")).toList();
    }

    private static boolean matchesQuery(CatalogProduct product, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        return product.sku().toLowerCase(Locale.ROOT).contains(normalized)
                || product.slug().toLowerCase(Locale.ROOT).contains(normalized)
                || product.categorySlug().toLowerCase(Locale.ROOT).contains(normalized)
                || product.nameKey().toLowerCase(Locale.ROOT).contains(normalized)
                || product.descriptionKey().toLowerCase(Locale.ROOT).contains(normalized)
                || product.tags().stream().anyMatch(tag -> tag.toLowerCase(Locale.ROOT).contains(normalized));
    }

    private static boolean matchesAvailability(CatalogProduct product, String availability) {
        if (availability == null || availability.isBlank() || "all".equalsIgnoreCase(availability)) {
            return true;
        }
        if ("inStock".equalsIgnoreCase(availability)) {
            return product.availability() == AvailabilityStatus.IN_STOCK || product.availability() == AvailabilityStatus.LOW_STOCK;
        }
        if ("outOfStock".equalsIgnoreCase(availability)) {
            return product.availability() == AvailabilityStatus.OUT_OF_STOCK;
        }
        return true;
    }

    private static Comparator<CatalogProduct> comparator(CatalogSort sort, String query) {
        CatalogSort normalized = sort == null ? CatalogSort.relevance : sort;
        return switch (normalized) {
            case newest -> Comparator.comparing(CatalogProduct::createdAt).reversed();
            case priceAsc -> Comparator.comparing(CatalogProduct::price);
            case priceDesc -> Comparator.comparing(CatalogProduct::price).reversed();
            case popular -> Comparator.comparingInt(CatalogProduct::popularRank).reversed();
            case relevance -> Comparator
                    .comparing((CatalogProduct product) -> query == null || query.isBlank() || product.slug().contains(query.toLowerCase(Locale.ROOT)))
                    .reversed()
                    .thenComparing(Comparator.comparingInt(CatalogProduct::popularRank).reversed());
        };
    }
}
