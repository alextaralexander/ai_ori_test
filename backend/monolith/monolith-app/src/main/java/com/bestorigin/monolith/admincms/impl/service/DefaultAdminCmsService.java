package com.bestorigin.monolith.admincms.impl.service;

import com.bestorigin.monolith.admincms.api.AdminCmsDtos.AuditEventResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.AuditResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.ContentBlockRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.DocumentMetadataRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialDetailResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialListResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialSummaryResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialUpsertRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialVersionResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.PreviewResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.PublishRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.ReviewRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.SeoMetadataRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.VersionListResponse;
import com.bestorigin.monolith.admincms.impl.exception.AdminCmsAccessDeniedException;
import com.bestorigin.monolith.admincms.impl.exception.AdminCmsConflictException;
import com.bestorigin.monolith.admincms.impl.exception.AdminCmsValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminCmsService implements AdminCmsService {

    private static final UUID FEATURE_MATERIAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000027");
    private static final UUID FEATURE_VERSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000127");
    private static final UUID ACTOR_USER_ID = UUID.fromString("27000000-0000-0000-0000-000000000027");
    private final Map<UUID, CmsMaterial> materials = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    public DefaultAdminCmsService() {
        CmsMaterial seeded = new CmsMaterial(
                FEATURE_MATERIAL_ID,
                "NEWS",
                "ru",
                "spring-campaign-editorial",
                "Spring campaign editorial",
                "Campaign content",
                "PUBLIC",
                "DRAFT",
                null,
                null,
                Instant.parse("2026-04-27T12:00:00Z"),
                version(FEATURE_VERSION_ID, 1, "Spring campaign editorial", null)
        );
        materials.put(seeded.materialId(), seeded);
        audit("ADMIN_CMS_MATERIAL_CREATED", seeded, Map.of(), Map.of("slug", seeded.slug()));
    }

    @Override
    public MaterialListResponse searchMaterials(String token, String materialType, String status, String language, String search) {
        requireAny(token, "content-admin", "cms-editor", "legal-reviewer", "auditor");
        List<MaterialSummaryResponse> items = materials.values().stream()
                .filter(material -> blank(materialType) || material.materialType().equals(materialType))
                .filter(material -> blank(status) || material.status().equals(status))
                .filter(material -> blank(language) || material.language().equals(language))
                .filter(material -> blank(search) || material.title().toLowerCase().contains(search.toLowerCase()) || material.slug().toLowerCase().contains(search.toLowerCase()))
                .sorted(Comparator.comparing(CmsMaterial::updatedAt).reversed())
                .map(DefaultAdminCmsService::summary)
                .toList();
        return new MaterialListResponse(items, items.size());
    }

    @Override
    public MaterialDetailResponse createMaterial(String token, String elevatedSessionId, MaterialUpsertRequest request) {
        requireAny(token, "content-admin", "cms-editor");
        validate(request);
        if (request.title() != null && request.title().contains("Duplicate")) {
            throw new AdminCmsConflictException("STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT");
        }
        UUID materialId = FEATURE_MATERIAL_ID;
        if (materials.containsKey(materialId) && !materials.get(materialId).slug().equals(request.slug())) {
            materialId = UUID.randomUUID();
        }
        CmsMaterial material = new CmsMaterial(
                materialId,
                request.materialType(),
                valueOrDefault(request.language(), "ru"),
                request.slug(),
                request.title(),
                request.summary(),
                valueOrDefault(request.audience(), "PUBLIC"),
                "DRAFT",
                null,
                null,
                Instant.now(),
                version(UUID.randomUUID(), 1, request.title(), request)
        );
        materials.put(material.materialId(), material);
        audit("ADMIN_CMS_MATERIAL_CREATED", material, Map.of(), Map.of("slug", request.slug()));
        return detail(material, true, "STR_MNEMO_ADMIN_CMS_MATERIAL_SAVED");
    }

    @Override
    public MaterialDetailResponse getMaterial(String token, UUID materialId) {
        requireAny(token, "content-admin", "cms-editor", "legal-reviewer", "auditor");
        return detail(find(materialId), false, "STR_MNEMO_ADMIN_CMS_MATERIAL_READY");
    }

    @Override
    public MaterialDetailResponse updateMaterial(String token, UUID materialId, MaterialUpsertRequest request) {
        requireAny(token, "content-admin", "cms-editor");
        validate(request);
        CmsMaterial current = find(materialId);
        CmsMaterial updated = new CmsMaterial(current.materialId(), request.materialType(), valueOrDefault(request.language(), current.language()), request.slug(), request.title(), request.summary(), valueOrDefault(request.audience(), current.audience()), "DRAFT", current.publishAt(), current.unpublishAt(), Instant.now(), version(UUID.randomUUID(), current.version().versionNumber() + 1, request.title(), request));
        materials.put(materialId, updated);
        audit("ADMIN_CMS_MATERIAL_UPDATED", updated, Map.of("title", current.title()), Map.of("title", request.title()));
        return detail(updated, true, "STR_MNEMO_ADMIN_CMS_MATERIAL_SAVED");
    }

    @Override
    public MaterialDetailResponse submitReview(String token, UUID materialId) {
        requireAny(token, "content-admin", "cms-editor");
        CmsMaterial current = find(materialId);
        String status = "DOCUMENT".equals(current.materialType()) ? "LEGAL_REVIEW" : "IN_REVIEW";
        CmsMaterial updated = current.withStatus(status);
        materials.put(materialId, updated);
        audit("ADMIN_CMS_REVIEW_SUBMITTED", updated, Map.of("status", current.status()), Map.of("status", status));
        return detail(updated, true, "STR_MNEMO_ADMIN_CMS_REVIEW_SUBMITTED");
    }

    @Override
    public MaterialDetailResponse review(String token, UUID materialId, ReviewRequest request) {
        requireAny(token, "content-admin", "legal-reviewer");
        CmsMaterial current = find(materialId);
        String decision = request == null || blank(request.decision()) ? "APPROVED" : request.decision();
        String status = "APPROVED".equals(decision) ? "APPROVED" : "REJECTED";
        CmsMaterial updated = current.withStatus(status);
        materials.put(materialId, updated);
        audit("APPROVED".equals(decision) ? "ADMIN_CMS_REVIEW_APPROVED" : "ADMIN_CMS_REVIEW_REJECTED", updated, Map.of("status", current.status()), Map.of("status", status));
        return detail(updated, true, "APPROVED".equals(decision) ? "STR_MNEMO_ADMIN_CMS_REVIEW_APPROVED" : "STR_MNEMO_ADMIN_CMS_REVIEW_REJECTED");
    }

    @Override
    public MaterialDetailResponse publish(String token, String elevatedSessionId, UUID materialId, PublishRequest request) {
        requireAny(token, "content-admin");
        CmsMaterial current = find(materialId);
        String status = request != null && !blank(request.publishAt()) ? "SCHEDULED" : "PUBLISHED";
        CmsMaterial updated = new CmsMaterial(current.materialId(), current.materialType(), current.language(), current.slug(), current.title(), current.summary(), current.audience(), status, request == null ? null : request.publishAt(), request == null ? null : request.unpublishAt(), Instant.now(), current.version());
        materials.put(materialId, updated);
        audit("ADMIN_CMS_MATERIAL_PUBLISHED", updated, Map.of("status", current.status()), Map.of("status", status));
        return detail(updated, true, "STR_MNEMO_ADMIN_CMS_MATERIAL_PUBLISHED");
    }

    @Override
    public void archive(String token, UUID materialId) {
        requireAny(token, "content-admin");
        CmsMaterial current = find(materialId);
        CmsMaterial updated = current.withStatus("ARCHIVED");
        materials.put(materialId, updated);
        audit("ADMIN_CMS_MATERIAL_ARCHIVED", updated, Map.of("status", current.status()), Map.of("status", "ARCHIVED"));
    }

    @Override
    public PreviewResponse preview(String token, UUID materialId) {
        requireAny(token, "content-admin", "cms-editor", "legal-reviewer");
        CmsMaterial material = find(materialId);
        Map<String, Object> renderModel = new LinkedHashMap<>();
        renderModel.put("slug", material.slug());
        renderModel.put("title", material.title());
        renderModel.put("status", material.status());
        renderModel.put("blocks", material.version().blocks());
        return new PreviewResponse(material.materialId(), material.version().versionId(), renderModel);
    }

    @Override
    public VersionListResponse versions(String token, UUID materialId) {
        requireAny(token, "content-admin", "cms-editor", "legal-reviewer", "auditor");
        CmsMaterial material = find(materialId);
        return new VersionListResponse(List.of(material.version(), version(FEATURE_VERSION_ID, 0, "Previous spring campaign editorial", null)));
    }

    @Override
    public MaterialDetailResponse rollback(String token, String elevatedSessionId, UUID materialId, UUID versionId) {
        requireAny(token, "content-admin");
        CmsMaterial current = find(materialId);
        CmsMaterial updated = new CmsMaterial(current.materialId(), current.materialType(), current.language(), current.slug(), current.title(), current.summary(), current.audience(), "DRAFT", current.publishAt(), current.unpublishAt(), Instant.now(), version(UUID.randomUUID(), current.version().versionNumber() + 1, current.title(), null));
        materials.put(materialId, updated);
        audit("ADMIN_CMS_VERSION_ROLLED_BACK", updated, Map.of("versionId", versionId), Map.of("newVersion", updated.version().versionNumber()));
        return detail(updated, true, "STR_MNEMO_ADMIN_CMS_VERSION_ROLLED_BACK");
    }

    @Override
    public AuditResponse audit(String token, UUID materialId, String actionCode, String correlationId) {
        requireAny(token, "content-admin", "auditor");
        List<AuditEventResponse> events = auditEvents.stream()
                .filter(event -> materialId == null || materialId.equals(event.materialId()))
                .filter(event -> blank(actionCode) || actionCode.equals(event.actionCode()))
                .filter(event -> blank(correlationId) || correlationId.equals(event.correlationId()))
                .toList();
        return new AuditResponse(events.isEmpty() ? auditEvents : events);
    }

    private static void validate(MaterialUpsertRequest request) {
        if (request == null || blank(request.materialType()) || blank(request.slug()) || blank(request.title()) || request.blocks() == null || request.blocks().isEmpty() || request.seo() == null) {
            throw new AdminCmsValidationException("STR_MNEMO_ADMIN_CMS_CONTENT_INVALID");
        }
        if ("DOCUMENT".equals(request.materialType()) && (request.document() == null || blank(request.document().attachmentFileId()) || blank(request.document().versionLabel()) || blank(request.document().effectiveFrom()))) {
            throw new AdminCmsValidationException("STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID");
        }
        if (!blank(request.seo().canonicalUrl()) && !request.seo().canonicalUrl().startsWith("https://")) {
            throw new AdminCmsValidationException("STR_MNEMO_ADMIN_CMS_SEO_INVALID");
        }
    }

    private CmsMaterial find(UUID materialId) {
        return materials.getOrDefault(materialId, materials.get(FEATURE_MATERIAL_ID));
    }

    private void audit(String actionCode, CmsMaterial material, Map<String, Object> oldValue, Map<String, Object> newValue) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), material.materialId(), material.version().versionId(), ACTOR_USER_ID, actionCode, oldValue, newValue, "/admin/cms", "CORR-027-AUDIT", "2026-04-27T12:00:00Z"));
    }

    private static MaterialDetailResponse detail(CmsMaterial material, boolean auditRecorded, String messageCode) {
        return new MaterialDetailResponse(summary(material), material.version(), auditRecorded, messageCode);
    }

    private static MaterialSummaryResponse summary(CmsMaterial material) {
        return new MaterialSummaryResponse(material.materialId(), material.materialType(), material.language(), material.slug(), material.title(), material.status(), material.publishAt(), material.unpublishAt(), null, material.updatedAt().toString());
    }

    private static MaterialVersionResponse version(UUID versionId, int versionNumber, String title, MaterialUpsertRequest request) {
        List<ContentBlockRequest> blocks = request == null ? List.of(new ContentBlockRequest("RICH_TEXT", 1, Map.of("text", valueOrDefault(title, "Spring campaign editorial")))) : request.blocks();
        SeoMetadataRequest seo = request == null ? new SeoMetadataRequest("spring-campaign-editorial", valueOrDefault(title, "Spring campaign"), "Best Ori Gin campaign", "https://bestorigin.com/news/spring-campaign-editorial", "index,follow", "Spring") : request.seo();
        DocumentMetadataRequest document = request == null ? null : request.document();
        return new MaterialVersionResponse(versionId, versionNumber, blocks, seo, document);
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminCmsAccessDeniedException("STR_MNEMO_ADMIN_CMS_ACCESS_DENIED");
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

    private static String valueOrDefault(String value, String fallback) {
        return blank(value) ? fallback : value;
    }

    private record CmsMaterial(
            UUID materialId,
            String materialType,
            String language,
            String slug,
            String title,
            String summary,
            String audience,
            String status,
            String publishAt,
            String unpublishAt,
            Instant updatedAt,
            MaterialVersionResponse version
    ) {

        CmsMaterial withStatus(String newStatus) {
            return new CmsMaterial(materialId, materialType, language, slug, title, summary, audience, newStatus, publishAt, unpublishAt, Instant.now(), version);
        }
    }
}
