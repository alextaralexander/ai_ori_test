package com.bestorigin.monolith.delivery.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DeliveryDtos {

    private DeliveryDtos() {
    }

    public enum DeliveryMethod {
        HOME_DELIVERY,
        COURIER_DELIVERY,
        PICKUP_POINT
    }

    public enum ShipmentStatus {
        ORDER_CONFIRMED,
        ASSEMBLY_STARTED,
        ASSEMBLY_COMPLETED,
        SHIPPED,
        IN_TRANSIT,
        ARRIVED_AT_PICKUP_POINT,
        READY_FOR_PICKUP,
        DELIVERED,
        PARTIALLY_DELIVERED,
        NOT_COLLECTED,
        RETURNED_TO_LOGISTICS,
        DELIVERY_PROBLEM
    }

    public record DeliveryErrorResponse(String code, String correlationId, List<String> details) {
    }

    public record DeliveryOptionsResponse(List<DeliveryOptionDto> options) {
    }

    public record DeliveryOptionDto(DeliveryMethod method, boolean available, BigDecimal priceAmount, String currency, Instant expectedReceiveAt, String unavailableReasonCode) {
    }

    public record PickupPointDto(UUID id, String code, String name, String addressLine, String city, String region, String contactPhone, String workSchedule, int storageLimitDays, int maxOrdersCapacity, String status) {
    }

    public record CreateShipmentRequest(UUID orderId, UUID customerId, UUID partnerId, DeliveryMethod deliveryMethod, UUID pickupPointId, String addressLine, Instant deliveryWindowStart, Instant deliveryWindowEnd) {
    }

    public record ShipmentDto(UUID id, UUID orderId, DeliveryMethod deliveryMethod, PickupPointDto pickupPoint, String addressLine, Instant expectedReceiveAt, ShipmentStatus currentStatus, String externalShipmentId, String correlationId, String messageCode) {
    }

    public record TrackingTimelineDto(ShipmentDto shipment, List<TrackingEventDto> events) {
    }

    public record TrackingEventDto(UUID id, ShipmentStatus status, String sourceSystem, String reasonCode, Instant occurredAt, String correlationId) {
    }

    public record PickupAcceptRequest(String shipmentCode, Instant occurredAt) {
    }

    public record PickupDeliverRequest(String verificationCode, Instant occurredAt) {
    }

    public record PickupPartialDeliverRequest(String verificationCode, String reasonCode, Instant occurredAt, List<PickupOperationItemDto> items) {
    }

    public record PickupOperationItemDto(UUID orderItemId, String sku, BigDecimal quantity, String itemResult) {
    }

    public record ExternalDeliveryStatusEvent(String externalShipmentId, String externalEventId, ShipmentStatus status, String sourceSystem, String reasonCode, Instant occurredAt) {
    }
}
