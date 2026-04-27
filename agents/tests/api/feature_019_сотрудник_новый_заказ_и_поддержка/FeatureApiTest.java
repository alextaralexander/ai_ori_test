package com.bestorigin.tests.feature019;

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
    void employeeFindsCustomerWorkspace() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/workspace?query=CUST-019-001", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "CUST-019-001");
        assertContains(response.body(), "BOG-ORD-019-001");
        assertContains(response.body(), "auditContext");
    }

    @Test
    void employeeCreatesOperatorOrder() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = postAuthorized("/api/employee/operator-orders", userContextId, "{\"targetCustomerId\":\"CUST-019-001\",\"supportReasonCode\":\"PHONE_ORDER\",\"cartType\":\"MAIN\",\"items\":[{\"sku\":\"SKU-019-CREAM-001\",\"quantity\":2}]}");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-ORD-019-NEW");
        assertContains(response.body(), "auditRecorded");
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_OPERATOR_ORDER_CREATED");
    }

    @Test
    void employeeConfirmsOperatorOrderIdempotently() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = postAuthorized("/api/employee/operator-orders/01900000-0000-0000-0000-000000000001/confirm", userContextId, "{\"checkoutVersion\":1}");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "READY_TO_PAY");
        assertContains(response.body(), "CARD_ONLINE");
    }

    @Test
    void employeeOpensOrderSupport() throws Exception {
        String userContextId = loginAs("order-support");

        HttpResponse<String> response = getAuthorized("/api/employee/order-support/BOG-ORD-019-001", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-ORD-019-001");
        assertContains(response.body(), "timeline");
        assertContains(response.body(), "linkedRoutes");
    }

    @Test
    void employeeAddsInternalNote() throws Exception {
        String userContextId = loginAs("order-support");

        HttpResponse<String> response = postAuthorized("/api/employee/order-support/BOG-ORD-019-001/notes", userContextId, "{\"reasonCode\":\"DELIVERY_DELAY\",\"note\":\"Клиент сообщил о задержке доставки\"}");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_SUPPORT_NOTE_ADDED");
        assertContains(response.body(), "EMPLOYEE_ONLY");
    }

    @Test
    void employeeRecordsServiceAdjustment() throws Exception {
        String userContextId = loginAs("order-support");

        HttpResponse<String> response = postAuthorized("/api/employee/order-support/BOG-ORD-019-001/adjustments", userContextId, "{\"reasonCode\":\"DELIVERY_DELAY\",\"amount\":350}");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_SUPPORT_ADJUSTMENT_RECORDED");
        assertContains(response.body(), "supervisorRequired");
    }

    @Test
    void supervisorSeesEscalations() throws Exception {
        String userContextId = loginAs("supervisor");

        HttpResponse<String> response = getAuthorized("/api/employee/supervisor/escalations", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-ORD-019-001");
        assertContains(response.body(), "SLA_AT_RISK");
    }

    @Test
    void partnerCannotOpenEmployeeApi() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = getAuthorized("/api/employee/workspace?query=CUST-019-001", userContextId);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        employeeFindsCustomerWorkspace();
        employeeCreatesOperatorOrder();
        employeeConfirmsOperatorOrderIdempotently();
        employeeOpensOrderSupport();
        employeeAddsInternalNote();
        employeeRecordsServiceAdjustment();
        supervisorSeesEscalations();
        partnerCannotOpenEmployeeApi();
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

    private HttpResponse<String> postAuthorized(String path, String userContextId, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", "feature-019-idempotency")
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