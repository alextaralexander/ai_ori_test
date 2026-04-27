package com.bestorigin.monolith.adminwms.impl.controller;

import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.AdminWmsErrorResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.AuditEventPage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.AvailabilityRuleRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.ReservationCreateRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.ReservationResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.StockPage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.StockResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SupplyCreateRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SupplyPage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SupplyResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SyncMessagePage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SyncRunCreateRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SyncRunResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.WarehouseCreateRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.WarehousePage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.WarehouseResponse;
import com.bestorigin.monolith.adminwms.impl.exception.AdminWmsAccessDeniedException;
import com.bestorigin.monolith.adminwms.impl.exception.AdminWmsConflictException;
import com.bestorigin.monolith.adminwms.impl.exception.AdminWmsValidationException;
import com.bestorigin.monolith.adminwms.impl.service.AdminWmsService;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/admin/wms")
public class AdminWmsController {

    private final AdminWmsService service;

    public AdminWmsController(AdminWmsService service) {
        this.service = service;
    }

    @GetMapping("/warehouses")
    public WarehousePage warehouses(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String status, @RequestParam(required = false) String regionCode, @RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchWarehouses(token(headers), status, regionCode, search, page, size);
    }

    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponse> createWarehouse(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody WarehouseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createWarehouse(token(headers), idempotencyKey, request));
    }

    @PatchMapping("/warehouses/{warehouseId}")
    public WarehouseResponse updateWarehouse(@RequestHeader HttpHeaders headers, @PathVariable UUID warehouseId, @RequestBody WarehouseCreateRequest request) {
        return service.updateWarehouse(token(headers), warehouseId, request);
    }

    @GetMapping("/stocks")
    public StockPage stocks(@RequestHeader HttpHeaders headers, @RequestParam(required = false) UUID warehouseId, @RequestParam(required = false) String sku, @RequestParam(required = false) String catalogPeriodCode, @RequestParam(required = false) String channelCode, @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchStocks(token(headers), warehouseId, sku, catalogPeriodCode, channelCode, status, page, size);
    }

    @PostMapping("/stocks/{stockItemId}/availability-rule")
    public StockResponse changeAvailabilityRule(@RequestHeader HttpHeaders headers, @PathVariable UUID stockItemId, @RequestBody AvailabilityRuleRequest request) {
        return service.changeAvailabilityRule(token(headers), stockItemId, request);
    }

    @GetMapping("/supplies")
    public SupplyPage supplies(@RequestHeader HttpHeaders headers, @RequestParam(required = false) UUID warehouseId, @RequestParam(required = false) String status, @RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchSupplies(token(headers), warehouseId, status, search, page, size);
    }

    @PostMapping("/supplies")
    public ResponseEntity<SupplyResponse> createSupply(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody SupplyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSupply(token(headers), idempotencyKey, request));
    }

    @PostMapping("/supplies/{supplyId}/acceptance")
    public SupplyResponse acceptSupply(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID supplyId, @RequestBody SupplyCreateRequest request) {
        return service.acceptSupply(token(headers), idempotencyKey, supplyId, request);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserveStock(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ReservationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reserveStock(token(headers), idempotencyKey, request));
    }

    @PostMapping("/reservations/{reservationId}/release")
    public ReservationResponse releaseReservation(@RequestHeader HttpHeaders headers, @PathVariable UUID reservationId) {
        return service.releaseReservation(token(headers), reservationId);
    }

    @PostMapping("/sync-runs")
    public ResponseEntity<SyncRunResponse> startSyncRun(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody SyncRunCreateRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.startSyncRun(token(headers), idempotencyKey, request));
    }

    @GetMapping("/sync-messages")
    public SyncMessagePage syncMessages(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String messageStatus, @RequestParam(required = false) String entityType, @RequestParam(required = false) String correlationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchSyncMessages(token(headers), messageStatus, entityType, correlationId, page, size);
    }

    @GetMapping("/audit-events")
    public AuditEventPage audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String entityType, @RequestParam(required = false) String entityId, @RequestParam(required = false) String correlationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.audit(token(headers), entityType, entityId, correlationId, page, size);
    }

    @ExceptionHandler(AdminWmsAccessDeniedException.class)
    public ResponseEntity<AdminWmsErrorResponse> handleForbidden(AdminWmsAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminWmsConflictException.class)
    public ResponseEntity<AdminWmsErrorResponse> handleConflict(AdminWmsConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminWmsValidationException.class)
    public ResponseEntity<AdminWmsErrorResponse> handleValidation(AdminWmsValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminWmsErrorResponse error(String messageCode, java.util.List<String> details) {
        return new AdminWmsErrorResponse(messageCode, "CORR-032-ERROR", details == null ? java.util.List.of() : details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
