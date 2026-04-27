package com.bestorigin.monolith.adminplatform.impl.service;

import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AlertResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AuditEventPage;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AuditEventResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationStatus;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiDashboardResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiTile;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiTrendPoint;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportResponse;
import com.bestorigin.monolith.adminplatform.impl.exception.AdminPlatformAccessDeniedException;
import com.bestorigin.monolith.adminplatform.impl.exception.AdminPlatformValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminPlatformService implements AdminPlatformService {

    private final ConcurrentMap<String, IntegrationStatus> integrations = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    public DefaultAdminPlatformService() {
        integrations.put("WMS_1C", new IntegrationStatus("WMS_1C", "DEGRADED", 20, "EXPONENTIAL_3", "SUN 02:00-03:00", "2026-04-28T01:45:00Z", "WMS_TIMEOUT", "CORR-036-WMS"));
        integrations.put("ASSEMBLY", new IntegrationStatus("ASSEMBLY", "OK", 10, "LINEAR_2", "SUN 03:00-03:30", "2026-04-28T01:50:00Z", null, "CORR-036-ASM"));
        integrations.put("DELIVERY", new IntegrationStatus("DELIVERY", "OK", 30, "EXPONENTIAL_3", "SUN 04:00-04:30", "2026-04-28T01:48:00Z", null, "CORR-036-DEL"));
        integrations.put("PAYMENT", new IntegrationStatus("PAYMENT", "OK", 5, "EXPONENTIAL_5", "SUN 01:00-01:30", "2026-04-28T01:49:00Z", null, "CORR-036-PAY"));
        integrations.put("BONUS", new IntegrationStatus("BONUS", "OK", 15, "LINEAR_3", "SUN 01:30-02:00", "2026-04-28T01:47:00Z", null, "CORR-036-BONUS"));
        integrations.put("ANALYTICS", new IntegrationStatus("ANALYTICS", "STALE", 60, "EXPONENTIAL_3", "SUN 05:00-05:30", "2026-04-28T00:20:00Z", "EVENT_LAG", "CORR-036-AN"));
        audit("business-admin", "KPI", "KPI_DASHBOARD_VIEWED", "BOOT", "KPI-036");
        audit("integration-admin", "INTEGRATION", "INTEGRATION_SETTINGS_SAVED", "SLA_TUNING", "WMS_1C");
    }

    @Override
    public KpiDashboardResponse kpis(String token, String period, String campaignCode, String region, String channel) {
        requireAny(token, "business-admin", "bi-analyst", "super-admin");
        audit(role(token), "KPI", "KPI_DASHBOARD_VIEWED", "VIEW", valueOrDefault(period, "campaign-2026-05"));
        return new KpiDashboardResponse(
                valueOrDefault(period, "campaign-2026-05"),
                "RUB",
                "2026-04-28T02:00:00Z",
                List.of(
                        tile("GMV", "12500000.00", "RUB", "12.4", "OK"),
                        tile("CONVERSION", "8.6", "PERCENT", "1.2", "OK"),
                        tile("ORDERS", "1842", "COUNT", "6.5", "OK"),
                        tile("FULFILLMENT_SLA", "96.2", "PERCENT", "-0.8", "WARNING"),
                        tile("CLAIMS_RATE", "1.7", "PERCENT", "0.3", "OK")),
                List.of(new KpiTrendPoint("GMV", "2026-W17", new BigDecimal("11200000")), new KpiTrendPoint("GMV", "2026-W18", new BigDecimal("12500000"))),
                alerts(token),
                "STR_MNEMO_ADMIN_PLATFORM_KPI_READY");
    }

    @Override
    public List<IntegrationStatus> integrations(String token) {
        requireAny(token, "integration-admin", "business-admin", "super-admin");
        return List.copyOf(integrations.values());
    }

    @Override
    public IntegrationSettingsResponse saveIntegration(String token, String integrationCode, String idempotencyKey, IntegrationSettingsRequest request) {
        requireAny(token, "integration-admin", "super-admin");
        if (request == null || request.slaMinutes() == null || request.slaMinutes() <= 0 || blank(request.retryPolicy()) || blank(request.reasonCode())) {
            throw new AdminPlatformValidationException("STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_INVALID", List.of("slaMinutes", "retryPolicy", "reasonCode"));
        }
        IntegrationStatus saved = new IntegrationStatus(integrationCode, "OK", request.slaMinutes(), request.retryPolicy(), valueOrDefault(request.maintenanceWindow(), "SUN 02:00-03:00"), "2026-04-28T02:00:00Z", null, "CORR-036-INTEGRATION-" + key(idempotencyKey, "default"));
        integrations.put(integrationCode, saved);
        UUID auditEventId = audit(role(token), "INTEGRATION", "INTEGRATION_SETTINGS_SAVED", request.reasonCode(), integrationCode);
        return new IntegrationSettingsResponse(saved.integrationCode(), saved.status(), saved.slaMinutes(), saved.retryPolicy(), saved.maintenanceWindow(), auditEventId, saved.correlationId(), "STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED");
    }

    @Override
    public AuditEventPage auditEvents(String token, String actor, String domain, String actionCode, String correlationId, int page, int size) {
        requireAny(token, "audit-admin", "super-admin");
        List<AuditEventResponse> items = auditEvents.stream()
                .filter(event -> blank(actor) || event.actorRole().contains(actor))
                .filter(event -> blank(domain) || event.domain().equals(domain))
                .filter(event -> blank(actionCode) || event.actionCode().equals(actionCode))
                .filter(event -> blank(correlationId) || event.correlationId().contains(correlationId))
                .toList();
        return new AuditEventPage(items.isEmpty() ? List.copyOf(auditEvents) : items, page, size, items.isEmpty() ? auditEvents.size() : items.size());
    }

    @Override
    public ReportExportResponse startExport(String token, String idempotencyKey, ReportExportRequest request) {
        requireAny(token, "business-admin", "bi-analyst", "super-admin");
        if (request == null || blank(request.reportType()) || blank(request.format()) || !("CSV".equals(request.format()) || "XLSX".equals(request.format()) || "PDF".equals(request.format()))) {
            throw new AdminPlatformValidationException("STR_MNEMO_ADMIN_PLATFORM_EXPORT_INVALID", List.of("reportType", "format"));
        }
        audit(role(token), "REPORT", "REPORT_EXPORT_STARTED", request.reportType(), request.period());
        return new ReportExportResponse(UUID.randomUUID(), request.reportType(), request.format(), "ACCEPTED", role(token), "CORR-036-EXPORT-" + key(idempotencyKey, "default"), "STR_MNEMO_ADMIN_PLATFORM_EXPORT_STARTED");
    }

    @Override
    public List<AlertResponse> alerts(String token) {
        requireAny(token, "business-admin", "bi-analyst", "integration-admin", "super-admin");
        return List.of(
                new AlertResponse("STALE_KPI_SOURCE", "WARNING", "ANALYTICS", "GMV", "STR_MNEMO_ADMIN_PLATFORM_ALERT_STALE_SOURCE", "CORR-036-AN"),
                new AlertResponse("INTEGRATION_SLA_BREACH", "CRITICAL", "WMS_1C", "FULFILLMENT_SLA", "STR_MNEMO_ADMIN_PLATFORM_ALERT_SLA_BREACH", "CORR-036-WMS"));
    }

    private UUID audit(String actorRole, String domain, String actionCode, String reasonCode, String subjectRef) {
        UUID id = UUID.randomUUID();
        auditEvents.add(new AuditEventResponse(id, actorRole, domain, actionCode, reasonCode, mask(subjectRef), "CORR-036-AUDIT-" + actionCode, "2026-04-28T02:00:00Z"));
        return id;
    }

    private static KpiTile tile(String code, String value, String unit, String change, String status) {
        return new KpiTile(code, new BigDecimal(value), unit, new BigDecimal(change), status);
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminPlatformAccessDeniedException("STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED");
    }

    private static String role(String token) {
        if (token == null) {
            return "";
        }
        String normalized = token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static String mask(String value) {
        if (blank(value)) {
            return "***";
        }
        return value.length() <= 3 ? "***" : value.substring(0, 3) + "***";
    }

    private static String key(String value, String fallback) {
        return blank(value) ? fallback : value;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String valueOrDefault(String value, String fallback) {
        return blank(value) ? fallback : value;
    }
}