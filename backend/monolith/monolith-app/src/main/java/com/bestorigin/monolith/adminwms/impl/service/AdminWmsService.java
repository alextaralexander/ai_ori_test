package com.bestorigin.monolith.adminwms.impl.service;

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
import java.util.UUID;

public interface AdminWmsService {

    WarehousePage searchWarehouses(String token, String status, String regionCode, String search, int page, int size);

    WarehouseResponse createWarehouse(String token, String idempotencyKey, WarehouseCreateRequest request);

    WarehouseResponse updateWarehouse(String token, UUID warehouseId, WarehouseCreateRequest request);

    StockPage searchStocks(String token, UUID warehouseId, String sku, String catalogPeriodCode, String channelCode, String status, int page, int size);

    StockResponse changeAvailabilityRule(String token, UUID stockItemId, AvailabilityRuleRequest request);

    SupplyPage searchSupplies(String token, UUID warehouseId, String status, String search, int page, int size);

    SupplyResponse createSupply(String token, String idempotencyKey, SupplyCreateRequest request);

    SupplyResponse acceptSupply(String token, String idempotencyKey, UUID supplyId, SupplyCreateRequest request);

    ReservationResponse reserveStock(String token, String idempotencyKey, ReservationCreateRequest request);

    ReservationResponse releaseReservation(String token, UUID reservationId);

    SyncRunResponse startSyncRun(String token, String idempotencyKey, SyncRunCreateRequest request);

    SyncMessagePage searchSyncMessages(String token, String messageStatus, String entityType, String correlationId, int page, int size);

    AuditEventPage audit(String token, String entityType, String entityId, String correlationId, int page, int size);
}
