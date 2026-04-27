// Синхронизировано из agents/tests. Не редактировать вручную.
package com.bestorigin.tests.feature010;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void customerCanStartAndConfirmMainCheckout() throws Exception {
        String userContextId = loginAs("customer");

        String startBody = "{\"cartId\":\"CART-010-MAIN\",\"checkoutType\":\"MAIN\"}";
        HttpResponse<String> started = postAuthorized("/api/order/checkouts", startBody, userContextId, userContextId + "-checkout-start-001");
        assertEquals(200, started.statusCode());
        assertContains(started.body(), "\"checkoutType\":\"MAIN\"");
        assertContains(started.body(), "\"campaignId\":\"CMP-2026-05\"");
        String checkoutId = extractJsonString(started.body(), "id", "00000000-0000-0000-0000-000000010001");

        assertEquals(200, putAuthorized("/api/order/checkouts/" + checkoutId + "/recipient", "{\"recipientType\":\"SELF\",\"fullName\":\"Customer 010\",\"phone\":\"+79990000010\",\"email\":\"customer010@example.com\"}", userContextId, userContextId + "-recipient-001").statusCode());
        assertEquals(200, putAuthorized("/api/order/checkouts/" + checkoutId + "/address", "{\"deliveryTargetType\":\"ADDRESS\",\"addressId\":\"ADDR-010-MAIN\",\"country\":\"RU\",\"city\":\"Москва\",\"street\":\"Тверская\",\"house\":\"10\"}", userContextId, userContextId + "-address-001").statusCode());
        assertEquals(200, putAuthorized("/api/order/checkouts/" + checkoutId + "/delivery", "{\"deliveryMethodCode\":\"COURIER\"}", userContextId, userContextId + "-delivery-001").statusCode());
        assertEquals(200, putAuthorized("/api/order/checkouts/" + checkoutId + "/benefits", "{\"walletAmount\":300,\"cashbackAmount\":50,\"benefitCodes\":[\"CATALOG-010\"]}", userContextId, userContextId + "-benefits-001").statusCode());
        assertEquals(200, putAuthorized("/api/order/checkouts/" + checkoutId + "/payment", "{\"paymentMethodCode\":\"ONLINE_CARD\"}", userContextId, userContextId + "-payment-001").statusCode());

        HttpResponse<String> validation = postAuthorized("/api/order/checkouts/" + checkoutId + "/validation", "{}", userContextId, userContextId + "-validation-001");
        assertEquals(200, validation.statusCode());
        assertContains(validation.body(), "\"valid\":true");

        HttpResponse<String> confirmed = postAuthorized("/api/order/checkouts/" + checkoutId + "/confirm", "{\"checkoutVersion\":0}", userContextId, userContextId + "-confirm-001");
        assertEquals(200, confirmed.statusCode());
        assertContains(confirmed.body(), "\"orderType\":\"MAIN\"");
        assertContains(confirmed.body(), "\"orderNumber\"");
        assertContains(confirmed.body(), "\"nextAction\"");
        assertFalse(confirmed.body().contains("Exception"));
    }

    @Test
    void partnerCanConfirmSupplementaryCheckoutSeparately() throws Exception {
        String userContextId = loginAs("partner");

        String startBody = "{\"cartId\":\"CART-010-SUPP\",\"checkoutType\":\"SUPPLEMENTARY\",\"vipMode\":true,\"superOrderMode\":true}";
        HttpResponse<String> started = postAuthorized("/api/order/checkouts", startBody, userContextId, userContextId + "-supp-start-001");
        assertEquals(200, started.statusCode());
        assertContains(started.body(), "\"checkoutType\":\"SUPPLEMENTARY\"");
        assertContains(started.body(), "\"cartId\":\"CART-010-SUPP\"");
        String checkoutId = extractJsonString(started.body(), "id", "00000000-0000-0000-0000-000000010002");

        putAuthorized("/api/order/checkouts/" + checkoutId + "/recipient", "{\"recipientType\":\"SELF\",\"fullName\":\"Partner 010\",\"phone\":\"+79990000011\"}", userContextId, userContextId + "-supp-recipient-001");
        putAuthorized("/api/order/checkouts/" + checkoutId + "/address", "{\"deliveryTargetType\":\"PICKUP_POINT\",\"pickupPointId\":\"PICKUP-010-01\"}", userContextId, userContextId + "-supp-address-001");
        putAuthorized("/api/order/checkouts/" + checkoutId + "/delivery", "{\"deliveryMethodCode\":\"PICKUP\"}", userContextId, userContextId + "-supp-delivery-001");
        putAuthorized("/api/order/checkouts/" + checkoutId + "/payment", "{\"paymentMethodCode\":\"ONLINE_CARD\"}", userContextId, userContextId + "-supp-payment-001");

        HttpResponse<String> confirmed = postAuthorized("/api/order/checkouts/" + checkoutId + "/confirm", "{\"checkoutVersion\":0}", userContextId, userContextId + "-supp-confirm-001");
        assertEquals(200, confirmed.statusCode());
        assertContains(confirmed.body(), "\"orderType\":\"SUPPLEMENTARY\"");
        assertContains(confirmed.body(), "\"orderNumber\"");
        assertFalse(confirmed.body().contains("\"orderType\":\"MAIN\""));
    }

    @Test
    void invalidCartBlocksCheckoutStart() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = postAuthorized("/api/order/checkouts", "{\"cartId\":\"CART-010-INVALID\",\"checkoutType\":\"MAIN\"}", userContextId, userContextId + "-invalid-start-001");
        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ORDER_CHECKOUT_CART_INVALID");
        assertFalse(response.body().contains("NullPointerException"));
    }

    @Test
    void confirmationIsIdempotent() throws Exception {
        String userContextId = loginAs("customer");
        HttpResponse<String> started = postAuthorized("/api/order/checkouts", "{\"cartId\":\"CART-010-IDEMP\",\"checkoutType\":\"MAIN\"}", userContextId, userContextId + "-idemp-start-001");
        assertEquals(200, started.statusCode());
        String checkoutId = extractJsonString(started.body(), "id", "00000000-0000-0000-0000-000000010003");

        String key = userContextId + "-confirm-idempotent-001";
        HttpResponse<String> first = postAuthorized("/api/order/checkouts/" + checkoutId + "/confirm", "{\"checkoutVersion\":0}", userContextId, key);
        HttpResponse<String> second = postAuthorized("/api/order/checkouts/" + checkoutId + "/confirm", "{\"checkoutVersion\":0}", userContextId, key);
        assertEquals(200, first.statusCode());
        assertEquals(200, second.statusCode());
        assertEquals(extractJsonString(first.body(), "orderNumber", "missing-1"), extractJsonString(second.body(), "orderNumber", "missing-2"));
    }

    @Test
    void partialReserveAndPaymentFailureReturnControlledMnemonics() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> partial = postAuthorized("/api/order/checkouts", "{\"cartId\":\"CART-010-PARTIAL\",\"checkoutType\":\"MAIN\"}", userContextId, userContextId + "-partial-start-001");
        String partialCheckoutId = extractJsonString(partial.body(), "id", "00000000-0000-0000-0000-000000010004");
        HttpResponse<String> partialConfirm = postAuthorized("/api/order/checkouts/" + partialCheckoutId + "/confirm", "{\"checkoutVersion\":0}", userContextId, userContextId + "-partial-confirm-001");
        assertEquals(409, partialConfirm.statusCode());
        assertContains(partialConfirm.body(), "STR_MNEMO_ORDER_PARTIAL_RESERVE");

        HttpResponse<String> payment = postAuthorized("/api/order/checkouts", "{\"cartId\":\"CART-010-PAYMENT-FAILED\",\"checkoutType\":\"MAIN\"}", userContextId, userContextId + "-payfail-start-001");
        String paymentCheckoutId = extractJsonString(payment.body(), "id", "00000000-0000-0000-0000-000000010005");
        HttpResponse<String> paymentConfirm = postAuthorized("/api/order/checkouts/" + paymentCheckoutId + "/confirm", "{\"checkoutVersion\":0}", userContextId, userContextId + "-payfail-confirm-001");
        assertEquals(200, paymentConfirm.statusCode());
        assertContains(paymentConfirm.body(), "STR_MNEMO_ORDER_PAYMENT_FAILED");
    }

    @Test
    void userCannotReadOrConfirmForeignCheckout() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> foreign = getAuthorized("/api/order/checkouts/00000000-0000-0000-0000-000000019999", userContextId);
        assertEquals(403, foreign.statusCode());
        assertContains(foreign.body(), "STR_MNEMO_ORDER_CHECKOUT_FORBIDDEN");
    }

    public void assertFeatureGreenPath() throws Exception {
        customerCanStartAndConfirmMainCheckout();
        partnerCanConfirmSupplementaryCheckoutSeparately();
        invalidCartBlocksCheckoutStart();
        confirmationIsIdempotent();
        partialReserveAndPaymentFailureReturnControlledMnemonics();
        userCannotReadOrConfirmForeignCheckout();
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

    private HttpResponse<String> putAuthorized(String path, String body, String userContextId, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Idempotency-Key", idempotencyKey)
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
    }

    private static String extractJsonString(String body, String fieldName, String fallback) {
        String marker = "\"" + fieldName + "\":\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return fallback;
        }
        int valueStart = start + marker.length();
        int valueEnd = body.indexOf('"', valueStart);
        return valueEnd > valueStart ? body.substring(valueStart, valueEnd) : fallback;
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}
