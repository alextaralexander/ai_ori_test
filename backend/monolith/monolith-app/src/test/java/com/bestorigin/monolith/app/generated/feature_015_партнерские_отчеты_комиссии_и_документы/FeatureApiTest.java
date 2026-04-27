// Synchronized from agents/tests/. Do not edit this generated runtime copy manually.
package com.bestorigin.tests.feature015;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class FeatureApiTest {

    private static final String BASE_URL = System.getProperty("bestorigin.baseUrl", "http://localhost:8080");
    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void partnerSeesReportSummary() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = getAuthorized("/api/partner-reporting/reports/summary?dateFrom=2026-05-01&dateTo=2026-05-21&catalogId=CAT-2026-05&bonusProgramId=MLM-BASE", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"partnerId\"");
        assertContains(response.body(), "\"grossSales\"");
        assertContains(response.body(), "\"accruedCommission\"");
        assertContains(response.body(), "\"reconciliationStatus\":\"MATCHED\"");
    }

    @Test
    void partnerFiltersOrderCommissionLines() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = getAuthorized("/api/partner-reporting/reports/orders?catalogId=CAT-2026-05&orderNumber=ORD-015-STRUCTURE-001&page=0&size=20", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "ORD-015-STRUCTURE-001");
        assertContains(response.body(), "\"commissionBase\"");
        assertContains(response.body(), "\"calculationStatus\"");
    }

    @Test
    void partnerOpensCommissionDetailsWithAdjustments() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = getAuthorized("/api/partner-reporting/reports/orders/ORD-015-STRUCTURE-001/commission", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"orderNumber\":\"ORD-015-STRUCTURE-001\"");
        assertContains(response.body(), "\"adjustments\"");
        assertContains(response.body(), "\"payoutReference\"");
        assertContains(response.body(), "\"correlationId\"");
    }

    @Test
    void partnerDownloadsPublishedDocument() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = postAuthorized("/api/partner-reporting/documents/00000000-0015-0000-0000-000000000001/download", "{}", userContextId, userContextId + "-download-001");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"documentStatus\":\"PUBLISHED\"");
        assertContains(response.body(), "\"checksumSha256\"");
        assertContains(response.body(), "\"downloadUrl\"");
    }

    @Test
    void partnerExportReturnsReadyMetadata() throws Exception {
        String userContextId = loginAs("partner");
        String body = "{\"format\":\"XLSX\",\"dateFrom\":\"2026-05-01\",\"dateTo\":\"2026-05-21\",\"catalogId\":\"CAT-2026-05\",\"bonusProgramId\":\"MLM-BASE\"}";

        HttpResponse<String> response = postAuthorized("/api/partner-reporting/exports", body, userContextId, userContextId + "-export-001");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"exportStatus\":\"READY\"");
        assertContains(response.body(), "\"format\":\"XLSX\"");
        assertContains(response.body(), "STR_MNEMO_PARTNER_REPORT_EXPORT_READY");
    }

    @Test
    void accountantPublishesReadyDocument() throws Exception {
        String userContextId = loginAs("accountant");
        String body = "{\"reasonCode\":\"MONTHLY_PAYOUT_APPROVED\",\"comment\":\"approved\"}";

        HttpResponse<String> response = postAuthorized("/api/partner-reporting/finance/documents/00000000-0015-0000-0000-000000000002/publish", body, userContextId, userContextId + "-publish-001");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"documentStatus\":\"PUBLISHED\"");
        assertContains(response.body(), "\"versionNumber\"");
        assertContains(response.body(), "\"checksumSha256\"");
    }

    @Test
    void financeControllerSeesMismatchAndRevokesDocument() throws Exception {
        String userContextId = loginAs("finance-controller");

        HttpResponse<String> reconciliation = getAuthorized("/api/partner-reporting/finance/reconciliations?partnerId=partner-015&dateFrom=2026-05-01&dateTo=2026-05-21&reason=MONTHLY_CONTROL", userContextId);
        assertEquals(200, reconciliation.statusCode());
        assertContains(reconciliation.body(), "\"reconciliationStatus\":\"MISMATCH\"");
        assertContains(reconciliation.body(), "STR_MNEMO_PARTNER_REPORT_RECONCILIATION_MISMATCH");

        HttpResponse<String> revoked = postAuthorized("/api/partner-reporting/finance/documents/00000000-0015-0000-0000-000000000003/revoke", "{\"reasonCode\":\"WRONG_AMOUNT\"}", userContextId, userContextId + "-revoke-001");
        assertEquals(200, revoked.statusCode());
        assertContains(revoked.body(), "\"documentStatus\":\"REVOKED\"");
    }

    @Test
    void partnerCannotOpenForeignReport() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = getAuthorized("/api/partner-reporting/finance/reconciliations?partnerId=partner-foreign&dateFrom=2026-05-01&dateTo=2026-05-21&reason=CHECK", userContextId);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_PARTNER_REPORT_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        partnerSeesReportSummary();
        partnerFiltersOrderCommissionLines();
        partnerOpensCommissionDetailsWithAdjustments();
        partnerDownloadsPublishedDocument();
        partnerExportReturnsReadyMetadata();
        accountantPublishesReadyDocument();
        financeControllerSeesMismatchAndRevokesDocument();
        partnerCannotOpenForeignReport();
    }

    private HttpResponse<String> getAuthorized(String path, String userContextId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> postAuthorized(String path, String body, String userContextId, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Idempotency-Key", idempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}
