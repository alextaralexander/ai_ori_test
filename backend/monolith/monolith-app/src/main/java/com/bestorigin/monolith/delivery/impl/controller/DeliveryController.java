package com.bestorigin.monolith.delivery.impl.controller;

import com.bestorigin.monolith.delivery.api.DeliveryDtos.CreateShipmentRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.DeliveryErrorResponse;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.DeliveryOptionsResponse;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ExternalDeliveryStatusEvent;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupAcceptRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupDeliverRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupPartialDeliverRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupPointDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ShipmentDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ShipmentStatus;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.TrackingEventDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.TrackingTimelineDto;
import com.bestorigin.monolith.delivery.impl.exception.DeliveryAccessDeniedException;
import com.bestorigin.monolith.delivery.impl.exception.DeliveryConflictException;
import com.bestorigin.monolith.delivery.impl.exception.DeliveryValidationException;
import com.bestorigin.monolith.delivery.impl.service.DeliveryService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService service;

    public DeliveryController(DeliveryService service) {
        this.service = service;
    }

    @GetMapping("/options")
    public DeliveryOptionsResponse options(@RequestHeader HttpHeaders headers, @RequestParam UUID orderDraftId, @RequestParam(required = false) String city) {
        return service.options(token(headers), orderDraftId, city);
    }

    @GetMapping("/pickup-points")
    public List<PickupPointDto> pickupPoints(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String city, @RequestParam(required = false) String region) {
        return service.pickupPoints(token(headers), city, region);
    }

    @PostMapping("/shipments")
    public ResponseEntity<ShipmentDto> createShipment(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId, @RequestBody CreateShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createShipment(token(headers), idempotencyKey, correlationId, request));
    }

    @GetMapping("/shipments/{shipmentId}/tracking")
    public TrackingTimelineDto tracking(@RequestHeader HttpHeaders headers, @PathVariable UUID shipmentId) {
        return service.tracking(token(headers), shipmentId);
    }

    @GetMapping("/pickup-owner/shipments")
    public List<ShipmentDto> pickupOwnerShipments(@RequestHeader HttpHeaders headers, @RequestParam(required = false) ShipmentStatus status) {
        return service.pickupOwnerShipments(token(headers), status);
    }

    @PostMapping("/pickup-owner/shipments/{shipmentId}/accept")
    public ShipmentDto accept(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId, @PathVariable UUID shipmentId, @RequestBody PickupAcceptRequest request) {
        return service.accept(token(headers), idempotencyKey, correlationId, shipmentId, request);
    }

    @PostMapping("/pickup-owner/shipments/{shipmentId}/deliver")
    public ShipmentDto deliver(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId, @PathVariable UUID shipmentId, @RequestBody PickupDeliverRequest request) {
        return service.deliver(token(headers), idempotencyKey, correlationId, shipmentId, request);
    }

    @PostMapping("/pickup-owner/shipments/{shipmentId}/partial-deliver")
    public ShipmentDto partialDeliver(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId, @PathVariable UUID shipmentId, @RequestBody PickupPartialDeliverRequest request) {
        return service.partialDeliver(token(headers), idempotencyKey, correlationId, shipmentId, request);
    }

    @PostMapping("/integration/status-events")
    public ResponseEntity<TrackingEventDto> statusEvent(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId, @RequestBody ExternalDeliveryStatusEvent event) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.acceptExternalStatus(token(headers), idempotencyKey, correlationId, event));
    }

    @ExceptionHandler(DeliveryAccessDeniedException.class)
    public ResponseEntity<DeliveryErrorResponse> forbidden(DeliveryAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(DeliveryConflictException.class)
    public ResponseEntity<DeliveryErrorResponse> conflict(DeliveryConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), ex.details()));
    }

    @ExceptionHandler(DeliveryValidationException.class)
    public ResponseEntity<DeliveryErrorResponse> validation(DeliveryValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static DeliveryErrorResponse error(String code, List<String> details) {
        return new DeliveryErrorResponse(code, "CORR-037-ERROR", details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return value == null ? "" : value.replace("Bearer ", "").trim();
    }
}
