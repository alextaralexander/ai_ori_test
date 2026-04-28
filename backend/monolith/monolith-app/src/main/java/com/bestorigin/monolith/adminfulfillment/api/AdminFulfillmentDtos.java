package com.bestorigin.monolith.adminfulfillment.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class AdminFulfillmentDtos {

    private AdminFulfillmentDtos() {
    }

    public record AdminFulfillmentErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record FulfillmentShipmentSummary(UUID taskId, String orderId, String shipmentId, String stage, String status, String slaDeadlineAt, String deliveryMethod, UUID pickupPointId, String lastReasonMnemonic, String correlationId) {
    }

    public record FulfillmentShipmentPage(List<FulfillmentShipmentSummary> items, int page, int size, long total) {
    }

    public record FulfillmentTaskRequest(String taskCode, String orderId, String shipmentId, String warehouseCode, String zoneCode, String stage, Integer priority, String slaDeadlineAt, String correlationId) {
    }

    public record FulfillmentTaskResponse(UUID id, String taskCode, String orderId, String shipmentId, String warehouseCode, String zoneCode, String stage, String status, Integer priority, String slaDeadlineAt, String correlationId, String updatedAt, String messageCode) {
    }

    public record StageTransitionRequest(String targetStage, String scannedOrderCode, List<String> scannedSkuCodes, String reasonCode, String correlationId) {
    }

    public record ReasonRequest(String reasonCode, String internalComment, String correlationId) {
    }

    public record FulfillmentEventResponse(UUID id, String eventType, String stageFrom, String stageTo, String reasonCode, String correlationId, String occurredAt, String messageCode) {
    }

    public record DeliveryServiceRequest(String serviceCode, String displayNameKey, String integrationMode, String endpointAlias) {
    }

    public record DeliveryServiceResponse(UUID id, String serviceCode, String displayNameKey, String status, String integrationMode, String endpointAlias, int version, String messageCode) {
    }

    public record DeliveryServicePage(List<DeliveryServiceResponse> items, int page, int size, long total) {
    }

    public record DeliveryTariffRequest(String zoneCode, String deliveryMethod, String currency, BigDecimal baseAmount, String validFrom, String validTo, Integer priority) {
    }

    public record DeliveryTariffResponse(UUID id, UUID serviceId, String zoneCode, String deliveryMethod, String currency, BigDecimal baseAmount, String validFrom, String validTo, Integer priority, String messageCode) {
    }

    public record DeliverySlaRuleRequest(String zoneCode, String stage, Integer durationMinutes) {
    }

    public record DeliverySlaRuleResponse(UUID id, UUID serviceId, String zoneCode, String stage, Integer durationMinutes, String status, String messageCode) {
    }

    public record PickupPointRequest(String pickupPointCode, String ownerUserId, String addressText, BigDecimal latitude, BigDecimal longitude, Integer storageLimitDays, Integer shipmentLimit, List<String> zoneCodes) {
    }

    public record PickupPointResponse(UUID id, String pickupPointCode, String ownerUserId, String addressText, BigDecimal latitude, BigDecimal longitude, Integer storageLimitDays, Integer shipmentLimit, List<String> zoneCodes, String status, int version, String messageCode) {
    }

    public record PickupPointPage(List<PickupPointResponse> items, int page, int size, long total) {
    }

    public record PickupDeliveryRequest(String recipientCheckCode, Boolean partialDelivery, List<String> deliveredSkuCodes, String reasonCode) {
    }

    public record PickupShipmentResponse(String shipmentId, String orderId, UUID pickupPointId, String status, String storageExpiresAt, String lastReasonCode, String correlationId, String messageCode) {
    }

    public record IntegrationEventResponse(String sourceSystem, String endpointAlias, String externalId, String status, String checksum, int retryCount, String lastErrorCode, String lastErrorMessageMnemonic, String correlationId, String createdAt) {
    }

    public record IntegrationEventPage(List<IntegrationEventResponse> items, int page, int size, long total) {
    }
}
