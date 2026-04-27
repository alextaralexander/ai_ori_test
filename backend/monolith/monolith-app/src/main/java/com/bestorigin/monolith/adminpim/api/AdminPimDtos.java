package com.bestorigin.monolith.adminpim.api;

import java.util.List;
import java.util.UUID;

public final class AdminPimDtos {

    private AdminPimDtos() {
    }

    public record AdminPimErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record WorkspaceResponse(int categoryCount, int productCount, int mediaCount, int draftProductCount, String messageCode) {
    }

    public record CategoryUpsertRequest(
            UUID parentId,
            String slug,
            String locale,
            String name,
            String description,
            String audience,
            Integer sortOrder,
            String activeFrom,
            String activeTo
    ) {
    }

    public record CategoryResponse(
            UUID categoryId,
            UUID parentId,
            String slug,
            String locale,
            String name,
            String status,
            int sortOrder,
            String messageCode
    ) {
    }

    public record CategoryListResponse(List<CategoryResponse> items, long total) {
    }

    public record ProductUpsertRequest(
            String sku,
            String articleCode,
            String brandCode,
            String locale,
            String name,
            String shortDescription,
            String description,
            String composition,
            String usageInstructions,
            String restrictions,
            List<UUID> categoryIds,
            List<String> tags
    ) {
    }

    public record ProductSummaryResponse(
            UUID productId,
            String sku,
            String articleCode,
            String name,
            String status,
            boolean readyForPublication,
            boolean mainImageReady
    ) {
    }

    public record ProductListResponse(List<ProductSummaryResponse> items, long total) {
    }

    public record ProductDetailResponse(
            ProductSummaryResponse product,
            List<String> validationChecklist,
            List<MediaResponse> media,
            String messageCode
    ) {
    }

    public record PublishRequest(String versionComment) {
    }

    public record MediaCreateRequest(
            String usageType,
            String fileName,
            String mimeType,
            Long sizeBytes,
            String checksum,
            String altText,
            String locale,
            String fileReferenceId
    ) {
    }

    public record MediaResponse(
            UUID mediaId,
            String usageType,
            String fileName,
            String mimeType,
            String checksum,
            String status,
            String messageCode
    ) {
    }

    public record AttributeDefinitionRequest(
            String attributeCode,
            String displayName,
            String valueType,
            boolean filterable,
            boolean searchable,
            List<String> allowedValues
    ) {
    }

    public record AttributeDefinitionResponse(UUID attributeId, String attributeCode, String valueType, String messageCode) {
    }

    public record RecommendationRequest(UUID sourceProductId, UUID targetProductId, String relationType, Integer sortOrder) {
    }

    public record RecommendationResponse(UUID recommendationId, String relationType, String messageCode) {
    }

    public record ImportRequest(String jobCode, String dataType, String sourceFileName) {
    }

    public record ImportJobResponse(UUID importJobId, String status, int rowCount, int appliedCount, int errorCount, String messageCode) {
    }

    public record ExportRequest(UUID categoryId, String status) {
    }

    public record ExportJobResponse(UUID exportJobId, String status, int productCount, int categoryCount, String checksum, String messageCode) {
    }

    public record AuditResponse(List<AuditEventResponse> items) {
    }

    public record AuditEventResponse(
            UUID auditEventId,
            String entityType,
            UUID entityId,
            String actionCode,
            UUID actorUserId,
            String correlationId,
            String occurredAt
    ) {
    }
}
