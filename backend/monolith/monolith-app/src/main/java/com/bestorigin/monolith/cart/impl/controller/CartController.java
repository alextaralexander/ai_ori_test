package com.bestorigin.monolith.cart.impl.controller;

import com.bestorigin.monolith.cart.api.CartDtos.AddCartItemRequest;
import com.bestorigin.monolith.cart.api.CartDtos.CartResponse;
import com.bestorigin.monolith.cart.api.CartDtos.CartType;
import com.bestorigin.monolith.cart.api.CartDtos.CartValidationResponse;
import com.bestorigin.monolith.cart.api.CartDtos.ChangeQuantityRequest;
import com.bestorigin.monolith.cart.api.CartDtos.ErrorResponse;
import com.bestorigin.monolith.cart.api.CartDtos.ShoppingOffersResponse;
import com.bestorigin.monolith.cart.impl.service.CartAccessDeniedException;
import com.bestorigin.monolith.cart.impl.service.CartService;
import com.bestorigin.monolith.cart.impl.service.CartValidationException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping("/current")
    public CartResponse current(@RequestHeader HttpHeaders headers) {
        return service.getCurrentCart(userContext(headers), CartType.MAIN);
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @RequestHeader HttpHeaders headers,
            @RequestBody AddCartItemRequest request
    ) {
        return service.addItem(userContext(headers), CartType.MAIN, request, idempotencyKey(headers));
    }

    @PatchMapping("/items/{lineId}")
    public CartResponse changeQuantity(
            @RequestHeader HttpHeaders headers,
            @PathVariable UUID lineId,
            @RequestBody ChangeQuantityRequest request
    ) {
        return service.changeQuantity(userContext(headers), lineId, request, idempotencyKey(headers));
    }

    @DeleteMapping("/items/{lineId}")
    public CartResponse removeLine(
            @RequestHeader HttpHeaders headers,
            @PathVariable UUID lineId
    ) {
        return service.removeLine(userContext(headers), lineId, idempotencyKey(headers));
    }

    @GetMapping("/shopping-offers")
    public ShoppingOffersResponse shoppingOffers(@RequestHeader HttpHeaders headers) {
        return service.getShoppingOffers(userContext(headers), CartType.MAIN);
    }

    @PostMapping("/shopping-offers/{offerId}/apply")
    public CartResponse applyOffer(
            @RequestHeader HttpHeaders headers,
            @PathVariable String offerId
    ) {
        return service.applyOffer(userContext(headers), CartType.MAIN, offerId, idempotencyKey(headers));
    }

    @PostMapping("/validate")
    public CartValidationResponse validate(@RequestHeader HttpHeaders headers) {
        return service.getCurrentCart(userContext(headers), CartType.MAIN).validation();
    }

    @GetMapping("/supplementary/current")
    public CartResponse supplementary(@RequestHeader HttpHeaders headers) {
        return service.getCurrentCart(userContext(headers), CartType.SUPPLEMENTARY);
    }

    @PostMapping("/supplementary/items")
    public CartResponse addSupplementaryItem(
            @RequestHeader HttpHeaders headers,
            @RequestBody AddCartItemRequest request
    ) {
        return service.addItem(userContext(headers), CartType.SUPPLEMENTARY, request, idempotencyKey(headers));
    }

    @GetMapping("/supplementary/shopping-offers")
    public ShoppingOffersResponse supplementaryOffers(@RequestHeader HttpHeaders headers) {
        return service.getShoppingOffers(userContext(headers), CartType.SUPPLEMENTARY);
    }

    @PostMapping("/supplementary/shopping-offers/{offerId}/apply")
    public CartResponse applySupplementaryOffer(
            @RequestHeader HttpHeaders headers,
            @PathVariable String offerId
    ) {
        return service.applyOffer(userContext(headers), CartType.SUPPLEMENTARY, offerId, idempotencyKey(headers));
    }

    @GetMapping("/support/users/{userId}/current")
    public CartResponse supportView(
            @RequestHeader HttpHeaders headers,
            @PathVariable String userId,
            @RequestParam(defaultValue = "MAIN") CartType cartType
    ) {
        return service.supportView(userContext(headers), userId, cartType);
    }

    @ExceptionHandler(CartValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(CartValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("CART_VALIDATION_FAILED", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(CartAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(CartAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("CART_FORBIDDEN", ex.getMessage(), Map.of()));
    }

    private static String userContext(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "anonymous";
        }
        return value.replace("Bearer ", "").trim();
    }

    private static String idempotencyKey(HttpHeaders headers) {
        String value = headers.getFirst("Idempotency-Key");
        return value == null || value.isBlank() ? "implicit-idempotency-key" : value;
    }
}
