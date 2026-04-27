package com.bestorigin.monolith.admincms.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminCmsDtos {

    private AdminCmsDtos() {
    }

    public record AdminCmsErrorResponse(String messageCode, String correlationId) {
    }

    public record MaterialListResponse(List<MaterialSummaryResponse> items, long total) {
    }

    public record MaterialSummaryResponse(
            UUID materialId,
            String materialType,
            String language,
            String slug,
            String title,
            String status,
            String publishAt,
            String unpublishAt,
            UUID reviewerUserId,
            String updatedAt
    ) {
    }

    public record MaterialDetailResponse(
            MaterialSummaryResponse material,
            MaterialVersionResponse activeVersion,
            boolean auditRecorded,
            String messageCode
    ) {
    }

    public record MaterialVersionResponse(
            UUID versionId,
            int versionNumber,
            List<ContentBlockRequest> blocks,
            SeoMetadataRequest seo,
            DocumentMetadataRequest document
    ) {
    }

    public record MaterialUpsertRequest(
            String materialType,
            String language,
            String slug,
            String title,
            String summary,
            String audience,
            List<ContentBlockRequest> blocks,
            SeoMetadataRequest seo,
            DocumentMetadataRequest document
    ) {
    }

    public record ContentBlockRequest(String blockType, int sortOrder, Map<String, Object> payload) {
    }

    public record SeoMetadataRequest(
            String slug,
            String title,
            String description,
            String canonicalUrl,
            String robotsPolicy,
            String breadcrumbTitle
    ) {
    }

    public record DocumentMetadataRequest(
            String documentType,
            String versionLabel,
            String effectiveFrom,
            Boolean required,
            String attachmentFileId,
            String checksum
    ) {
    }

    public record ReviewRequest(String decision, String comment) {
    }

    public record PublishRequest(String publishAt, String unpublishAt) {
    }

    public record PreviewResponse(UUID materialId, UUID versionId, Map<String, Object> renderModel) {
    }

    public record VersionListResponse(List<MaterialVersionResponse> items) {
    }

    public record AuditResponse(List<AuditEventResponse> items) {
    }

    public record AuditEventResponse(
            UUID eventId,
            UUID materialId,
            UUID versionId,
            UUID actorUserId,
            String actionCode,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String sourceRoute,
            String correlationId,
            String occurredAt
    ) {
    }
}
