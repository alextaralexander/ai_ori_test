package com.bestorigin.monolith.adminplatform.impl.service;

import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AuditEventPage;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationStatus;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiDashboardResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AlertResponse;
import java.util.List;

public interface AdminPlatformService {
    KpiDashboardResponse kpis(String token, String period, String campaignCode, String region, String channel);
    List<IntegrationStatus> integrations(String token);
    IntegrationSettingsResponse saveIntegration(String token, String integrationCode, String idempotencyKey, IntegrationSettingsRequest request);
    AuditEventPage auditEvents(String token, String actor, String domain, String actionCode, String correlationId, int page, int size);
    ReportExportResponse startExport(String token, String idempotencyKey, ReportExportRequest request);
    List<AlertResponse> alerts(String token);
}