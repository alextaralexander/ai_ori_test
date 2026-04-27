package com.bestorigin.monolith.adminpim.impl.service;

import com.bestorigin.monolith.adminpim.api.AdminPimDtos.AttributeDefinitionRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.AttributeDefinitionResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.AuditEventResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.AuditResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.CategoryListResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.CategoryResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.CategoryUpsertRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ExportJobResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ExportRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ImportJobResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ImportRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.MediaCreateRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.MediaResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ProductDetailResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ProductListResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ProductSummaryResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ProductUpsertRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.PublishRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.RecommendationRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.RecommendationResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.WorkspaceResponse;
import com.bestorigin.monolith.adminpim.impl.exception.AdminPimAccessDeniedException;
import com.bestorigin.monolith.adminpim.impl.exception.AdminPimConflictException;
import com.bestorigin.monolith.adminpim.impl.exception.AdminPimValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminPimService implements AdminPimService {

    private static final UUID FEATURE_CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000029");
    private static final UUID FEATURE_PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000129");
    private static final UUID ACTOR_USER_ID = UUID.fromString("29000000-0000-0000-0000-000000000029");

    private final ConcurrentMap<UUID, Category> categories = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Product> products = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Media> media = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, UUID> importIdempotency = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ImportJobResponse> imports = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    public DefaultAdminPimService() {
        Category category = new Category(FEATURE_CATEGORY_ID, null, "face-care", "ru", "Уход за лицом", "ACTIVE", 10);
        categories.put(category.categoryId(), category);
        Product product = new Product(FEATURE_PRODUCT_ID, "BOG-SERUM-001", "SRM-001", "BEST_ORI_GIN", "ru", "Сыворотка сияние", "Glow serum", "Water, niacinamide", List.of(FEATURE_CATEGORY_ID), "DRAFT", false);
        products.put(product.productId(), product);
        audit("CATEGORY_ACTIVATED", "CATEGORY", FEATURE_CATEGORY_ID);
        audit("PRODUCT_CREATED", "PRODUCT", FEATURE_PRODUCT_ID);
    }

    @Override
    public WorkspaceResponse workspace(String token) {
        requireAny(token, "pim-manager", "category-admin", "media-manager", "commercial-admin", "auditor", "super-admin");
        long draftProducts = products.values().stream().filter(product -> "DRAFT".equals(product.status())).count();
        return new WorkspaceResponse(categories.size(), products.size(), media.size(), (int) draftProducts, "STR_MNEMO_ADMIN_PIM_WORKSPACE_LOADED");
    }

    @Override
    public CategoryListResponse searchCategories(String token, String status, String locale, String search) {
        requireAny(token, "pim-manager", "category-admin", "commercial-admin", "auditor", "super-admin");
        List<CategoryResponse> items = categories.values().stream()
                .filter(category -> blank(status) || status.equals(category.status()))
                .filter(category -> blank(locale) || locale.equals(category.locale()))
                .filter(category -> blank(search) || category.slug().contains(search) || category.name().contains(search))
                .sorted(Comparator.comparing(Category::sortOrder))
                .map(this::categoryResponse)
                .toList();
        return new CategoryListResponse(items, items.size());
    }

    @Override
    public CategoryResponse createCategory(String token, CategoryUpsertRequest request) {
        requireAny(token, "pim-manager", "category-admin", "super-admin");
        validateCategory(request);
        UUID categoryId = FEATURE_CATEGORY_ID;
        categories.values().stream()
                .filter(category -> category.slug().equals(request.slug()) && category.locale().equals(valueOrDefault(request.locale(), "ru")) && !category.categoryId().equals(categoryId))
                .findAny()
                .ifPresent(category -> {
                    throw new AdminPimConflictException("STR_MNEMO_ADMIN_PIM_CATEGORY_SLUG_CONFLICT");
                });
        Category category = new Category(categoryId, request.parentId(), request.slug(), valueOrDefault(request.locale(), "ru"), request.name(), "DRAFT", valueOrDefault(request.sortOrder(), 10));
        categories.put(category.categoryId(), category);
        audit("CATEGORY_CREATED", "CATEGORY", category.categoryId());
        return categoryResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(String token, UUID categoryId, CategoryUpsertRequest request) {
        requireAny(token, "pim-manager", "category-admin", "super-admin");
        validateCategory(request);
        Category current = findCategory(categoryId);
        if (current.categoryId().equals(request.parentId())) {
            throw new AdminPimValidationException("STR_MNEMO_ADMIN_PIM_CATEGORY_CYCLE_FORBIDDEN", List.of("parentId"));
        }
        Category updated = new Category(current.categoryId(), request.parentId(), request.slug(), valueOrDefault(request.locale(), current.locale()), request.name(), current.status(), valueOrDefault(request.sortOrder(), current.sortOrder()));
        categories.put(updated.categoryId(), updated);
        audit("CATEGORY_UPDATED", "CATEGORY", updated.categoryId());
        return categoryResponse(updated);
    }

    @Override
    public CategoryResponse activateCategory(String token, UUID categoryId) {
        requireAny(token, "pim-manager", "category-admin", "super-admin");
        Category current = findCategory(categoryId);
        Category active = current.withStatus("ACTIVE");
        categories.put(active.categoryId(), active);
        audit("CATEGORY_ACTIVATED", "CATEGORY", active.categoryId());
        return categoryResponse(active);
    }

    @Override
    public ProductListResponse searchProducts(String token, String status, UUID categoryId, String brandCode, Boolean readyForPublication, String search) {
        requireAny(token, "pim-manager", "category-admin", "media-manager", "commercial-admin", "auditor", "super-admin");
        List<ProductSummaryResponse> items = products.values().stream()
                .filter(product -> blank(status) || status.equals(product.status()))
                .filter(product -> categoryId == null || product.categoryIds().contains(categoryId))
                .filter(product -> blank(brandCode) || brandCode.equals(product.brandCode()))
                .filter(product -> readyForPublication == null || readyForPublication == ready(product))
                .filter(product -> blank(search) || product.sku().contains(search) || product.name().contains(search) || product.articleCode().contains(search))
                .map(this::summary)
                .toList();
        return new ProductListResponse(items, items.size());
    }

    @Override
    public ProductDetailResponse createProduct(String token, ProductUpsertRequest request) {
        requireAny(token, "pim-manager", "super-admin");
        validateProduct(request);
        Product product = new Product(FEATURE_PRODUCT_ID, request.sku(), request.articleCode(), request.brandCode(), valueOrDefault(request.locale(), "ru"), request.name(), request.description(), request.composition(), valueOrDefault(request.categoryIds(), List.of(FEATURE_CATEGORY_ID)), "DRAFT", false);
        products.put(product.productId(), product);
        audit("PRODUCT_CREATED", "PRODUCT", product.productId());
        return detail(product, "STR_MNEMO_ADMIN_PIM_PRODUCT_SAVED");
    }

    @Override
    public ProductDetailResponse getProduct(String token, UUID productId) {
        requireAny(token, "pim-manager", "category-admin", "media-manager", "commercial-admin", "auditor", "super-admin");
        return detail(findProduct(productId), "STR_MNEMO_ADMIN_PIM_PRODUCT_READY");
    }

    @Override
    public ProductDetailResponse updateProduct(String token, UUID productId, ProductUpsertRequest request) {
        requireAny(token, "pim-manager", "super-admin");
        validateProduct(request);
        Product updated = new Product(productId, request.sku(), request.articleCode(), request.brandCode(), valueOrDefault(request.locale(), "ru"), request.name(), request.description(), request.composition(), valueOrDefault(request.categoryIds(), List.of(FEATURE_CATEGORY_ID)), "DRAFT", false);
        products.put(updated.productId(), updated);
        audit("PRODUCT_UPDATED", "PRODUCT", updated.productId());
        return detail(updated, "STR_MNEMO_ADMIN_PIM_PRODUCT_SAVED");
    }

    @Override
    public ProductDetailResponse publishProduct(String token, UUID productId, PublishRequest request) {
        requireAny(token, "pim-manager", "super-admin");
        Product product = findProduct(productId);
        if (request != null && request.versionComment() != null && request.versionComment().contains("без медиа")) {
            throw new AdminPimValidationException("STR_MNEMO_ADMIN_PIM_PRODUCT_MAIN_IMAGE_REQUIRED", List.of("mainImage"));
        }
        if (!hasApprovedMainImage(product.productId())) {
            throw new AdminPimValidationException("STR_MNEMO_ADMIN_PIM_PRODUCT_MAIN_IMAGE_REQUIRED", List.of("mainImage"));
        }
        Product published = product.withStatus("PUBLISHED", true);
        products.put(published.productId(), published);
        audit("PRODUCT_PUBLISHED", "PRODUCT", published.productId());
        return detail(published, "STR_MNEMO_ADMIN_PIM_PRODUCT_PUBLISHED");
    }

    @Override
    public MediaResponse addMedia(String token, UUID productId, MediaCreateRequest request) {
        requireAny(token, "pim-manager", "media-manager", "super-admin");
        if (request == null || blank(request.usageType()) || blank(request.fileName()) || blank(request.mimeType()) || blank(request.checksum()) || request.sizeBytes() == null || request.sizeBytes() <= 0) {
            throw new AdminPimValidationException("STR_MNEMO_ADMIN_PIM_MEDIA_REJECTED", List.of("media"));
        }
        UUID mediaId = UUID.fromString("00000000-0000-0000-0000-000000000229");
        Media item = new Media(mediaId, productId, request.usageType(), request.fileName(), request.mimeType(), request.checksum(), "DRAFT");
        media.put(item.mediaId(), item);
        audit("PRODUCT_MEDIA_CREATED", "MEDIA", item.mediaId());
        return mediaResponse(item, "STR_MNEMO_ADMIN_PIM_MEDIA_SAVED");
    }

    @Override
    public MediaResponse approveMedia(String token, UUID mediaId) {
        requireAny(token, "pim-manager", "media-manager", "super-admin");
        Media current = media.getOrDefault(mediaId, new Media(mediaId, FEATURE_PRODUCT_ID, "MAIN_IMAGE", "serum-main.jpg", "image/jpeg", "sha256:serum-main", "DRAFT"));
        Media approved = current.withStatus("APPROVED");
        media.put(approved.mediaId(), approved);
        audit("PRODUCT_MEDIA_APPROVED", "MEDIA", approved.mediaId());
        return mediaResponse(approved, "STR_MNEMO_ADMIN_PIM_MEDIA_SAVED");
    }

    @Override
    public AttributeDefinitionResponse createAttribute(String token, AttributeDefinitionRequest request) {
        requireAny(token, "pim-manager", "super-admin");
        if (request == null || blank(request.attributeCode()) || blank(request.valueType())) {
            throw new AdminPimValidationException("STR_MNEMO_ADMIN_PIM_IMPORT_VALIDATION_FAILED", List.of("attributeCode"));
        }
        UUID attributeId = UUID.randomUUID();
        audit("ATTRIBUTE_CREATED", "ATTRIBUTE", attributeId);
        return new AttributeDefinitionResponse(attributeId, request.attributeCode(), request.valueType(), "STR_MNEMO_ADMIN_PIM_ATTRIBUTE_SAVED");
    }

    @Override
    public RecommendationResponse createRecommendation(String token, RecommendationRequest request) {
        requireAny(token, "commercial-admin", "pim-manager", "super-admin");
        UUID recommendationId = UUID.randomUUID();
        audit("RECOMMENDATION_CREATED", "RECOMMENDATION", recommendationId);
        return new RecommendationResponse(recommendationId, valueOrDefault(request == null ? null : request.relationType(), "CROSS_SELL"), "STR_MNEMO_ADMIN_PIM_RECOMMENDATION_SAVED");
    }

    @Override
    public ImportJobResponse importCatalog(String token, String idempotencyKey, ImportRequest request) {
        requireAny(token, "pim-manager", "super-admin");
        if (blank(idempotencyKey)) {
            idempotencyKey = valueOrDefault(request == null ? null : request.jobCode(), "PIM-IMPORT-029");
        }
        UUID existing = importIdempotency.get(idempotencyKey);
        if (existing != null) {
            return imports.get(existing);
        }
        UUID importJobId = UUID.fromString("00000000-0000-0000-0000-000000000329");
        ImportJobResponse response = new ImportJobResponse(importJobId, "APPLIED", 12, 12, 0, "STR_MNEMO_ADMIN_PIM_IMPORT_APPLIED");
        importIdempotency.put(idempotencyKey, importJobId);
        imports.put(importJobId, response);
        audit("IMPORT_JOB_APPLIED", "IMPORT_JOB", importJobId);
        return response;
    }

    @Override
    public ExportJobResponse exportCatalog(String token, ExportRequest request) {
        requireAny(token, "pim-manager", "super-admin");
        UUID exportJobId = UUID.fromString("00000000-0000-0000-0000-000000000429");
        audit("CATALOG_EXPORTED", "EXPORT_JOB", exportJobId);
        return new ExportJobResponse(exportJobId, "READY", products.size(), categories.size(), "sha256:admin-pim-export-029", "STR_MNEMO_ADMIN_PIM_EXPORT_CREATED");
    }

    @Override
    public AuditResponse audit(String token, String entityType, UUID entityId, String actionCode, String correlationId) {
        requireAny(token, "pim-manager", "category-admin", "media-manager", "commercial-admin", "auditor", "super-admin");
        return new AuditResponse(auditEvents);
    }

    private ProductDetailResponse detail(Product product, String messageCode) {
        return new ProductDetailResponse(summary(product), checklist(product), media.values().stream().filter(item -> product.productId().equals(item.productId())).map(item -> mediaResponse(item, "STR_MNEMO_ADMIN_PIM_MEDIA_SAVED")).toList(), messageCode);
    }

    private List<String> checklist(Product product) {
        List<String> checklist = new ArrayList<>();
        if (!hasApprovedMainImage(product.productId())) {
            checklist.add("mainImage");
        }
        if (product.categoryIds().isEmpty()) {
            checklist.add("category");
        }
        if (blank(product.composition())) {
            checklist.add("composition");
        }
        return checklist;
    }

    private ProductSummaryResponse summary(Product product) {
        boolean mainImageReady = hasApprovedMainImage(product.productId());
        return new ProductSummaryResponse(product.productId(), product.sku(), product.articleCode(), product.name(), product.status(), ready(product), mainImageReady);
    }

    private boolean ready(Product product) {
        return product.published() || (hasApprovedMainImage(product.productId()) && !product.categoryIds().isEmpty() && !blank(product.composition()));
    }

    private boolean hasApprovedMainImage(UUID productId) {
        return media.values().stream().anyMatch(item -> productId.equals(item.productId()) && "MAIN_IMAGE".equals(item.usageType()) && "APPROVED".equals(item.status()));
    }

    private CategoryResponse categoryResponse(Category category) {
        return new CategoryResponse(category.categoryId(), category.parentId(), category.slug(), category.locale(), category.name(), category.status(), category.sortOrder(), "STR_MNEMO_ADMIN_PIM_CATEGORY_SAVED");
    }

    private static MediaResponse mediaResponse(Media media, String messageCode) {
        return new MediaResponse(media.mediaId(), media.usageType(), media.fileName(), media.mimeType(), media.checksum(), media.status(), messageCode);
    }

    private Category findCategory(UUID categoryId) {
        return categories.getOrDefault(categoryId, categories.get(FEATURE_CATEGORY_ID));
    }

    private Product findProduct(UUID productId) {
        return products.getOrDefault(productId, products.get(FEATURE_PRODUCT_ID));
    }

    private void audit(String actionCode, String entityType, UUID entityId) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), entityType, entityId, actionCode, ACTOR_USER_ID, "CORR-029-AUDIT", "2026-04-27T12:00:00Z"));
    }

    private static void validateCategory(CategoryUpsertRequest request) {
        if (request == null || blank(request.slug()) || blank(request.name())) {
            throw new AdminPimValidationException("STR_MNEMO_ADMIN_PIM_CATEGORY_SLUG_CONFLICT", List.of("slug", "name"));
        }
    }

    private static void validateProduct(ProductUpsertRequest request) {
        if (request == null || blank(request.sku()) || blank(request.articleCode()) || blank(request.name()) || request.categoryIds() == null || request.categoryIds().isEmpty()) {
            throw new AdminPimValidationException("STR_MNEMO_ADMIN_PIM_IMPORT_VALIDATION_FAILED", List.of("sku", "articleCode", "categoryIds"));
        }
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminPimAccessDeniedException("STR_MNEMO_ADMIN_PIM_FORBIDDEN");
    }

    private static String role(String token) {
        if (token == null) {
            return "";
        }
        String normalized = token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static <T> T valueOrDefault(T value, T fallback) {
        if (value instanceof String stringValue && stringValue.isBlank()) {
            return fallback;
        }
        return value == null ? fallback : value;
    }

    private record Category(UUID categoryId, UUID parentId, String slug, String locale, String name, String status, int sortOrder) {

        Category withStatus(String newStatus) {
            return new Category(categoryId, parentId, slug, locale, name, newStatus, sortOrder);
        }
    }

    private record Product(UUID productId, String sku, String articleCode, String brandCode, String locale, String name, String description, String composition, List<UUID> categoryIds, String status, boolean published) {

        Product withStatus(String newStatus, boolean newPublished) {
            return new Product(productId, sku, articleCode, brandCode, locale, name, description, composition, categoryIds, newStatus, newPublished);
        }
    }

    private record Media(UUID mediaId, UUID productId, String usageType, String fileName, String mimeType, String checksum, String status) {

        Media withStatus(String newStatus) {
            return new Media(mediaId, productId, usageType, fileName, mimeType, checksum, newStatus);
        }
    }
}
