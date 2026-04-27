// Generated from agents/tests/. Do not edit manually.
package com.bestorigin.tests.feature022;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class FeatureApiTest {

    private static final String BASE_URL = System.getProperty("bestorigin.baseUrl", "http://localhost:8080");
    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void employeeOpensPartnerCardWithKpiAndAudit() throws Exception {
        String userContextId = loginAs("backoffice");

        HttpResponse<String> response = getAuthorized("/api/employee/partner-card?query=" + encode("P-022-7788") + "&supportReasonCode=EMPLOYEE_PARTNER_CARD_VIEW", userContextId);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "PART-022-001");
        assertContains(response.body(), "P-022-7788");
        assertContains(response.body(), "personalVolume");
        assertContains(response.body(), "groupVolume");
        assertContains(response.body(), "riskSignals");
        assertContains(response.body(), "auditRecorded");
        assertContains(response.body(), "linkedRoutes");
    }

    @Test
    void employeeOpensPartnerCardByPartnerId() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/partner-card/PART-022-001?supportReasonCode=EMPLOYEE_PARTNER_CARD_VIEW", userContextId);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "Partner 022");
        assertContains(response.body(), "maskedPhone");
        assertContains(response.body(), "recentOrders");
    }

    @Test
    void regionalManagerLoadsPartnerOrderReport() throws Exception {
        String userContextId = loginAs("regional-manager");

        HttpResponse<String> response = getAuthorized("/api/employee/report/order-history?partnerId=PART-022-001&dateFrom=2026-04-01&dateTo=2026-04-27&regionCode=RU-MOW", userContextId);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-ORD-022-001");
        assertContains(response.body(), "aggregates");
        assertContains(response.body(), "totalOrders");
        assertContains(response.body(), "openClaimCount");
        assertContains(response.body(), "auditRecorded");
    }

    @Test
    void employeeFiltersPartnerReportByProblemOrders() throws Exception {
        String userContextId = loginAs("backoffice");

        HttpResponse<String> response = getAuthorized("/api/employee/report/order-history?personNumber=P-022-7788&problemOnly=true&campaignCode=2026-C06", userContextId);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "problemFlags");
        assertContains(response.body(), "OPEN_CLAIM");
        assertContains(response.body(), "appliedFilters");
    }

    @Test
    void invalidPartnerCardQueryReturnsMnemonic() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/partner-card?query=" + encode("P2"), userContextId);

        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_PARTNER_QUERY_INVALID");
    }

    @Test
    void invalidPartnerReportPeriodReturnsMnemonic() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/report/order-history?partnerId=PART-022-001&dateFrom=2026-04-27&dateTo=2026-04-01", userContextId);

        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_PARTNER_REPORT_FILTER_INVALID");
    }

    @Test
    void guestCannotOpenPartnerCard() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> response = getAuthorized("/api/employee/partner-card?query=" + encode("P-022-7788"), userContextId);

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        employeeOpensPartnerCardWithKpiAndAudit();
        employeeOpensPartnerCardByPartnerId();
        regionalManagerLoadsPartnerOrderReport();
        employeeFiltersPartnerReportByProblemOrders();
        invalidPartnerCardQueryReturnsMnemonic();
        invalidPartnerReportPeriodReturnsMnemonic();
        guestCannotOpenPartnerCard();
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

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}