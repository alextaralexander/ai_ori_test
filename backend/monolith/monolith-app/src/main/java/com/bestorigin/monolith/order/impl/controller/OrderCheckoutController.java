package com.bestorigin.monolith.order.impl.controller;

import com.bestorigin.monolith.order.api.OrderDtos.AddressRequest;
import com.bestorigin.monolith.order.api.OrderDtos.BenefitApplyRequest;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutDraftResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutValidationResponse;
import com.bestorigin.monolith.order.api.OrderDtos.ConfirmCheckoutRequest;
import com.bestorigin.monolith.order.api.OrderDtos.DeliverySelectionRequest;
import com.bestorigin.monolith.order.api.OrderDtos.ErrorResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderConfirmationResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderDetailsResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderHistoryPageResponse;
import com.bestorigin.monolith.order.api.OrderDtos.PaymentSelectionRequest;
import com.bestorigin.monolith.order.api.OrderDtos.RecipientRequest;
import com.bestorigin.monolith.order.api.OrderDtos.RepeatOrderResponse;
import com.bestorigin.monolith.order.api.OrderDtos.StartCheckoutRequest;
import com.bestorigin.monolith.order.api.OrderDtos.ValidationReasonResponse;
import com.bestorigin.monolith.order.impl.service.OrderCheckoutAccessDeniedException;
import com.bestorigin.monolith.order.impl.service.OrderCheckoutNotFoundException;
import com.bestorigin.monolith.order.impl.service.OrderCheckoutService;
import com.bestorigin.monolith.order.impl.service.OrderCheckoutValidationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderCheckoutController {

    private final OrderCheckoutService service;

    public OrderCheckoutController(OrderCheckoutService service) {
        this.service = service;
    }

    @PostMapping("/checkouts")
    public CheckoutDraftResponse start(@RequestHeader HttpHeaders headers, @RequestBody StartCheckoutRequest request) {
        return service.start(userContext(headers), request, idempotencyKey(headers));
    }

    @GetMapping("/checkouts/{checkoutId}")
    public CheckoutDraftResponse get(@RequestHeader HttpHeaders headers, @PathVariable UUID checkoutId) {
        return service.get(userContext(headers), checkoutId);
    }

    @PutMapping("/checkouts/{checkoutId}/recipient")
    public CheckoutDraftResponse recipient(@RequestHeader HttpHeaders headers, @PathVariable UUID checkoutId, @RequestBody RecipientRequest request) {
        return service.updateRecipient(userContext(headers), checkoutId, request);
    }

    @PutMapping("/checkouts/{checkoutId}/address")
    public CheckoutDraftResponse address(@RequestHeader HttpHeaders headers, @PathVariable UUID checkoutId, @RequestBody AddressRequest request) {
        return service.updateAddress(userContext(headers), checkoutId, request);
    }

    @PutMapping("/checkouts/{checkoutId}/delivery")
    public CheckoutDraftResponse delivery(@RequestHeader HttpHeaders headers, @PathVariable UUID checkoutId, @RequestBody DeliverySelectionRequest request) {
        return service.selectDelivery(userContext(headers), checkoutId, request);
    }

    @PutMapping("/checkouts/{checkoutId}/payment")
    public CheckoutDraftResponse payment(@RequestHeader HttpHeaders headers, @PathVariable UUID checkoutId, @RequestBody PaymentSelectionRequest request) {
        return service.selectPayment(userContext(headers), checkoutId, request, idempotencyKey(headers));
    }

    @PutMapping("/checkouts/{checkoutId}/benefits")
    public CheckoutDraftResponse benefits(@RequestHeader HttpHeaders headers, @PathVariable UUID checkoutId, @RequestBody BenefitApplyRequest request) {
        return service.applyBenefits(userContext(headers), checkoutId, request, idempotencyKey(headers));
    }

    @PostMapping("/checkouts/{checkoutId}/validation")
    public CheckoutValidationResponse validate(@RequestHeader HttpHeaders headers, @PathVariable UUID checkoutId) {
        return service.validate(userContext(headers), checkoutId);
    }

    @PostMapping("/checkouts/{checkoutId}/confirm")
    public OrderConfirmationResponse confirm(@RequestHeader HttpHeaders headers, @PathVariable UUID checkoutId, @RequestBody ConfirmCheckoutRequest request) {
        return service.confirm(userContext(headers), checkoutId, request, idempotencyKey(headers));
    }

    @GetMapping("/orders/{orderNumber}")
    public OrderConfirmationResponse order(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber) {
        return service.getOrder(userContext(headers), orderNumber);
    }

    @GetMapping("/order-history")
    public OrderHistoryPageResponse orderHistory(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String campaignId,
            @RequestParam(required = false) String orderType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.searchOrderHistory(userContext(headers), query, campaignId, orderType, page, size);
    }

    @GetMapping("/order-history/{orderNumber}")
    public OrderDetailsResponse orderDetails(
            @RequestHeader HttpHeaders headers,
            @PathVariable String orderNumber,
            @RequestParam(required = false) String supportCustomerId,
            @RequestParam(required = false) String reason
    ) {
        return service.getOrderHistoryDetails(userContext(headers), orderNumber, supportCustomerId, reason);
    }

    @PostMapping("/order-history/{orderNumber}/repeat")
    public RepeatOrderResponse repeatOrder(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber) {
        return service.repeatOrder(userContext(headers), orderNumber, idempotencyKey(headers));
    }

    @ExceptionHandler(OrderCheckoutValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(OrderCheckoutValidationException ex) {
        HttpStatus status = ex.statusCode() == 409 ? HttpStatus.CONFLICT : ex.statusCode() == 403 ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(error(ex.getMessage()));
    }

    @ExceptionHandler(OrderCheckoutAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(OrderCheckoutAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
    }

    @ExceptionHandler(OrderCheckoutNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderCheckoutNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage()));
    }

    private static ErrorResponse error(String code) {
        return new ErrorResponse(code, List.of(new ValidationReasonResponse(code, "BLOCKING", "order")), Map.of());
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
