package com.bestorigin.monolith.admincms.impl.service;

import com.bestorigin.monolith.admincms.api.AdminCmsDtos.AuditResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialDetailResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialListResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialUpsertRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.PreviewResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.PublishRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.ReviewRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.VersionListResponse;
import java.util.UUID;

public interface AdminCmsService {

    MaterialListResponse searchMaterials(String token, String materialType, String status, String language, String search);

    MaterialDetailResponse createMaterial(String token, String elevatedSessionId, MaterialUpsertRequest request);

    MaterialDetailResponse getMaterial(String token, UUID materialId);

    MaterialDetailResponse updateMaterial(String token, UUID materialId, MaterialUpsertRequest request);

    MaterialDetailResponse submitReview(String token, UUID materialId);

    MaterialDetailResponse review(String token, UUID materialId, ReviewRequest request);

    MaterialDetailResponse publish(String token, String elevatedSessionId, UUID materialId, PublishRequest request);

    void archive(String token, UUID materialId);

    PreviewResponse preview(String token, UUID materialId);

    VersionListResponse versions(String token, UUID materialId);

    MaterialDetailResponse rollback(String token, String elevatedSessionId, UUID materialId, UUID versionId);

    AuditResponse audit(String token, UUID materialId, String actionCode, String correlationId);
}
