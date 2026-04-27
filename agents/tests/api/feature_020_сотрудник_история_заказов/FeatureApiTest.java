package com.bestorigin.tests.feature020;

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
    void employeeOpensOrderHistoryList() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/order-history?query=BOG-ORD-020-001", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-ORD-020-001");
        assertContains(response.body(), "auditRecorded");
        assertContains(response.body(), "linkedRoutes");
    }

    @Test
    void employeeFiltersProblemOrders() throws Exception {
        String userContextId = loginAs("order-support");

        HttpResponse<String> response = getAuthorized("/api/employee/order-history?partnerId=PART-020-001&dateFrom=2026-04-01&dateTo=2026-04-27&problemOnly=true", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "OPEN_CLAIM");
        assertContains(response.body(), "WMS_HOLD");
        assertContains(response.body(), "PAYMENT_EVENT");
    }

    @Test
    void employeeOpensOrderHistoryDetails() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/order-history/BOG-ORD-020-001", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "SKU-020-CREAM-001");
        assertContains(response.body(), "paymentEvents");
        assertContains(response.body(), "wmsEvents");
        assertContains(response.body(), "auditEvents");
    }

    @Test
    void supervisorSeesExtendedAuditTrail() throws Exception {
        String userContextId = loginAs("supervisor");

        HttpResponse<String> response = getAuthorized("/api/employee/order-history/BOG-ORD-020-001", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "ORDER_DETAILS_VIEWED");
        assertContains(response.body(), "actorRole");
        assertContains(response.body(), "supervisorRequired");
    }

    @Test
    void invalidFilterReturnsMnemonic() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/order-history?dateFrom=2026-04-27&dateTo=2026-04-01", userContextId);
        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID");
    }

    @Test
    void partnerCannotOpenEmployeeOrderHistory() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = getAuthorized("/api/employee/order-history", userContextId);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        employeeOpensOrderHistoryList();
        employeeFiltersProblemOrders();
        employeeOpensOrderHistoryDetails();
        supervisorSeesExtendedAuditTrail();
        invalidFilterReturnsMnemonic();
        partnerCannotOpenEmployeeOrderHistory();
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

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}