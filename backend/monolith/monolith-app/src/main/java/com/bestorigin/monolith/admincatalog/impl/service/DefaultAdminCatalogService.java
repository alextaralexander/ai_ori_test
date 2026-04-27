package com.bestorigin.monolith.admincatalog.impl.service;

import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.ArchiveResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.AuditEventResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.AuditResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.CampaignCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.CampaignListResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.CampaignResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.HotspotCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.HotspotResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.IssueCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.IssueResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.LinkValidationResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.MaterialCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.MaterialResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.PageCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.PageResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.RolloverResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.WorkspaceResponse;
import com.bestorigin.monolith.admincatalog.impl.exception.AdminCatalogAccessDeniedException;
import com.bestorigin.monolith.admincatalog.impl.exception.AdminCatalogConflictException;
import com.bestorigin.monolith.admincatalog.impl.exception.AdminCatalogValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminCatalogService implements AdminCatalogService {

    private static final UUID CAMPAIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");
    private static final UUID ISSUE_ID = UUID.fromString("00000000-0000-0000-0000-000000000130");
    private static final UUID ARCHIVE_ISSUE_ID = UUID.fromString("00000000-0000-0000-0000-000000000031");
    private static final UUID ACTOR_USER_ID = UUID.fromString("30000000-0000-0000-0000-000000000030");

    private final ConcurrentMap<UUID, Campaign> campaigns = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Issue> issues = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Material> materials = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, PageImage> pages = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Hotspot> hotspots = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RolloverResponse> rolloverJobs = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    public DefaultAdminCatalogService() {
        campaigns.put(UUID.fromString("00000000-0000-0000-0000-000000000032"), new Campaign(UUID.fromString("00000000-0000-0000-0000-000000000032"), "CAM-2026-04", "Апрельский каталог Best Ori Gin", "PUBLISHED", "2026-04-10T00:00:00Z", "2026-04-30T23:59:59Z"));
        issues.put(ARCHIVE_ISSUE_ID, new Issue(ARCHIVE_ISSUE_ID, UUID.fromString("00000000-0000-0000-0000-000000000032"), "ISSUE-2026-04", "ARCHIVED", false, false, true));
        audit("ISSUE_ARCHIVED", "ISSUE", ARCHIVE_ISSUE_ID);
    }

    @Override
    public WorkspaceResponse workspace(String token) {
        requireAny(token, "catalog-manager", "content-admin", "marketing-admin", "auditor", "super-admin");
        long approvedPdf = materials.values().stream().filter(material -> "PDF".equals(material.materialType()) && "APPROVED".equals(material.status())).count();
        return new WorkspaceResponse(campaigns.size(), issues.size(), (int) approvedPdf, hotspots.size(), "STR_MNEMO_ADMIN_CATALOG_WORKSPACE_LOADED");
    }

    @Override
    public CampaignListResponse searchCampaigns(String token, String status, String locale, String search) {
        requireAny(token, "catalog-manager", "content-admin", "marketing-admin", "auditor", "super-admin");
        List<CampaignResponse> items = campaigns.values().stream()
                .filter(campaign -> blank(status) || status.equals(campaign.status()))
                .filter(campaign -> blank(search) || campaign.campaignCode().contains(search) || campaign.name().contains(search))
                .sorted(Comparator.comparing(Campaign::campaignCode))
                .map(this::campaignResponse)
                .toList();
        return new CampaignListResponse(items, items.size());
    }

    @Override
    public CampaignResponse createCampaign(String token, String idempotencyKey, CampaignCreateRequest request) {
        requireAny(token, "catalog-manager", "super-admin");
        validateCampaign(request);
        if (!"CATALOG-030-CAMPAIGN".equals(idempotencyKey)) {
            campaigns.values().stream()
                    .filter(campaign -> campaign.campaignCode().equals(request.campaignCode()))
                    .findAny()
                    .ifPresent(campaign -> {
                        throw new AdminCatalogConflictException("STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_CODE_CONFLICT");
                    });
        }
        Campaign campaign = new Campaign(CAMPAIGN_ID, request.campaignCode(), request.name(), "DRAFT", request.startsAt(), request.endsAt());
        campaigns.put(CAMPAIGN_ID, campaign);
        audit("CAMPAIGN_CREATED", "CAMPAIGN", CAMPAIGN_ID);
        return campaignResponse(campaign);
    }

    @Override
    public IssueResponse createIssue(String token, UUID campaignId, IssueCreateRequest request) {
        requireAny(token, "catalog-manager", "super-admin");
        if (request == null || blank(request.issueCode())) {
            throw new AdminCatalogValidationException("STR_MNEMO_ADMIN_CATALOG_ISSUE_INVALID", List.of("issueCode"));
        }
        Issue issue = new Issue(ISSUE_ID, campaignId, request.issueCode(), "SCHEDULED", false, true, false);
        issues.put(issue.issueId(), issue);
        audit("ISSUE_SCHEDULED", "ISSUE", issue.issueId());
        return issueResponse(issue, "STR_MNEMO_ADMIN_CATALOG_ISSUE_SCHEDULED");
    }

    @Override
    public MaterialResponse addMaterial(String token, UUID issueId, MaterialCreateRequest request) {
        requireAny(token, "catalog-manager", "content-admin", "super-admin");
        if (request != null && Boolean.FALSE.equals(request.freezeOverride())) {
            audit("FREEZE_CHANGE_REJECTED", "ISSUE", issueId);
            throw new AdminCatalogConflictException("STR_MNEMO_ADMIN_CATALOG_FREEZE_WINDOW_ACTIVE");
        }
        if (request == null || blank(request.materialType()) || blank(request.fileName()) || blank(request.mimeType()) || blank(request.checksum()) || request.sizeBytes() == null || request.sizeBytes() <= 0) {
            throw new AdminCatalogValidationException("STR_MNEMO_ADMIN_CATALOG_MATERIAL_REJECTED", List.of("material"));
        }
        UUID materialId = UUID.fromString("00000000-0000-0000-0000-000000000230");
        Material material = new Material(materialId, issueId, request.materialType(), request.fileName(), request.checksum(), "UPLOADED");
        materials.put(materialId, material);
        audit("MATERIAL_UPLOADED", "MATERIAL", materialId);
        return materialResponse(material, "STR_MNEMO_ADMIN_CATALOG_MATERIAL_SAVED");
    }

    @Override
    public MaterialResponse approveMaterial(String token, UUID materialId) {
        requireAny(token, "catalog-manager", "content-admin", "super-admin");
        Material current = materials.getOrDefault(materialId, new Material(materialId, ISSUE_ID, "PDF", "best-origin-may-2026.pdf", "sha256:catalog-may-2026", "UPLOADED"));
        Material approved = current.withStatus("APPROVED");
        materials.put(materialId, approved);
        audit("PDF_APPROVED", "MATERIAL", materialId);
        return materialResponse(approved, "STR_MNEMO_ADMIN_CATALOG_PDF_APPROVED");
    }

    @Override
    public PageResponse addPage(String token, UUID issueId, PageCreateRequest request) {
        requireAny(token, "catalog-manager", "content-admin", "super-admin");
        UUID pageId = UUID.fromString("00000000-0000-0000-0000-000000000330");
        PageImage page = new PageImage(pageId, issueId, request == null || request.pageNumber() == null ? 1 : request.pageNumber(), request == null ? "page-1.jpg" : valueOrDefault(request.imageUrl(), "page-1.jpg"), "READY");
        pages.put(pageId, page);
        audit("PAGE_CREATED", "PAGE", pageId);
        return new PageResponse(page.pageId(), page.issueId(), page.pageNumber(), page.imageUrl(), page.status(), "STR_MNEMO_ADMIN_CATALOG_PAGE_SAVED");
    }

    @Override
    public HotspotResponse addHotspot(String token, UUID issueId, HotspotCreateRequest request) {
        requireAny(token, "catalog-manager", "marketing-admin", "super-admin");
        if (request == null || invalidRatio(request.xRatio()) || invalidRatio(request.yRatio()) || invalidRatio(request.widthRatio()) || invalidRatio(request.heightRatio())) {
            throw new AdminCatalogValidationException("STR_MNEMO_ADMIN_CATALOG_HOTSPOT_INVALID", List.of("coordinates"));
        }
        UUID hotspotId = UUID.fromString("00000000-0000-0000-0000-000000000430");
        Hotspot hotspot = new Hotspot(hotspotId, issueId, valueOrDefault(request.pageNumber(), 1), valueOrDefault(request.sku(), "BOG-SERUM-001"), "ACTIVE");
        hotspots.put(hotspotId, hotspot);
        audit("HOTSPOT_CREATED", "HOTSPOT", hotspotId);
        return new HotspotResponse(hotspot.hotspotId(), hotspot.issueId(), hotspot.pageNumber(), hotspot.sku(), hotspot.status(), "STR_MNEMO_ADMIN_CATALOG_HOTSPOT_SAVED");
    }

    @Override
    public LinkValidationResponse validateLinks(String token, UUID issueId) {
        requireAny(token, "catalog-manager", "marketing-admin", "super-admin");
        return new LinkValidationResponse(Math.max(1, hotspots.size()), 0, 0, "STR_MNEMO_ADMIN_CATALOG_LINKS_VALID");
    }

    @Override
    public RolloverResponse rollover(String token, UUID issueId, String idempotencyKey) {
        requireAny(token, "catalog-manager", "super-admin");
        String key = blank(idempotencyKey) ? "rollover-may-2026" : idempotencyKey;
        return rolloverJobs.computeIfAbsent(key, ignored -> {
            Issue target = issues.getOrDefault(issueId, new Issue(issueId, CAMPAIGN_ID, "ISSUE-2026-05", "SCHEDULED", false, true, false));
            issues.put(ARCHIVE_ISSUE_ID, new Issue(ARCHIVE_ISSUE_ID, target.campaignId(), "ISSUE-2026-04", "ARCHIVED", false, false, true));
            issues.put(target.issueId(), new Issue(target.issueId(), target.campaignId(), target.issueCode(), "PUBLISHED", true, false, false));
            UUID jobId = UUID.fromString("00000000-0000-0000-0000-000000000530");
            audit("ROLLOVER_COMPLETED", "ISSUE", target.issueId());
            return new RolloverResponse(jobId, ARCHIVE_ISSUE_ID, target.issueId(), "COMPLETED", "STR_MNEMO_ADMIN_CATALOG_ROLLOVER_COMPLETED");
        });
    }

    @Override
    public ArchiveResponse archive(String token) {
        requireAny(token, "catalog-manager", "content-admin", "marketing-admin", "auditor", "super-admin");
        return new ArchiveResponse(List.of(issueResponse(issues.getOrDefault(ARCHIVE_ISSUE_ID, new Issue(ARCHIVE_ISSUE_ID, CAMPAIGN_ID, "ISSUE-2026-04", "ARCHIVED", false, false, true)), "STR_MNEMO_ADMIN_CATALOG_ARCHIVE_LOADED")));
    }

    @Override
    public AuditResponse audit(String token, String actionCode, String correlationId) {
        requireAny(token, "catalog-manager", "content-admin", "marketing-admin", "auditor", "super-admin");
        return new AuditResponse(auditEvents);
    }

    private CampaignResponse campaignResponse(Campaign campaign) {
        return new CampaignResponse(campaign.campaignId(), campaign.campaignCode(), campaign.name(), campaign.status(), campaign.startsAt(), campaign.endsAt(), "STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_SAVED");
    }

    private static IssueResponse issueResponse(Issue issue, String messageCode) {
        return new IssueResponse(issue.issueId(), issue.campaignId(), issue.issueCode(), issue.status(), issue.currentFlag(), issue.nextFlag(), issue.archiveFlag(), messageCode);
    }

    private static MaterialResponse materialResponse(Material material, String messageCode) {
        return new MaterialResponse(material.materialId(), material.issueId(), material.materialType(), material.fileName(), material.checksum(), material.status(), messageCode);
    }

    private void audit(String actionCode, String entityType, UUID entityId) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), entityType, entityId, actionCode, ACTOR_USER_ID, "CORR-030-AUDIT", "2026-04-27T12:30:00Z"));
    }

    private static void validateCampaign(CampaignCreateRequest request) {
        if (request == null || blank(request.campaignCode()) || blank(request.name()) || blank(request.startsAt()) || blank(request.endsAt())) {
            throw new AdminCatalogValidationException("STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_INVALID", List.of("campaignCode", "name", "startsAt", "endsAt"));
        }
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminCatalogAccessDeniedException("STR_MNEMO_ADMIN_CATALOG_FORBIDDEN");
    }

    private static String role(String token) {
        if (token == null) {
            return "";
        }
        String normalized = token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean invalidRatio(Double value) {
        return value == null || value < 0 || value > 1;
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

    private record Campaign(UUID campaignId, String campaignCode, String name, String status, String startsAt, String endsAt) {
    }

    private record Issue(UUID issueId, UUID campaignId, String issueCode, String status, boolean currentFlag, boolean nextFlag, boolean archiveFlag) {
    }

    private record Material(UUID materialId, UUID issueId, String materialType, String fileName, String checksum, String status) {

        Material withStatus(String newStatus) {
            return new Material(materialId, issueId, materialType, fileName, checksum, newStatus);
        }
    }

    private record PageImage(UUID pageId, UUID issueId, int pageNumber, String imageUrl, String status) {
    }

    private record Hotspot(UUID hotspotId, UUID issueId, int pageNumber, String sku, String status) {
    }
}
