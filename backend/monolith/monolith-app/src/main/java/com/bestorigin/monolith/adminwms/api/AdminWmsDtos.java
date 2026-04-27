package com.bestorigin.monolith.adminwms.api;

import java.util.List;
import java.util.UUID;

public final class AdminWmsDtos {

    private AdminWmsDtos() {
    }

    public record AdminWmsErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record WarehouseCreateRequest(String warehouseCode, String name, String warehouseType, String regionCode, String sourceSystem, String externalWarehouseId, List<String> salesChannels) {
    }

    public record WarehouseResponse(UUID warehouseId, String warehouseCode, String name, String warehouseType, String regionCode, String sourceSystem, String externalWarehouseId, List<String> salesChannels, String status, String messageCode) {
    }

    public record WarehousePage(List<WarehouseResponse> items, int page, int size, long total) {
    }

    public record StockPage(List<StockResponse> items, int page, int size, long total) {
    }

    public record StockResponse(UUID stockItemId, UUID warehouseId, String sku, String channelCode, String catalogPeriodCode, int availableQty, int reservedQty, String policy, String status, String messageCode) {
    }

    public record AvailabilityRuleRequest(String policy, String reasonCode, String activeFrom, String activeTo) {
    }

    public record SupplyCreateRequest(String supplyCode, UUID warehouseId, String sourceSystem, String externalDocumentId, String expectedAt, List<SupplyLineRequest> lines) {
    }

    public record SupplyLineRequest(String sku, Integer plannedQty, String externalLineId, Integer acceptedQty, Integer damagedQty, Integer shortageQty, Integer surplusQty, String reasonCode) {
    }

    public record SupplyLineResponse(String sku, int plannedQty, int acceptedQty, int damagedQty, int shortageQty, int surplusQty, String reasonCode) {
    }

    public record SupplyResponse(UUID supplyId, String supplyCode, UUID warehouseId, String status, List<SupplyLineResponse> lines, String correlationId, String messageCode) {
    }

    public record SupplyPage(List<SupplyResponse> items, int page, int size, long total) {
    }

    public record ReservationCreateRequest(String orderId, UUID warehouseId, String sku, Integer quantity) {
    }

    public record ReservationResponse(UUID reservationId, String orderId, UUID warehouseId, String sku, int quantity, String status, String messageCode) {
    }

    public record SyncRunCreateRequest(String sourceSystem, UUID warehouseId, String skuFilter, String documentType) {
    }

    public record SyncRunResponse(UUID syncRunId, String sourceSystem, UUID warehouseId, String documentType, String status, String correlationId, String messageCode) {
    }

    public record SyncMessagePage(List<SyncMessageResponse> items, int page, int size, long total) {
    }

    public record SyncMessageResponse(UUID syncMessageId, UUID syncRunId, String sourceSystem, String entityType, String entityId, String messageStatus, String quarantineReasonCode, String correlationId) {
    }

    public record AuditEventPage(List<AuditEventResponse> items, int page, int size, long total) {
    }

    public record AuditEventResponse(UUID auditEventId, UUID actorUserId, String actionCode, String entityType, String entityId, String reasonCode, String correlationId, String occurredAt) {
    }
}
