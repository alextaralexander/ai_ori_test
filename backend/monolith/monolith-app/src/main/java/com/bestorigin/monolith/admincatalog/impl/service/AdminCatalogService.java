package com.bestorigin.monolith.admincatalog.impl.service;

import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.ArchiveResponse;
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
import java.util.UUID;

public interface AdminCatalogService {

    WorkspaceResponse workspace(String token);

    CampaignListResponse searchCampaigns(String token, String status, String locale, String search);

    CampaignResponse createCampaign(String token, String idempotencyKey, CampaignCreateRequest request);

    IssueResponse createIssue(String token, UUID campaignId, IssueCreateRequest request);

    MaterialResponse addMaterial(String token, UUID issueId, MaterialCreateRequest request);

    MaterialResponse approveMaterial(String token, UUID materialId);

    PageResponse addPage(String token, UUID issueId, PageCreateRequest request);

    HotspotResponse addHotspot(String token, UUID issueId, HotspotCreateRequest request);

    LinkValidationResponse validateLinks(String token, UUID issueId);

    RolloverResponse rollover(String token, UUID issueId, String idempotencyKey);

    ArchiveResponse archive(String token);

    AuditResponse audit(String token, String actionCode, String correlationId);
}
