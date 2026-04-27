package com.bestorigin.tests.feature031;

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
    void pricingManagerCreatesPriceListBasePricePromoPriceAndSegmentRule() throws Exception {
        String token = extractJsonString(loginAs("pricing-manager").body(), "token", "test-token-pricing-manager");
        String priceListBody = "{\"priceListCode\":\"PL-RU-2026-05\",\"name\":\"Цены майской кампании\",\"campaignId\":\"CAM-2026-05\",\"countryCode\":\"RU\",\"currencyCode\":\"RUB\",\"channelCode\":\"WEB\",\"activeFrom\":\"2026-05-01T00:00:00Z\",\"activeTo\":\"2026-05-21T23:59:59Z\"}";

        HttpResponse<String> priceList = sendAuthorized("/api/admin/pricing/price-lists", token, "POST", priceListBody, "PRICING-031-PRICE-LIST");
        assertEquals(201, priceList.statusCode());
        assertContains(priceList.body(), "PL-RU-2026-05");
        assertContains(priceList.body(), "STR_MNEMO_ADMIN_PRICING_PRICE_LIST_SAVED");
        assertNoHardcodedRussianUiText(priceList.body());

        String priceListId = extractJsonString(priceList.body(), "priceListId", "00000000-0000-0000-0000-000000000031");
        String basePriceBody = "{\"productId\":\"BOG-PRODUCT-SERUM-001\",\"sku\":\"BOG-SERUM-001\",\"basePrice\":1890.00,\"taxMode\":\"VAT_INCLUDED\",\"activeFrom\":\"2026-05-01T00:00:00Z\",\"activeTo\":\"2026-05-21T23:59:59Z\"}";
        HttpResponse<String> basePrice = sendAuthorized("/api/admin/pricing/price-lists/" + priceListId + "/prices", token, "POST", basePriceBody, "PRICING-031-BASE-PRICE");
        assertEquals(201, basePrice.statusCode());
        assertContains(basePrice.body(), "BOG-SERUM-001");
        assertContains(basePrice.body(), "STR_MNEMO_ADMIN_PRICING_PRICE_SAVED");

        String promoPriceBody = "{\"sku\":\"BOG-SERUM-001\",\"promoPrice\":1590.00,\"discountType\":\"FIXED_PRICE\",\"discountValue\":300.00,\"segmentCode\":\"PARTNER\",\"reasonCode\":\"MAY_PARTNER_PROMO\",\"activeFrom\":\"2026-05-01T00:00:00Z\",\"activeTo\":\"2026-05-21T23:59:59Z\"}";
        HttpResponse<String> promoPrice = sendAuthorized("/api/admin/pricing/price-lists/" + priceListId + "/promo-prices", token, "POST", promoPriceBody, "PRICING-031-PROMO-PRICE");
        assertEquals(201, promoPrice.statusCode());
        assertContains(promoPrice.body(), "PARTNER");
        assertContains(promoPrice.body(), "STR_MNEMO_ADMIN_PRICING_PROMO_PRICE_SAVED");

        String segmentRuleBody = "{\"campaignId\":\"CAM-2026-05\",\"segmentCode\":\"PARTNER\",\"roleCode\":\"PARTNER\",\"partnerLevel\":\"VIP\",\"regionCode\":\"RU\",\"priority\":100,\"activeFrom\":\"2026-05-01T00:00:00Z\",\"activeTo\":\"2026-05-21T23:59:59Z\"}";
        HttpResponse<String> segmentRule = sendAuthorized("/api/admin/pricing/segment-rules", token, "POST", segmentRuleBody, "PRICING-031-SEGMENT-RULE");
        assertEquals(201, segmentRule.statusCode());
        assertContains(segmentRule.body(), "STR_MNEMO_ADMIN_PRICING_SEGMENT_RULE_SAVED");
    }

    @Test
    void duplicatePriceListAndOverlappingPriceReturnMnemonicCodes() throws Exception {
        String token = extractJsonString(loginAs("pricing-manager").body(), "token", "test-token-pricing-manager");
        String priceListBody = "{\"priceListCode\":\"PL-RU-2026-05\",\"name\":\"Цены майской кампании\",\"campaignId\":\"CAM-2026-05\",\"countryCode\":\"RU\",\"currencyCode\":\"RUB\",\"channelCode\":\"WEB\",\"activeFrom\":\"2026-05-01T00:00:00Z\",\"activeTo\":\"2026-05-21T23:59:59Z\"}";

        sendAuthorized("/api/admin/pricing/price-lists", token, "POST", priceListBody, "PRICING-031-DUP-1");
        HttpResponse<String> duplicate = sendAuthorized("/api/admin/pricing/price-lists", token, "POST", priceListBody, "PRICING-031-DUP-2");
        assertEquals(409, duplicate.statusCode());
        assertContains(duplicate.body(), "STR_MNEMO_ADMIN_PRICING_PRICE_LIST_CODE_CONFLICT");
        assertNoHardcodedRussianUiText(duplicate.body());

        String priceListId = extractJsonString(duplicate.body(), "priceListId", "00000000-0000-0000-0000-000000000031");
        String overlapBody = "{\"productId\":\"BOG-PRODUCT-SERUM-001\",\"sku\":\"BOG-SERUM-001\",\"basePrice\":1790.00,\"taxMode\":\"VAT_INCLUDED\",\"activeFrom\":\"2026-05-10T00:00:00Z\",\"activeTo\":\"2026-05-20T23:59:59Z\"}";
        HttpResponse<String> overlap = sendAuthorized("/api/admin/pricing/price-lists/" + priceListId + "/prices", token, "POST", overlapBody, "PRICING-031-OVERLAP");
        assertEquals(409, overlap.statusCode());
        assertContains(overlap.body(), "STR_MNEMO_ADMIN_PRICING_PRICE_PERIOD_OVERLAP");
    }

    @Test
    void promotionsManagerCreatesPromotionOfferGiftAndPublishIsIdempotent() throws Exception {
        String promoToken = extractJsonString(loginAs("promotions-manager").body(), "token", "test-token-promotions-manager");
        String promotionBody = "{\"promotionCode\":\"MAY-BEAUTY-BUNDLE\",\"nameKey\":\"adminPricing.promotion.mayBeautyBundle\",\"campaignId\":\"CAM-2026-05\",\"audience\":\"CUSTOMER_AND_PARTNER\",\"channelCode\":\"WEB\",\"activeFrom\":\"2026-05-01T00:00:00Z\",\"activeTo\":\"2026-05-21T23:59:59Z\"}";

        HttpResponse<String> promotion = sendAuthorized("/api/admin/pricing/promotions", promoToken, "POST", promotionBody, "PRICING-031-PROMOTION");
        assertEquals(201, promotion.statusCode());
        assertContains(promotion.body(), "MAY-BEAUTY-BUNDLE");
        assertContains(promotion.body(), "STR_MNEMO_ADMIN_PRICING_PROMOTION_SAVED");

        String promotionId = extractJsonString(promotion.body(), "promotionId", "00000000-0000-0000-0000-000000000131");
        String offerBody = "{\"offerCode\":\"MAY-SERUM-CREAM-BUNDLE\",\"offerType\":\"BUNDLE\",\"titleKey\":\"adminPricing.offer.maySerumCreamBundle.title\",\"descriptionKey\":\"adminPricing.offer.maySerumCreamBundle.description\",\"priority\":100,\"stackable\":false,\"mutuallyExclusiveGroup\":\"MAY-BUNDLE\",\"items\":[{\"sku\":\"BOG-SERUM-001\",\"quantity\":1,\"itemRole\":\"REQUIRED\",\"sortOrder\":10},{\"sku\":\"BOG-CREAM-002\",\"quantity\":1,\"itemRole\":\"OPTIONAL\",\"sortOrder\":20}]}";
        HttpResponse<String> offer = sendAuthorized("/api/admin/pricing/promotions/" + promotionId + "/offers", promoToken, "POST", offerBody, "PRICING-031-OFFER");
        assertEquals(201, offer.statusCode());
        assertContains(offer.body(), "MAY-SERUM-CREAM-BUNDLE");
        assertContains(offer.body(), "STR_MNEMO_ADMIN_PRICING_OFFER_SAVED");

        String giftBody = "{\"giftSku\":\"BOG-GIFT-MASK-001\",\"triggerType\":\"CART_AMOUNT\",\"thresholdAmount\":5000.00,\"maxGiftQuantity\":1,\"activeFrom\":\"2026-05-01T00:00:00Z\",\"activeTo\":\"2026-05-21T23:59:59Z\"}";
        HttpResponse<String> gift = sendAuthorized("/api/admin/pricing/promotions/" + promotionId + "/gift-rules", promoToken, "POST", giftBody, "PRICING-031-GIFT");
        assertEquals(201, gift.statusCode());
        assertContains(gift.body(), "STR_MNEMO_ADMIN_PRICING_GIFT_RULE_SAVED");

        String adminToken = extractJsonString(loginAs("business-admin").body(), "token", "test-token-business-admin");
        String publishBody = "{\"priceListId\":\"00000000-0000-0000-0000-000000000031\",\"promotionIds\":[\"" + promotionId + "\"],\"comment\":\"Майская публикация цен и промо\"}";
        HttpResponse<String> first = sendAuthorized("/api/admin/pricing/publish", adminToken, "POST", publishBody, "publish-pricing-may-2026");
        HttpResponse<String> second = sendAuthorized("/api/admin/pricing/publish", adminToken, "POST", publishBody, "publish-pricing-may-2026");
        assertEquals(200, first.statusCode());
        assertEquals(200, second.statusCode());
        assertEquals(extractJsonString(first.body(), "correlationId", "missing"), extractJsonString(second.body(), "correlationId", "different"));
        assertContains(first.body(), "STR_MNEMO_ADMIN_PRICING_PUBLISHED");
    }

    @Test
    void pauseAuditAndForbiddenAccessAreExposedSafely() throws Exception {
        String adminToken = extractJsonString(loginAs("business-admin").body(), "token", "test-token-business-admin");
        String offerId = "00000000-0000-0000-0000-000000000231";
        HttpResponse<String> paused = sendAuthorized("/api/admin/pricing/offers/" + offerId + "/pause", adminToken, "POST", "{\"reasonCode\":\"INCORRECT_DISCOUNT\",\"comment\":\"Контрольное отключение\"}", "PRICING-031-PAUSE");
        assertEquals(200, paused.statusCode());
        assertContains(paused.body(), "PAUSED");
        assertContains(paused.body(), "STR_MNEMO_ADMIN_PRICING_OFFER_PAUSED");

        HttpResponse<String> audit = getAuthorized("/api/admin/pricing/audit-events?entityType=SHOPPING_OFFER&correlationId=PRICING-031-PAUSE", adminToken);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "SHOPPING_OFFER_PAUSED");
        assertFalse(audit.body().contains("secret"));
        assertFalse(audit.body().contains("stackTrace"));

        String forbiddenToken = extractJsonString(loginAs("employee-support").body(), "token", "test-token-employee-support");
        HttpResponse<String> forbidden = getAuthorized("/api/admin/pricing/price-lists", forbiddenToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_PRICING_FORBIDDEN");
        assertNoHardcodedRussianUiText(forbidden.body());
    }

    public void assertFeatureGreenPath() throws Exception {
        pricingManagerCreatesPriceListBasePricePromoPriceAndSegmentRule();
        duplicatePriceListAndOverlappingPriceReturnMnemonicCodes();
        promotionsManagerCreatesPromotionOfferGiftAndPublishIsIdempotent();
        pauseAuditAndForbiddenAccessAreExposedSafely();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}";
        return send("/api/auth/test-login", "POST", body, null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", null, token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String idempotencyKey) throws IOException, InterruptedException {
        return send(path, method, body, token, idempotencyKey);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Content-Type", "application/json; charset=utf-8");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest request = builder.method(method, publisher).build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static void assertNoHardcodedRussianUiText(String body) {
        assertFalse(body.contains("Прайс-лист сохранен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Акция опубликована"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Доступ запрещен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
    }

    private static String extractJsonString(String body, String field, String fallback) {
        String marker = "\"" + field + "\":\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return fallback;
        }
        int valueStart = start + marker.length();
        int valueEnd = body.indexOf('"', valueStart);
        return valueEnd > valueStart ? body.substring(valueStart, valueEnd) : fallback;
    }
}
