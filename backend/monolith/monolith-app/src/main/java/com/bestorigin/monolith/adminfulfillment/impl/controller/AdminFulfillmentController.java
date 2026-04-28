package com.bestorigin.monolith.adminfulfillment.impl.controller;

import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.AdminFulfillmentErrorResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryServicePage;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryServiceRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryServiceResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliverySlaRuleRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliverySlaRuleResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryTariffRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryTariffResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentEventResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentShipmentPage;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentTaskRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentTaskResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.IntegrationEventPage;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.PickupDeliveryRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.PickupPointPage;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.PickupPointRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.PickupPointResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.PickupShipmentResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.ReasonRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.StageTransitionRequest;
import com.bestorigin.monolith.adminfulfillment.impl.exception.AdminFulfillmentAccessDeniedException;
import com.bestorigin.monolith.adminfulfillment.impl.exception.AdminFulfillmentConflictException;
import com.bestorigin.monolith.adminfulfillment.impl.exception.AdminFulfillmentValidationException;
import com.bestorigin.monolith.adminfulfillment.impl.service.AdminFulfillmentService;
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
@RequestMapping("/api/admin/fulfillment")
public class AdminFulfillmentController {
    private final AdminFulfillmentService service;

    public AdminFulfillmentController(AdminFulfillmentService service) {
        this.service = service;
    }

    @GetMapping("/dashboard/shipments")
    public FulfillmentShipmentPage searchShipments(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String warehouseCode, @RequestParam(required = false) String stage, @RequestParam(required = false) String status, @RequestParam(required = false) Boolean slaRisk, @RequestParam(required = false) UUID pickupPointId, @RequestParam(required = false) String correlationId) {
        return service.searchShipments(token(headers), warehouseCode, stage, status, slaRisk, pickupPointId, correlationId);
    }

    @PostMapping("/tasks")
    public ResponseEntity<FulfillmentTaskResponse> createTask(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody FulfillmentTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTask(token(headers), idempotencyKey, request));
    }

    @PostMapping("/tasks/{taskId}/stage")
    public FulfillmentTaskResponse moveStage(@RequestHeader HttpHeaders headers, @PathVariable UUID taskId, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody StageTransitionRequest request) {
        return service.moveStage(token(headers), taskId, idempotencyKey, request);
    }

    @PostMapping("/tasks/{taskId}/exceptions")
    public ResponseEntity<FulfillmentEventResponse> createException(@RequestHeader HttpHeaders headers, @PathVariable UUID taskId, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ReasonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createException(token(headers), taskId, idempotencyKey, request));
    }

    @GetMapping("/delivery-services")
    public DeliveryServicePage deliveryServices(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String status, @RequestParam(required = false) String zoneCode) {
        return service.searchDeliveryServices(token(headers), status, zoneCode);
    }

    @PostMapping("/delivery-services")
    public ResponseEntity<DeliveryServiceResponse> createDeliveryService(@RequestHeader HttpHeaders headers, @RequestBody DeliveryServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDeliveryService(token(headers), request));
    }

    @PostMapping("/delivery-services/{serviceId}/activate")
    public DeliveryServiceResponse activateDeliveryService(@RequestHeader HttpHeaders headers, @PathVariable UUID serviceId) {
        return service.activateDeliveryService(token(headers), serviceId);
    }

    @PostMapping("/delivery-services/{serviceId}/tariffs")
    public ResponseEntity<DeliveryTariffResponse> addTariff(@RequestHeader HttpHeaders headers, @PathVariable UUID serviceId, @RequestBody DeliveryTariffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addTariff(token(headers), serviceId, request));
    }

    @PostMapping("/delivery-services/{serviceId}/sla-rules")
    public ResponseEntity<DeliverySlaRuleResponse> addSlaRule(@RequestHeader HttpHeaders headers, @PathVariable UUID serviceId, @RequestBody DeliverySlaRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addSlaRule(token(headers), serviceId, request));
    }

    @GetMapping("/pickup-points")
    public PickupPointPage pickupPoints(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String status, @RequestParam(required = false) String ownerUserId, @RequestParam(required = false) String zoneCode) {
        return service.searchPickupPoints(token(headers), status, ownerUserId, zoneCode);
    }

    @PostMapping("/pickup-points")
    public ResponseEntity<PickupPointResponse> createPickupPoint(@RequestHeader HttpHeaders headers, @RequestBody PickupPointRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPickupPoint(token(headers), request));
    }

    @PostMapping("/pickup-points/{pickupPointId}/activate")
    public PickupPointResponse activatePickupPoint(@RequestHeader HttpHeaders headers, @PathVariable UUID pickupPointId) {
        return service.activatePickupPoint(token(headers), pickupPointId);
    }

    @PostMapping("/pickup-points/{pickupPointId}/temporary-close")
    public PickupPointResponse temporaryClosePickupPoint(@RequestHeader HttpHeaders headers, @PathVariable UUID pickupPointId, @RequestBody ReasonRequest request) {
        return service.temporaryClosePickupPoint(token(headers), pickupPointId, request);
    }

    @PostMapping("/pickup-shipments/{shipmentId}/accept")
    public PickupShipmentResponse acceptPickupShipment(@RequestHeader HttpHeaders headers, @PathVariable String shipmentId, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.acceptPickupShipment(token(headers), shipmentId, idempotencyKey);
    }

    @PostMapping("/pickup-shipments/{shipmentId}/deliver")
    public PickupShipmentResponse deliverPickupShipment(@RequestHeader HttpHeaders headers, @PathVariable String shipmentId, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody PickupDeliveryRequest request) {
        return service.deliverPickupShipment(token(headers), shipmentId, idempotencyKey, request);
    }

    @PostMapping("/pickup-shipments/{shipmentId}/not-collected")
    public PickupShipmentResponse markNotCollected(@RequestHeader HttpHeaders headers, @PathVariable String shipmentId, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ReasonRequest request) {
        return service.markNotCollected(token(headers), shipmentId, idempotencyKey, request);
    }

    @GetMapping("/integration-events")
    public IntegrationEventPage integrationEvents(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String sourceSystem, @RequestParam(required = false) String status, @RequestParam(required = false) String correlationId) {
        return service.searchIntegrationEvents(token(headers), sourceSystem, status, correlationId);
    }

    @ExceptionHandler(AdminFulfillmentAccessDeniedException.class)
    public ResponseEntity<AdminFulfillmentErrorResponse> handleForbidden(AdminFulfillmentAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AdminFulfillmentConflictException.class)
    public ResponseEntity<AdminFulfillmentErrorResponse> handleConflict(AdminFulfillmentConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AdminFulfillmentValidationException.class)
    public ResponseEntity<AdminFulfillmentErrorResponse> handleValidation(AdminFulfillmentValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminFulfillmentErrorResponse error(String messageCode, List<String> details) {
        return new AdminFulfillmentErrorResponse(messageCode, "CORR-039-ERROR", details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return value == null ? "" : value.replace("Bearer ", "").trim();
    }
}
