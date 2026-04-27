package com.bestorigin.monolith.adminplatform.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class AdminPlatformDtos {

    private AdminPlatformDtos() {
    }

    public record AdminPlatformErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record KpiDashboardResponse(String period, String currency, String updatedAt, List<KpiTile> tiles, List<KpiTrendPoint> trends, List<AlertResponse> alerts, String messageCode) {
    }

    public record KpiTile(String metricCode, BigDecimal value, String unit, BigDecimal changePercent, String status) {
    }

    public record KpiTrendPoint(String metricCode, String bucket, BigDecimal value) {
    }

    public record IntegrationStatus(String integrationCode, String status, int slaMinutes, String retryPolicy, String maintenanceWindow, String lastExchangeAt, String lastErrorCode, String correlationId) {
    }

    public record IntegrationSettingsRequest(Integer slaMinutes, String retryPolicy, String maintenanceWindow, String reasonCode) {
    }

    public record IntegrationSettingsResponse(String integrationCode, String status, int slaMinutes, String retryPolicy, String maintenanceWindow, UUID auditEventId, String correlationId, String messageCode) {
    }

    public record AuditEventPage(List<AuditEventResponse> items, int page, int size, long total) {
    }

    public record AuditEventResponse(UUID auditEventId, String actorRole, String domain, String actionCode, String reasonCode, String maskedSubjectRef, String correlationId, String occurredAt) {
    }

    public record ReportExportRequest(String reportType, String format, String period) {
    }

    public record ReportExportResponse(UUID exportId, String reportType, String format, String status, String requestedByRole, String correlationId, String messageCode) {
    }

    public record AlertResponse(String alertCode, String severity, String domain, String metricCode, String messageCode, String correlationId) {
    }
}