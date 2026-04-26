package com.bestorigin.monolith.catalog.impl.service;

import com.bestorigin.monolith.catalog.api.AddToCartRequest;
import com.bestorigin.monolith.catalog.api.Audience;
import com.bestorigin.monolith.catalog.api.AvailabilityStatus;
import com.bestorigin.monolith.catalog.api.CartSummaryResponse;
import com.bestorigin.monolith.catalog.api.CatalogProductCardResponse;
import com.bestorigin.monolith.catalog.api.CatalogSearchResponse;
import com.bestorigin.monolith.catalog.api.CatalogSort;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueIssueResponse;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueMaterialActionRequest;
import com.bestorigin.monolith.catalog.api.DigitalCatalogueMaterialActionResponse;
import com.bestorigin.monolith.catalog.domain.CatalogProduct;
import com.bestorigin.monolith.catalog.domain.CatalogRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    private static final String DIGITAL_NOT_FOUND_CODE = "STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND";
    private static final String DIGITAL_FORBIDDEN_CODE = "STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN";
    private static final String DIGITAL_MATERIAL_UNAVAILABLE_CODE = "STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE";
    private static final String DIGITAL_SHARE_NOT_ALLOWED_CODE = "STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED";
    private static final String DIGITAL_DOWNLOAD_NOT_ALLOWED_CODE = "STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED";
    private static final String DIGITAL_MATERIAL_READY_CODE = "STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY";

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

    @Override
    public DigitalCatalogueIssueResponse getCurrentDigitalCatalogue(Audience audience) {
        return currentDigitalCatalogue(normalizeAudience(audience));
    }

    @Override
    public DigitalCatalogueIssueResponse getNextDigitalCatalogue(Audience audience, Boolean preview) {
        Audience normalizedAudience = normalizeAudience(audience);
        if (normalizedAudience == Audience.GUEST && Boolean.FALSE.equals(preview)) {
            throw new DigitalCatalogueForbiddenException(DIGITAL_FORBIDDEN_CODE);
        }
        return nextDigitalCatalogue(normalizedAudience);
    }

    @Override
    public DigitalCatalogueIssueResponse getDigitalCatalogueByCode(String issueCode, Audience audience) {
        if (issueCode == null || issueCode.isBlank()) {
            throw new DigitalCatalogueNotFoundException(DIGITAL_NOT_FOUND_CODE);
        }
        String normalized = issueCode.toLowerCase(Locale.ROOT);
        if ("catalog-2026-05".equals(normalized)) {
            return currentDigitalCatalogue(normalizeAudience(audience));
        }
        if ("catalog-2026-06".equals(normalized)) {
            return nextDigitalCatalogue(normalizeAudience(audience));
        }
        throw new DigitalCatalogueNotFoundException(DIGITAL_NOT_FOUND_CODE);
    }

    @Override
    public DigitalCatalogueMaterialActionResponse createMaterialDownload(String materialId, DigitalCatalogueMaterialActionRequest request) {
        DigitalCatalogueIssueResponse.Material material = findMaterial(materialId);
        if (!material.actions().canDownload()) {
            throw new DigitalCatalogueForbiddenException(DIGITAL_DOWNLOAD_NOT_ALLOWED_CODE);
        }
        return materialAction(material.materialId(), "download");
    }

    @Override
    public DigitalCatalogueMaterialActionResponse createMaterialShare(String materialId, DigitalCatalogueMaterialActionRequest request) {
        DigitalCatalogueIssueResponse.Material material = findMaterial(materialId);
        if (!material.actions().canShare()) {
            throw new DigitalCatalogueForbiddenException(DIGITAL_SHARE_NOT_ALLOWED_CODE);
        }
        return materialAction(material.materialId(), "share");
    }

    private static Audience normalizeAudience(Audience audience) {
        return audience == null ? Audience.GUEST : audience;
    }

    private static DigitalCatalogueIssueResponse currentDigitalCatalogue(Audience audience) {
        return digitalCatalogue(
                "catalog-2026-05",
                "Каталог 05/2026",
                "CURRENT",
                LocalDate.of(2026, 4, 27),
                LocalDate.of(2026, 5, 17),
                "PUBLISHED",
                true
        );
    }

    private static DigitalCatalogueIssueResponse nextDigitalCatalogue(Audience audience) {
        boolean manager = audience == Audience.CONTENT_MANAGER || audience == Audience.CATALOG_MANAGER;
        return digitalCatalogue(
                "catalog-2026-06",
                "Каталог 06/2026",
                "NEXT",
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 6, 7),
                manager ? "SCHEDULED" : "PUBLISHED",
                true
        );
    }

    private static DigitalCatalogueIssueResponse digitalCatalogue(
            String issueCode,
            String title,
            String periodType,
            LocalDate startDate,
            LocalDate endDate,
            String publicationStatus,
            boolean includeHotspot
    ) {
        List<DigitalCatalogueIssueResponse.Hotspot> hotspots = includeHotspot
                ? List.of(new DigitalCatalogueIssueResponse.Hotspot("BOG-CREAM-001", 22.5, 36.0, 18.0, 12.0))
                : List.of();
        return new DigitalCatalogueIssueResponse(
                issueCode,
                title,
                periodType,
                new DigitalCatalogueIssueResponse.Period(startDate, endDate),
                publicationStatus,
                new DigitalCatalogueIssueResponse.ViewerCapabilities(true, true, true),
                List.of(
                        new DigitalCatalogueIssueResponse.Page(1, "/assets/catalogues/" + issueCode + "/page-1.jpg", "/assets/catalogues/" + issueCode + "/thumb-1.jpg", 1240, 1754, hotspots),
                        new DigitalCatalogueIssueResponse.Page(2, "/assets/catalogues/" + issueCode + "/page-2.jpg", "/assets/catalogues/" + issueCode + "/thumb-2.jpg", 1240, 1754, hotspots)
                ),
                List.of(
                        new DigitalCatalogueIssueResponse.Material("catalog-current-pdf", "MAIN_CATALOG", title + " PDF", 4_200_000L, publicationStatus, "/assets/catalogues/" + issueCode + "/catalog.pdf", new DigitalCatalogueIssueResponse.MaterialActions(true, true, true)),
                        new DigitalCatalogueIssueResponse.Material(issueCode + "-brochure", "BROCHURE", "Брошюра кампании", 920_000L, publicationStatus, "/assets/catalogues/" + issueCode + "/brochure.pdf", new DigitalCatalogueIssueResponse.MaterialActions(true, true, true))
                )
        );
    }

    private static DigitalCatalogueIssueResponse.Material findMaterial(String materialId) {
        return java.util.stream.Stream.concat(
                        currentDigitalCatalogue(Audience.CUSTOMER).materials().stream(),
                        nextDigitalCatalogue(Audience.CUSTOMER).materials().stream()
                )
                .filter(material -> material.materialId().equalsIgnoreCase(materialId))
                .findFirst()
                .orElseThrow(() -> new DigitalCatalogueNotFoundException(DIGITAL_MATERIAL_UNAVAILABLE_CODE));
    }

    private static DigitalCatalogueMaterialActionResponse materialAction(String materialId, String action) {
        return new DigitalCatalogueMaterialActionResponse(
                "/assets/catalogues/actions/" + materialId + "/" + action + "?token=test",
                OffsetDateTime.now().plusMinutes(15),
                DIGITAL_MATERIAL_READY_CODE
        );
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
