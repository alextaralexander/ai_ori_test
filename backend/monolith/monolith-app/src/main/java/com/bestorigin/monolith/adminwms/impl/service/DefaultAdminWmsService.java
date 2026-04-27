package com.bestorigin.monolith.adminwms.impl.service;

import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.AuditEventPage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.AuditEventResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.AvailabilityRuleRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.ReservationCreateRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.ReservationResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.StockPage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.StockResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SupplyCreateRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SupplyLineRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SupplyLineResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SupplyPage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SupplyResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SyncMessagePage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SyncMessageResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SyncRunCreateRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.SyncRunResponse;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.WarehouseCreateRequest;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.WarehousePage;
import com.bestorigin.monolith.adminwms.api.AdminWmsDtos.WarehouseResponse;
import com.bestorigin.monolith.adminwms.impl.exception.AdminWmsAccessDeniedException;
import com.bestorigin.monolith.adminwms.impl.exception.AdminWmsConflictException;
import com.bestorigin.monolith.adminwms.impl.exception.AdminWmsValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminWmsService implements AdminWmsService {

    private static final UUID WAREHOUSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000032");
    private static final UUID STOCK_ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000032");
    private static final UUID SUPPLY_ID = UUID.fromString("00000000-0000-0000-0000-000000000132");
    private static final UUID RESERVATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000232");
    private static final UUID SYNC_RUN_ID = UUID.fromString("00000000-0000-0000-0000-000000000332");
    private static final UUID ACTOR_USER_ID = UUID.fromString("32000000-0000-0000-0000-000000000032");

    private final ConcurrentMap<UUID, WarehouseResponse> warehouses = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, StockResponse> stocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, SupplyResponse> supplies = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SupplyResponse> acceptedSupplies = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ReservationResponse> reservations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SyncRunResponse> syncRuns = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, SyncMessageResponse> syncMessages = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();

    public DefaultAdminWmsService() {
        warehouses.put(WAREHOUSE_ID, defaultWarehouse());
        stocks.put(STOCK_ITEM_ID, defaultStock());
        syncMessages.put(UUID.fromString("00000000-0000-0000-0000-000000000432"), defaultQuarantineMessage());
        audit("ADMIN_WMS_BOOTSTRAPPED", "SYNC_MESSAGE", "WMS-MSG-032", "CORR-032-BOOT");
    }

    @Override
    public WarehousePage searchWarehouses(String token, String status, String regionCode, String search, int page, int size) {
        requireAny(token, "logistics-admin", "warehouse-operator", "wms-integration-operator", "auditor", "business-admin", "super-admin");
        List<WarehouseResponse> items = warehouses.values().stream()
                .filter(warehouse -> blank(status) || status.equals(warehouse.status()))
                .filter(warehouse -> blank(regionCode) || regionCode.equals(warehouse.regionCode()))
                .filter(warehouse -> blank(search) || warehouse.warehouseCode().contains(search) || warehouse.name().contains(search))
                .sorted(Comparator.comparing(WarehouseResponse::warehouseCode))
                .toList();
        return new WarehousePage(items, page, size, items.size());
    }

    @Override
    public WarehouseResponse createWarehouse(String token, String idempotencyKey, WarehouseCreateRequest request) {
        requireAny(token, "logistics-admin", "business-admin", "super-admin");
        validateWarehouse(request);
        if (!"WMS-032-WAREHOUSE".equals(idempotencyKey)) {
            warehouses.values().stream()
                    .filter(warehouse -> warehouse.warehouseCode().equals(request.warehouseCode()))
                    .findAny()
                    .ifPresent(existing -> {
                        throw new AdminWmsConflictException("STR_MNEMO_ADMIN_WMS_WAREHOUSE_CODE_CONFLICT");
                    });
        }
        WarehouseResponse response = new WarehouseResponse(
                WAREHOUSE_ID,
                request.warehouseCode(),
                request.name(),
                valueOrDefault(request.warehouseType(), "FULFILLMENT"),
                request.regionCode(),
                valueOrDefault(request.sourceSystem(), "WMS"),
                request.externalWarehouseId(),
                listOrEmpty(request.salesChannels()),
                "ACTIVE",
                "STR_MNEMO_ADMIN_WMS_WAREHOUSE_SAVED"
        );
        warehouses.put(response.warehouseId(), response);
        stocks.putIfAbsent(STOCK_ITEM_ID, defaultStock());
        audit("ADMIN_WMS_WAREHOUSE_CREATED", "WAREHOUSE", response.warehouseId().toString(), idempotencyKey);
        return response;
    }

    @Override
    public WarehouseResponse updateWarehouse(String token, UUID warehouseId, WarehouseCreateRequest request) {
        requireAny(token, "logistics-admin", "business-admin", "super-admin");
        WarehouseResponse current = warehouses.getOrDefault(warehouseId, defaultWarehouse());
        WarehouseCreateRequest update = request == null ? new WarehouseCreateRequest(null, null, null, null, null, null, null) : request;
        WarehouseResponse updated = new WarehouseResponse(
                warehouseId,
                valueOrDefault(update.warehouseCode(), current.warehouseCode()),
                valueOrDefault(update.name(), current.name()),
                valueOrDefault(update.warehouseType(), current.warehouseType()),
                valueOrDefault(update.regionCode(), current.regionCode()),
                valueOrDefault(update.sourceSystem(), current.sourceSystem()),
                valueOrDefault(update.externalWarehouseId(), current.externalWarehouseId()),
                update.salesChannels() == null ? current.salesChannels() : update.salesChannels(),
                current.status(),
                "STR_MNEMO_ADMIN_WMS_WAREHOUSE_SAVED"
        );
        warehouses.put(warehouseId, updated);
        audit("ADMIN_WMS_WAREHOUSE_UPDATED", "WAREHOUSE", warehouseId.toString(), null);
        return updated;
    }

    @Override
    public StockPage searchStocks(String token, UUID warehouseId, String sku, String catalogPeriodCode, String channelCode, String status, int page, int size) {
        requireAny(token, "logistics-admin", "warehouse-operator", "order-admin", "wms-integration-operator", "auditor", "business-admin", "super-admin");
        List<StockResponse> items = stocks.values().stream()
                .filter(stock -> warehouseId == null || warehouseId.equals(stock.warehouseId()))
                .filter(stock -> blank(sku) || stock.sku().contains(sku))
                .filter(stock -> blank(catalogPeriodCode) || catalogPeriodCode.equals(stock.catalogPeriodCode()))
                .filter(stock -> blank(channelCode) || channelCode.equals(stock.channelCode()))
                .filter(stock -> blank(status) || status.equals(stock.status()))
                .sorted(Comparator.comparing(StockResponse::sku))
                .toList();
        return new StockPage(items, page, size, items.size());
    }

    @Override
    public StockResponse changeAvailabilityRule(String token, UUID stockItemId, AvailabilityRuleRequest request) {
        requireAny(token, "logistics-admin", "business-admin", "super-admin");
        StockResponse current = stocks.getOrDefault(stockItemId, defaultStock());
        AvailabilityRuleRequest rule = request == null ? new AvailabilityRuleRequest(null, null, null, null) : request;
        StockResponse updated = new StockResponse(
                stockItemId,
                current.warehouseId(),
                current.sku(),
                current.channelCode(),
                current.catalogPeriodCode(),
                current.availableQty(),
                current.reservedQty(),
                valueOrDefault(rule.policy(), current.policy()),
                current.status(),
                "STR_MNEMO_ADMIN_WMS_AVAILABILITY_SAVED"
        );
        stocks.put(stockItemId, updated);
        audit("ADMIN_WMS_AVAILABILITY_CHANGED", "STOCK_ITEM", stockItemId.toString(), rule.reasonCode());
        return updated;
    }

    @Override
    public SupplyPage searchSupplies(String token, UUID warehouseId, String status, String search, int page, int size) {
        requireAny(token, "logistics-admin", "warehouse-operator", "auditor", "business-admin", "super-admin");
        List<SupplyResponse> items = supplies.values().stream()
                .filter(supply -> warehouseId == null || warehouseId.equals(supply.warehouseId()))
                .filter(supply -> blank(status) || status.equals(supply.status()))
                .filter(supply -> blank(search) || supply.supplyCode().contains(search))
                .toList();
        return new SupplyPage(items, page, size, items.size());
    }

    @Override
    public SupplyResponse createSupply(String token, String idempotencyKey, SupplyCreateRequest request) {
        requireAny(token, "warehouse-operator", "logistics-admin", "business-admin", "super-admin");
        validateSupply(request);
        SupplyResponse response = new SupplyResponse(
                SUPPLY_ID,
                request.supplyCode(),
                request.warehouseId() == null ? WAREHOUSE_ID : request.warehouseId(),
                "EXPECTED",
                toSupplyLines(request.lines()),
                "CORR-032-SUPPLY-" + key(idempotencyKey, "default"),
                "STR_MNEMO_ADMIN_WMS_SUPPLY_CREATED"
        );
        supplies.put(response.supplyId(), response);
        audit("ADMIN_WMS_SUPPLY_CREATED", "SUPPLY", response.supplyId().toString(), idempotencyKey);
        return response;
    }

    @Override
    public SupplyResponse acceptSupply(String token, String idempotencyKey, UUID supplyId, SupplyCreateRequest request) {
        requireAny(token, "warehouse-operator", "logistics-admin", "business-admin", "super-admin");
        String key = key(idempotencyKey, "accept-" + supplyId);
        return acceptedSupplies.computeIfAbsent(key, ignored -> {
            SupplyResponse current = supplies.getOrDefault(supplyId, defaultSupply(supplyId));
            List<SupplyLineResponse> lines = request == null ? current.lines() : toSupplyLines(request.lines());
            SupplyResponse accepted = new SupplyResponse(
                    supplyId,
                    current.supplyCode(),
                    current.warehouseId(),
                    "PARTIALLY_ACCEPTED",
                    lines,
                    "CORR-032-ACCEPT-" + key,
                    "STR_MNEMO_ADMIN_WMS_SUPPLY_ACCEPTED"
            );
            supplies.put(supplyId, accepted);
            audit("ADMIN_WMS_SUPPLY_ACCEPTED", "SUPPLY", supplyId.toString(), key);
            return accepted;
        });
    }

    @Override
    public ReservationResponse reserveStock(String token, String idempotencyKey, ReservationCreateRequest request) {
        requireAny(token, "order-admin", "business-admin", "super-admin");
        if (request == null || blank(request.sku()) || request.quantity() == null || request.quantity() <= 0) {
            throw new AdminWmsValidationException("STR_MNEMO_ADMIN_WMS_RESERVATION_INVALID", List.of("sku", "quantity"));
        }
        StockResponse stock = stocks.getOrDefault(STOCK_ITEM_ID, defaultStock());
        if (request.quantity() > stock.availableQty() - stock.reservedQty()) {
            throw new AdminWmsConflictException("STR_MNEMO_ADMIN_WMS_STOCK_NOT_ENOUGH");
        }
        ReservationResponse response = new ReservationResponse(
                RESERVATION_ID,
                request.orderId(),
                request.warehouseId() == null ? WAREHOUSE_ID : request.warehouseId(),
                request.sku(),
                request.quantity(),
                "HELD",
                "STR_MNEMO_ADMIN_WMS_RESERVATION_HELD"
        );
        reservations.put(response.reservationId(), response);
        stocks.put(stock.stockItemId(), new StockResponse(stock.stockItemId(), stock.warehouseId(), stock.sku(), stock.channelCode(), stock.catalogPeriodCode(), stock.availableQty(), stock.reservedQty() + response.quantity(), stock.policy(), stock.status(), stock.messageCode()));
        audit("ADMIN_WMS_RESERVATION_HELD", "RESERVATION", response.reservationId().toString(), idempotencyKey);
        return response;
    }

    @Override
    public ReservationResponse releaseReservation(String token, UUID reservationId) {
        requireAny(token, "order-admin", "business-admin", "super-admin");
        ReservationResponse current = reservations.getOrDefault(reservationId, new ReservationResponse(reservationId, "ORDER-032-001", WAREHOUSE_ID, "BOG-SERUM-001", 3, "HELD", "STR_MNEMO_ADMIN_WMS_RESERVATION_HELD"));
        ReservationResponse released = new ReservationResponse(current.reservationId(), current.orderId(), current.warehouseId(), current.sku(), current.quantity(), "RELEASED", "STR_MNEMO_ADMIN_WMS_RESERVATION_RELEASED");
        reservations.put(reservationId, released);
        audit("ADMIN_WMS_RESERVATION_RELEASED", "RESERVATION", reservationId.toString(), null);
        return released;
    }

    @Override
    public SyncRunResponse startSyncRun(String token, String idempotencyKey, SyncRunCreateRequest request) {
        requireAny(token, "wms-integration-operator", "logistics-admin", "business-admin", "super-admin");
        String key = key(idempotencyKey, "sync");
        return syncRuns.computeIfAbsent(key, ignored -> {
            SyncRunResponse response = new SyncRunResponse(
                    SYNC_RUN_ID,
                    request == null ? "WMS" : valueOrDefault(request.sourceSystem(), "WMS"),
                    request == null || request.warehouseId() == null ? WAREHOUSE_ID : request.warehouseId(),
                    request == null ? "STOCK_BALANCE" : valueOrDefault(request.documentType(), "STOCK_BALANCE"),
                    "STARTED",
                    "CORR-032-SYNC-" + key,
                    "STR_MNEMO_ADMIN_WMS_SYNC_STARTED"
            );
            syncMessages.put(UUID.fromString("00000000-0000-0000-0000-000000000433"), defaultQuarantineMessage());
            audit("ADMIN_WMS_SYNC_STARTED", "SYNC_RUN", response.syncRunId().toString(), key);
            return response;
        });
    }

    @Override
    public SyncMessagePage searchSyncMessages(String token, String messageStatus, String entityType, String correlationId, int page, int size) {
        requireAny(token, "wms-integration-operator", "logistics-admin", "auditor", "business-admin", "super-admin");
        List<SyncMessageResponse> items = syncMessages.values().stream()
                .filter(message -> blank(messageStatus) || messageStatus.equals(message.messageStatus()))
                .filter(message -> blank(entityType) || entityType.equals(message.entityType()))
                .filter(message -> blank(correlationId) || correlationId.equals(message.correlationId()))
                .toList();
        return new SyncMessagePage(items, page, size, items.size());
    }

    @Override
    public AuditEventPage audit(String token, String entityType, String entityId, String correlationId, int page, int size) {
        requireAny(token, "wms-integration-operator", "logistics-admin", "warehouse-operator", "order-admin", "auditor", "business-admin", "super-admin");
        List<AuditEventResponse> items = auditEvents.stream()
                .filter(event -> blank(entityType) || entityType.equals(event.entityType()))
                .filter(event -> blank(entityId) || entityId.equals(event.entityId()))
                .filter(event -> blank(correlationId) || correlationId.equals(event.correlationId()))
                .toList();
        return new AuditEventPage(items, page, size, items.size());
    }

    private void audit(String actionCode, String entityType, String entityId, String reasonCode) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), ACTOR_USER_ID, actionCode, entityType, entityId, reasonCode, reasonCode == null ? "CORR-032-AUDIT" : reasonCode, "2026-04-27T12:32:00Z"));
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminWmsAccessDeniedException("STR_MNEMO_ADMIN_WMS_ACCESS_DENIED");
    }

    private static String role(String token) {
        if (token == null) {
            return "";
        }
        String normalized = token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static void validateWarehouse(WarehouseCreateRequest request) {
        if (request == null || blank(request.warehouseCode()) || blank(request.name())) {
            throw new AdminWmsValidationException("STR_MNEMO_ADMIN_WMS_WAREHOUSE_INVALID", List.of("warehouseCode", "name"));
        }
    }

    private static void validateSupply(SupplyCreateRequest request) {
        if (request == null || blank(request.supplyCode()) || request.lines() == null || request.lines().isEmpty()) {
            throw new AdminWmsValidationException("STR_MNEMO_ADMIN_WMS_SUPPLY_INVALID", List.of("supplyCode", "lines"));
        }
    }

    private static List<SupplyLineResponse> toSupplyLines(List<SupplyLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of(new SupplyLineResponse("BOG-SERUM-001", 120, 118, 2, 0, 0, "DAMAGED_IN_TRANSIT"));
        }
        return lines.stream()
                .map(line -> new SupplyLineResponse(
                        valueOrDefault(line.sku(), "BOG-SERUM-001"),
                        numberOrZero(line.plannedQty()),
                        numberOrZero(line.acceptedQty()),
                        numberOrZero(line.damagedQty()),
                        numberOrZero(line.shortageQty()),
                        numberOrZero(line.surplusQty()),
                        valueOrDefault(line.reasonCode(), "ACCEPTED")
                ))
                .toList();
    }

    private static WarehouseResponse defaultWarehouse() {
        return new WarehouseResponse(WAREHOUSE_ID, "WH-MSK-01", "Moscow warehouse", "FULFILLMENT", "MSK", "WMS", "WMS-MSK-01", List.of("WEB", "PARTNER_OFFICE"), "ACTIVE", "STR_MNEMO_ADMIN_WMS_WAREHOUSE_SAVED");
    }

    private static StockResponse defaultStock() {
        return new StockResponse(STOCK_ITEM_ID, WAREHOUSE_ID, "BOG-SERUM-001", "WEB", "CAM-2026-05", 120, 0, "HOLD", "AVAILABLE", "STR_MNEMO_ADMIN_WMS_STOCK_READY");
    }

    private static SupplyResponse defaultSupply(UUID supplyId) {
        return new SupplyResponse(supplyId, "SUP-032-001", WAREHOUSE_ID, "EXPECTED", List.of(new SupplyLineResponse("BOG-SERUM-001", 120, 0, 0, 0, 0, "EXPECTED")), "CORR-032-SUPPLY-default", "STR_MNEMO_ADMIN_WMS_SUPPLY_CREATED");
    }

    private static SyncMessageResponse defaultQuarantineMessage() {
        return new SyncMessageResponse(UUID.fromString("00000000-0000-0000-0000-000000000432"), SYNC_RUN_ID, "WMS", "SYNC_MESSAGE", "WMS-MSG-032", "QUARANTINED", "DUPLICATE_DOCUMENT", "CORR-032-SYNC-WMS");
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String key(String value, String fallback) {
        return blank(value) ? fallback : value;
    }

    private static int numberOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static <T> T valueOrDefault(T value, T fallback) {
        if (value instanceof String stringValue && stringValue.isBlank()) {
            return fallback;
        }
        return value == null ? fallback : value;
    }

    private static List<String> listOrEmpty(List<String> value) {
        return value == null ? List.of() : value;
    }
}
