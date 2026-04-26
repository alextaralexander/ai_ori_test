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
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DefaultCatalogService implements CatalogService {

    private static final String EMPTY_CODE = "STR_MNEMO_CATALOG_SEARCH_EMPTY";
    private static final String NOT_FOUND_CODE = "STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND";
    private static final String UNAVAILABLE_CODE = "STR_MNEMO_CATALOG_ITEM_UNAVAILABLE";
    private static final String QUANTITY_LIMIT_CODE = "STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED";
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
        String messageCode = items.isEmpty() ? EMPTY_CODE : null;

        return new CatalogSearchResponse(
                items,
                recommendations(normalizedAudience),
                safePage,
                safeSize,
                filtered.size(),
                to < filtered.size(),
                messageCode
        );
    }

    @Override
    public CatalogProductCardResponse getProductCard(String productCode, Audience audience, String campaignCode) {
        Audience normalizedAudience = audience == null ? Audience.GUEST : audience;
        CatalogProduct product = repository.findProductBySku(productCode)
                .filter(CatalogProduct::published)
                .filter(item -> campaignCode == null || campaignCode.isBlank() || item.campaignCode().equals(campaignCode))
                .orElseThrow(() -> new CatalogProductNotFoundException(NOT_FOUND_CODE));
        return toDetailedCard(product, normalizedAudience);
    }

    @Override
    public CartSummaryResponse addToCart(AddToCartRequest request) {
        Audience audience = request.audience() == null ? Audience.GUEST : request.audience();
        if (audience == Audience.GUEST || request.quantity() <= 0) {
            throw new CatalogItemUnavailableException(UNAVAILABLE_CODE);
        }
        CatalogProduct product = resolveCartProduct(request)
                .filter(CatalogProduct::published)
                .filter(item -> item.availability() != AvailabilityStatus.OUT_OF_STOCK)
                .orElseThrow(() -> new CatalogItemUnavailableException(UNAVAILABLE_CODE));
        if (request.quantity() < product.minOrderQuantity()
                || request.quantity() > product.maxOrderQuantity()
                || request.quantity() > product.availableQuantity()) {
            throw new CatalogItemUnavailableException(QUANTITY_LIMIT_CODE);
        }
        boolean partnerContext = audience == Audience.PARTNER || request.partnerContextId() != null && !request.partnerContextId().isBlank();
        CatalogRepository.CartSnapshot snapshot = repository.saveCartItem(
                request.userContextId(),
                product.id(),
                request.quantity(),
                partnerContext
        );
        return new CartSummaryResponse(snapshot.itemsCount(), snapshot.totalQuantity(), ADDED_CODE, partnerContext);
    }

    private Optional<CatalogProduct> resolveCartProduct(AddToCartRequest request) {
        if (request.productCode() != null && !request.productCode().isBlank()) {
            return repository.findProductBySku(request.productCode());
        }
        if (request.productId() != null && !request.productId().isBlank()) {
            return repository.findProduct(request.productId());
        }
        return Optional.empty();
    }

    private List<CatalogProductCardResponse> recommendations(Audience audience) {
        return repository.findPublishedProducts().stream()
                .filter(product -> product.availability() != AvailabilityStatus.OUT_OF_STOCK)
                .sorted(Comparator.comparingInt(CatalogProduct::popularRank).reversed())
                .limit(4)
                .map(product -> toCard(product, audience))
                .toList();
    }

    private List<CatalogProductCardResponse.CatalogProductRecommendation> productRecommendations(CatalogProduct current) {
        return repository.findPublishedProducts().stream()
                .filter(product -> !product.id().equals(current.id()))
                .filter(product -> product.availability() != AvailabilityStatus.OUT_OF_STOCK)
                .sorted(Comparator.comparingInt(CatalogProduct::popularRank).reversed())
                .limit(4)
                .map(product -> new CatalogProductCardResponse.CatalogProductRecommendation(
                        product.sku(),
                        displayName(product),
                        product.imageUrl(),
                        product.price(),
                        product.currency(),
                        product.availability(),
                        product.tags().contains("serum") ? "CROSS_SELL" : "RELATED"
                ))
                .toList();
    }

    private CatalogProductCardResponse toDetailedCard(CatalogProduct product, Audience audience) {
        return baseCard(product, audience, productRecommendations(product));
    }

    private static CatalogProductCardResponse toCard(CatalogProduct product, Audience audience) {
        return baseCard(product, audience, List.of());
    }

    private static CatalogProductCardResponse baseCard(
            CatalogProduct product,
            Audience audience,
            List<CatalogProductCardResponse.CatalogProductRecommendation> recommendations
    ) {
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
                canAdd ? null : UNAVAILABLE_CODE,
                product.sku(),
                displayName(product),
                categoryName(product.categorySlug()),
                product.brand(),
                product.volumeLabel(),
                product.campaignCode(),
                new CatalogProductCardResponse.CatalogOrderLimits(product.minOrderQuantity(), product.maxOrderQuantity()),
                media(product),
                information(product),
                attachments(product),
                recommendations
        );
    }

    private static List<CatalogProductCardResponse.CatalogProductMedia> media(CatalogProduct product) {
        return List.of(
                new CatalogProductCardResponse.CatalogProductMedia(product.imageUrl(), displayName(product), true, 1),
                new CatalogProductCardResponse.CatalogProductMedia(product.imageUrl().replace(".jpg", "-detail.jpg"), displayName(product), false, 2)
        );
    }

    private static CatalogProductCardResponse.CatalogProductInformation information(CatalogProduct product) {
        return new CatalogProductCardResponse.CatalogProductInformation(
                displayDescription(product),
                displayDescription(product) + " Подходит для ежедневного ухода Best Ori Gin.",
                "Нанесите небольшое количество на чистую кожу и распределите мягкими движениями.",
                "Вода, глицерин, растительные экстракты, косметическая основа.",
                List.of(
                        new CatalogProductCardResponse.CatalogCharacteristic("Тип кожи", product.categorySlug().equals("face-care") ? "Для всех типов кожи" : "Универсальный"),
                        new CatalogProductCardResponse.CatalogCharacteristic("Кампания", product.campaignCode()),
                        new CatalogProductCardResponse.CatalogCharacteristic("Объем", product.volumeLabel())
                )
        );
    }

    private static List<CatalogProductCardResponse.CatalogProductAttachment> attachments(CatalogProduct product) {
        return List.of(new CatalogProductCardResponse.CatalogProductAttachment(
                "Инструкция и состав",
                "PRODUCT_INFO",
                "/assets/catalog/" + product.slug() + "-info.pdf"
        ));
    }

    private static String displayName(CatalogProduct product) {
        if (product.sku().contains("CREAM")) {
            return "Увлажняющий крем Best Ori Gin";
        }
        if (product.sku().contains("SERUM")) {
            return "Сыворотка Vitamin Glow";
        }
        return "Товар Best Ori Gin";
    }

    private static String displayDescription(CatalogProduct product) {
        if (product.sku().contains("CREAM")) {
            return "Крем для увлажнения и поддержки сияния кожи.";
        }
        if (product.sku().contains("SERUM")) {
            return "Сыворотка для ровного тона и свежего вида кожи.";
        }
        return "Косметический продукт текущей кампании.";
    }

    private static String categoryName(String categorySlug) {
        return "face-care".equals(categorySlug) ? "Уход за лицом" : "Макияж";
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
