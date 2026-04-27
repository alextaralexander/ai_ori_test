package com.bestorigin.monolith.admincatalog.api;

import java.util.List;
import java.util.UUID;

public final class AdminCatalogDtos {

    private AdminCatalogDtos() {
    }

    public record AdminCatalogErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record WorkspaceResponse(int campaignCount, int issueCount, int approvedPdfCount, int hotspotCount, String messageCode) {
    }

    public record CampaignCreateRequest(String campaignCode, String name, String locale, String audience, String startsAt, String endsAt) {
    }

    public record CampaignResponse(UUID campaignId, String campaignCode, String name, String status, String startsAt, String endsAt, String messageCode) {
    }

    public record CampaignListResponse(List<CampaignResponse> items, long total) {
    }

    public record IssueCreateRequest(String issueCode, String publicationAt, String archiveAt, String freezeStartsAt, String rolloverWindowStartsAt, String rolloverWindowEndsAt) {
    }

    public record IssueResponse(UUID issueId, UUID campaignId, String issueCode, String status, boolean currentFlag, boolean nextFlag, boolean archiveFlag, String messageCode) {
    }

    public record MaterialCreateRequest(String materialType, String fileName, String mimeType, Long sizeBytes, String checksum, String storageKey, Boolean freezeOverride) {
    }

    public record MaterialResponse(UUID materialId, UUID issueId, String materialType, String fileName, String checksum, String status, String messageCode) {
    }

    public record PageCreateRequest(Integer pageNumber, String imageUrl, Integer widthPx, Integer heightPx) {
    }

    public record PageResponse(UUID pageId, UUID issueId, int pageNumber, String imageUrl, String status, String messageCode) {
    }

    public record HotspotCreateRequest(Integer pageNumber, UUID productId, String sku, String promoCode, Double xRatio, Double yRatio, Double widthRatio, Double heightRatio) {
    }

    public record HotspotResponse(UUID hotspotId, UUID issueId, int pageNumber, String sku, String status, String messageCode) {
    }

    public record LinkValidationResponse(int validHotspots, int warningHotspots, int blockedHotspots, String messageCode) {
    }

    public record RolloverResponse(UUID rolloverJobId, UUID sourceIssueId, UUID targetIssueId, String status, String messageCode) {
    }

    public record ArchiveResponse(List<IssueResponse> items) {
    }

    public record AuditResponse(List<AuditEventResponse> items) {
    }

    public record AuditEventResponse(UUID auditEventId, String entityType, UUID entityId, String actionCode, UUID actorUserId, String correlationId, String occurredAt) {
    }
}
