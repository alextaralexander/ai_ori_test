package com.bestorigin.monolith.cart.impl.service;

import com.bestorigin.monolith.cart.api.CartDtos.AddCartItemRequest;
import com.bestorigin.monolith.cart.api.CartDtos.AppliedOfferResponse;
import com.bestorigin.monolith.cart.api.CartDtos.AvailabilityStatus;
import com.bestorigin.monolith.cart.api.CartDtos.CartAvailability;
import com.bestorigin.monolith.cart.api.CartDtos.CartBlockingReason;
import com.bestorigin.monolith.cart.api.CartDtos.CartLinePrice;
import com.bestorigin.monolith.cart.api.CartDtos.CartLineResponse;
import com.bestorigin.monolith.cart.api.CartDtos.CartResponse;
import com.bestorigin.monolith.cart.api.CartDtos.CartStatus;
import com.bestorigin.monolith.cart.api.CartDtos.CartTotalsResponse;
import com.bestorigin.monolith.cart.api.CartDtos.CartType;
import com.bestorigin.monolith.cart.api.CartDtos.CartValidationResponse;
import com.bestorigin.monolith.cart.api.CartDtos.ChangeQuantityRequest;
import com.bestorigin.monolith.cart.api.CartDtos.OfferStatus;
import com.bestorigin.monolith.cart.api.CartDtos.OfferType;
import com.bestorigin.monolith.cart.api.CartDtos.RoleSegment;
import com.bestorigin.monolith.cart.api.CartDtos.ShoppingOfferResponse;
import com.bestorigin.monolith.cart.api.CartDtos.ShoppingOffersResponse;
import com.bestorigin.monolith.cart.domain.CartRepository;
import com.bestorigin.monolith.cart.domain.CartSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultCartService implements CartService {

    private static final String DEFAULT_CAMPAIGN = "CMP-2026-05";
    private static final String RECALCULATED = "STR_MNEMO_CART_RECALCULATED";
    private static final String ITEM_UNAVAILABLE = "STR_MNEMO_CART_ITEM_UNAVAILABLE";
    private static final String QUANTITY_LIMIT = "STR_MNEMO_CART_QUANTITY_LIMIT_EXCEEDED";
    private static final String OFFER_AVAILABLE = "STR_MNEMO_CART_OFFER_AVAILABLE";
    private static final String OFFER_APPLIED = "STR_MNEMO_CART_OFFER_APPLIED";
    private static final String OFFER_UNAVAILABLE = "STR_MNEMO_CART_OFFER_UNAVAILABLE";
    private static final String SUPPLEMENTARY_FORBIDDEN = "STR_MNEMO_SUPPLEMENTARY_ORDER_FORBIDDEN";

    private final CartRepository repository;

    public DefaultCartService(CartRepository repository) {
        this.repository = repository;
    }

    @Override
    public synchronized CartResponse getCurrentCart(String userContextId, CartType cartType) {
        CartSnapshot cart = currentCart(userContextId, cartType);
        recalculate(cart);
        return response(cart, RECALCULATED);
    }

    @Override
    public synchronized CartResponse addItem(String userContextId, CartType cartType, AddCartItemRequest request, String idempotencyKey) {
        if (cartType == CartType.SUPPLEMENTARY && role(userContextId) != RoleSegment.PARTNER) {
            throw new CartAccessDeniedException(SUPPLEMENTARY_FORBIDDEN);
        }
        if (request == null || request.productCode() == null || request.productCode().isBlank() || request.quantity() <= 0) {
            throw new CartValidationException(ITEM_UNAVAILABLE);
        }
        CartSnapshot cart = currentCart(userContextId, cartType);
        ProductData product = product(request.productCode());
        int safeQuantity = product.maxQuantity() <= 0 ? request.quantity() : Math.min(request.quantity(), product.maxQuantity());
        AvailabilityStatus status = product.availability();
        String messageCode = status == AvailabilityStatus.AVAILABLE || status == AvailabilityStatus.LOW_STOCK ? null : ITEM_UNAVAILABLE;
        if (messageCode == null && request.quantity() > product.maxQuantity()) {
            messageCode = QUANTITY_LIMIT;
        }
        upsertLine(cart, product, safeQuantity, source(request.source(), cartType), status, messageCode);
        recalculate(cart);
        return response(cart, messageCode == null ? RECALCULATED : messageCode);
    }

    @Override
    public synchronized CartResponse changeQuantity(String userContextId, UUID lineId, ChangeQuantityRequest request, String idempotencyKey) {
        CartSnapshot cart = currentCart(userContextId, CartType.MAIN);
        int quantity = request == null ? 1 : Math.max(1, request.quantity());
        List<CartLineResponse> changed = cart.lines().stream()
                .map(line -> line.lineId().equals(lineId) ? withQuantity(line, quantity) : line)
                .toList();
        cart.lines().clear();
        cart.lines().addAll(changed);
        recalculate(cart);
        return response(cart, RECALCULATED);
    }

    @Override
    public synchronized CartResponse removeLine(String userContextId, UUID lineId, String idempotencyKey) {
        CartSnapshot cart = currentCart(userContextId, CartType.MAIN);
        cart.lines().removeIf(line -> line.lineId().equals(lineId));
        recalculate(cart);
        return response(cart, "STR_MNEMO_CART_ITEM_REMOVED");
    }

    @Override
    public synchronized ShoppingOffersResponse getShoppingOffers(String userContextId, CartType cartType) {
        CartSnapshot cart = currentCart(userContextId, cartType);
        return new ShoppingOffersResponse(cart.cartId(), cart.cartType(), offers(cartType, cart.lines().isEmpty()));
    }

    @Override
    public synchronized CartResponse applyOffer(String userContextId, CartType cartType, String offerId, String idempotencyKey) {
        CartSnapshot cart = currentCart(userContextId, cartType);
        if (!"SET-GLOW-001".equals(offerId) && cartType == CartType.MAIN) {
            throw new CartValidationException(OFFER_UNAVAILABLE);
        }
        if (cartType == CartType.SUPPLEMENTARY && !"SUPP-REFILL-001".equals(offerId)) {
            throw new CartValidationException(OFFER_UNAVAILABLE);
        }
        ProductData product = cartType == CartType.MAIN ? product("BOG-SERUM-002") : product("BOG-CREAM-001");
        upsertLine(cart, product, 1, cartType == CartType.MAIN ? "SHOPPING_OFFER" : "SUPPLEMENTARY_OFFER", AvailabilityStatus.AVAILABLE, null);
        cart.appliedOffers().removeIf(offer -> offer.offerId().equals(offerId));
        cart.appliedOffers().add(new AppliedOfferResponse(
                offerId,
                cartType == CartType.MAIN ? OfferType.CROSS_SELL : OfferType.RETENTION,
                "APPLIED",
                money("250.00"),
                cartType == CartType.MAIN ? "BOG-SERUM-002" : null,
                OFFER_APPLIED
        ));
        recalculate(cart);
        return response(cart, RECALCULATED);
    }

    @Override
    public synchronized CartResponse supportView(String supportContextId, String userId, CartType cartType) {
        if (role(supportContextId) != RoleSegment.SUPPORT) {
            throw new CartAccessDeniedException("STR_MNEMO_CART_SUPPORT_FORBIDDEN");
        }
        CartSnapshot cart = currentCart(userId + "-api-session-support-view", cartType);
        if (cart.lines().isEmpty()) {
            upsertLine(cart, product("BOG-CREAM-001"), 1, "SUPPORT_VIEW", AvailabilityStatus.AVAILABLE, null);
        }
        recalculate(cart);
        return response(cart, RECALCULATED);
    }

    private CartSnapshot currentCart(String userContextId, CartType cartType) {
        CartType safeType = cartType == null ? CartType.MAIN : cartType;
        String owner = userContextId == null || userContextId.isBlank() ? "anonymous" : userContextId;
        return repository.findActiveCart(owner, safeType)
                .orElseGet(() -> repository.save(new CartSnapshot(
                        UUID.nameUUIDFromBytes((owner + safeType).getBytes(StandardCharsets.UTF_8)),
                        owner,
                        safeType,
                        DEFAULT_CAMPAIGN,
                        role(owner),
                        role(owner) == RoleSegment.PARTNER ? "partner-context-009" : null
                )));
    }

    private void upsertLine(CartSnapshot cart, ProductData product, int quantity, String source, AvailabilityStatus status, String messageCode) {
        UUID lineId = UUID.nameUUIDFromBytes((cart.cartId() + product.productCode() + source).getBytes(StandardCharsets.UTF_8));
        cart.lines().removeIf(line -> line.lineId().equals(lineId) || line.productCode().equals(product.productCode()) && line.source().equals(source));
        BigDecimal unit = product.price();
        BigDecimal promo = product.promoPrice();
        BigDecimal effective = promo == null ? unit : promo;
        cart.lines().add(new CartLineResponse(
                lineId,
                product.productCode(),
                product.name(),
                "/assets/catalog/" + product.productCode().toLowerCase() + ".jpg",
                quantity,
                new CartLinePrice(unit, promo, effective.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP)),
                new CartAvailability(status, status == AvailabilityStatus.RESERVED ? quantity : null, product.maxQuantity(), messageCode),
                source
        ));
    }

    private CartLineResponse withQuantity(CartLineResponse line, int quantity) {
        BigDecimal effective = line.price().promoUnitPrice() == null ? line.price().unitPrice() : line.price().promoUnitPrice();
        return new CartLineResponse(
                line.lineId(),
                line.productCode(),
                line.name(),
                line.imageUrl(),
                quantity,
                new CartLinePrice(line.price().unitPrice(), line.price().promoUnitPrice(), effective.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP)),
                line.availability(),
                line.source()
        );
    }

    private void recalculate(CartSnapshot cart) {
        BigDecimal subtotal = cart.lines().stream()
                .map(line -> line.price().unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal grand = cart.lines().stream()
                .map(line -> line.price().lineTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = subtotal.subtract(grand).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal benefit = cart.appliedOffers().stream()
                .map(AppliedOfferResponse::benefitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        cart.totals(new CartTotalsResponse(subtotal, discount, benefit, money("5000.00").subtract(grand).max(BigDecimal.ZERO), grand));
        cart.status(validation(cart).valid() ? CartStatus.READY_FOR_CHECKOUT : CartStatus.BLOCKED);
        cart.incrementVersion();
        repository.save(cart);
    }

    private CartResponse response(CartSnapshot cart, String messageCode) {
        return new CartResponse(
                cart.cartId(),
                cart.cartType(),
                cart.campaignId(),
                cart.roleSegment(),
                cart.partnerContextId(),
                cart.status(),
                "RUB",
                cart.version(),
                List.copyOf(cart.lines()),
                List.copyOf(cart.appliedOffers()),
                cart.totals(),
                validation(cart),
                messageCode
        );
    }

    private CartValidationResponse validation(CartSnapshot cart) {
        List<CartBlockingReason> reasons = cart.lines().stream()
                .filter(line -> line.availability().messageCode() != null)
                .map(line -> new CartBlockingReason(line.lineId(), "ITEM_UNAVAILABLE", line.availability().messageCode()))
                .toList();
        return new CartValidationResponse(reasons.isEmpty(), reasons, reasons.isEmpty() ? "/checkout" : null);
    }

    private static List<ShoppingOfferResponse> offers(CartType cartType, boolean emptyCart) {
        if (cartType == CartType.SUPPLEMENTARY) {
            return List.of(new ShoppingOfferResponse(
                    "SUPP-REFILL-001",
                    "cart.offer.supplementary.refill",
                    OfferType.RETENTION,
                    OfferStatus.AVAILABLE,
                    "PARTNER_SUPPLEMENTARY_AVAILABLE",
                    null,
                    List.of("BOG-CREAM-001"),
                    money("150.00"),
                    OFFER_AVAILABLE
            ));
        }
        return List.of(new ShoppingOfferResponse(
                "SET-GLOW-001",
                "cart.offer.setGlow",
                OfferType.CROSS_SELL,
                emptyCart ? OfferStatus.PENDING_CONDITION : OfferStatus.AVAILABLE,
                "ADD_MAIN_CART_ITEM",
                emptyCart ? "ADD_ONE_ITEM" : null,
                List.of("BOG-SERUM-002"),
                money("250.00"),
                OFFER_AVAILABLE
        ));
    }

    private static ProductData product(String productCode) {
        if ("BOG-REMOVED-003".equals(productCode)) {
            return new ProductData(productCode, "Снятый гель Best Ori Gin", money("900.00"), null, 0, AvailabilityStatus.REMOVED_FROM_CAMPAIGN);
        }
        if ("BOG-SOLDOUT-001".equals(productCode)) {
            return new ProductData(productCode, "Бархатная помада Best Ori Gin", money("790.00"), null, 0, AvailabilityStatus.UNAVAILABLE);
        }
        if ("BOG-SERUM-002".equals(productCode)) {
            return new ProductData(productCode, "Сыворотка Best Ori Gin", money("1590.00"), money("1390.00"), 20, AvailabilityStatus.AVAILABLE);
        }
        return new ProductData("BOG-CREAM-001", "Увлажняющий крем Best Ori Gin", money("1290.00"), money("990.00"), 50, AvailabilityStatus.AVAILABLE);
    }

    private static String source(String requested, CartType cartType) {
        if (requested == null || requested.isBlank()) {
            return cartType == CartType.SUPPLEMENTARY ? "SUPPLEMENTARY_OFFER" : "PRODUCT_CARD";
        }
        return requested;
    }

    private static RoleSegment role(String userContextId) {
        String value = userContextId == null ? "" : userContextId.toLowerCase();
        if (value.contains("support")) {
            return RoleSegment.SUPPORT;
        }
        if (value.contains("partner")) {
            return RoleSegment.PARTNER;
        }
        return RoleSegment.CUSTOMER;
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record ProductData(
            String productCode,
            String name,
            BigDecimal price,
            BigDecimal promoPrice,
            int maxQuantity,
            AvailabilityStatus availability
    ) {
    }
}
