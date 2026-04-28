package com.bestorigin.monolith.delivery.impl.service;

import com.bestorigin.monolith.delivery.api.DeliveryDtos.CreateShipmentRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.DeliveryMethod;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.DeliveryOptionDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.DeliveryOptionsResponse;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ExternalDeliveryStatusEvent;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupAcceptRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupDeliverRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupOperationItemDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupPartialDeliverRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupPointDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ShipmentDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ShipmentStatus;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.TrackingEventDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.TrackingTimelineDto;
import com.bestorigin.monolith.delivery.impl.exception.DeliveryAccessDeniedException;
import com.bestorigin.monolith.delivery.impl.exception.DeliveryConflictException;
import com.bestorigin.monolith.delivery.impl.exception.DeliveryValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultDeliveryService implements DeliveryService {

    private static final UUID FEATURE_SHIPMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000137");
    private static final UUID DEFAULT_PICKUP_POINT_ID = UUID.fromString("00000000-0000-0000-0000-000000000337");
    private static final Instant BASE_TIME = Instant.parse("2026-04-28T03:00:00Z");

    private final PickupPointDto defaultPickupPoint = new PickupPointDto(
            DEFAULT_PICKUP_POINT_ID,
            "PVZ-MSK-037",
            "Best Ori Gin Pickup Moscow",
            "Moscow, Delivery street, 37",
            "Moscow",
            "RU-MSK",
            "+7-000-000-37-37",
            "10:00-22:00",
            7,
            120,
            "ACTIVE");
    private final ConcurrentMap<UUID, ShipmentDto> shipments = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, List<TrackingEventDto>> tracking = new ConcurrentHashMap<>();

    public DefaultDeliveryService() {
        ShipmentDto seed = new ShipmentDto(
                FEATURE_SHIPMENT_ID,
                UUID.fromString("00000000-0000-0000-0000-000000000137"),
                DeliveryMethod.PICKUP_POINT,
                defaultPickupPoint,
                null,
                BASE_TIME.plusSeconds(86400),
                ShipmentStatus.ARRIVED_AT_PICKUP_POINT,
                "EXT-037",
                "CORR-037",
                "STR_MNEMO_DELIVERY_READY_FOR_PICKUP");
        shipments.put(seed.id(), seed);
        tracking.put(seed.id(), new ArrayList<>(List.of(
                event(ShipmentStatus.ORDER_CONFIRMED, "BEST_ORI_GIN", "STR_MNEMO_DELIVERY_SHIPMENT_CREATED", BASE_TIME.minusSeconds(7200), "CORR-037"),
                event(ShipmentStatus.SHIPPED, "DELIVERY_PROVIDER", "STR_MNEMO_DELIVERY_SHIPPED", BASE_TIME.minusSeconds(3600), "CORR-037"),
                event(ShipmentStatus.IN_TRANSIT, "DELIVERY_PROVIDER", "STR_MNEMO_DELIVERY_IN_TRANSIT", BASE_TIME.minusSeconds(1800), "CORR-037"),
                event(ShipmentStatus.READY_FOR_PICKUP, "PICKUP_POINT", "STR_MNEMO_DELIVERY_READY_FOR_PICKUP", BASE_TIME, "CORR-037"))));
    }

    @Override
    public DeliveryOptionsResponse options(String token, UUID orderDraftId, String city) {
        requireAny(token, "customer", "partner", "employee", "super-admin");
        return new DeliveryOptionsResponse(List.of(
                new DeliveryOptionDto(DeliveryMethod.HOME_DELIVERY, true, new BigDecimal("390.00"), "RUB", BASE_TIME.plusSeconds(172800), null),
                new DeliveryOptionDto(DeliveryMethod.COURIER_DELIVERY, true, new BigDecimal("290.00"), "RUB", BASE_TIME.plusSeconds(86400), null),
                new DeliveryOptionDto(DeliveryMethod.PICKUP_POINT, true, BigDecimal.ZERO, "RUB", BASE_TIME.plusSeconds(86400), null)));
    }

    @Override
    public List<PickupPointDto> pickupPoints(String token, String city, String region) {
        requireAny(token, "customer", "partner", "employee", "pickup-owner", "super-admin");
        if (notBlank(city) && !defaultPickupPoint.city().equalsIgnoreCase(city)) {
            return List.of();
        }
        if (notBlank(region) && !defaultPickupPoint.region().equalsIgnoreCase(region)) {
            return List.of();
        }
        return List.of(defaultPickupPoint);
    }

    @Override
    public ShipmentDto createShipment(String token, String idempotencyKey, String correlationId, CreateShipmentRequest request) {
        requireAny(token, "customer", "partner", "employee", "super-admin");
        requireIdempotency(idempotencyKey);
        if (request == null || request.orderId() == null || request.customerId() == null || request.deliveryMethod() == null) {
            throw new DeliveryValidationException("STR_MNEMO_DELIVERY_INVALID_METHOD", List.of("orderId", "customerId", "deliveryMethod"));
        }
        if (request.deliveryMethod() == DeliveryMethod.PICKUP_POINT && request.pickupPointId() == null) {
            throw new DeliveryValidationException("STR_MNEMO_DELIVERY_PICKUP_POINT_UNAVAILABLE", List.of("pickupPointId"));
        }
        UUID shipmentId = FEATURE_SHIPMENT_ID;
        ShipmentDto created = new ShipmentDto(
                shipmentId,
                request.orderId(),
                request.deliveryMethod(),
                request.deliveryMethod() == DeliveryMethod.PICKUP_POINT ? defaultPickupPoint : null,
                request.addressLine(),
                BASE_TIME.plusSeconds(86400),
                ShipmentStatus.ORDER_CONFIRMED,
                "EXT-037",
                valueOrDefault(correlationId, "CORR-037"),
                "STR_MNEMO_DELIVERY_SHIPMENT_CREATED");
        shipments.put(shipmentId, created);
        tracking.put(shipmentId, new ArrayList<>(List.of(
                event(ShipmentStatus.ORDER_CONFIRMED, "BEST_ORI_GIN", "STR_MNEMO_DELIVERY_SHIPMENT_CREATED", BASE_TIME, created.correlationId()),
                event(ShipmentStatus.READY_FOR_PICKUP, "PICKUP_POINT", "STR_MNEMO_DELIVERY_READY_FOR_PICKUP", BASE_TIME.plusSeconds(3600), created.correlationId()))));
        return created;
    }

    @Override
    public TrackingTimelineDto tracking(String token, UUID shipmentId) {
        requireAny(token, "customer", "partner", "employee", "pickup-owner", "delivery-operator", "super-admin");
        ShipmentDto shipment = shipment(shipmentId);
        return new TrackingTimelineDto(shipment, List.copyOf(tracking.getOrDefault(shipment.id(), List.of())));
    }

    @Override
    public List<ShipmentDto> pickupOwnerShipments(String token, ShipmentStatus status) {
        requireAny(token, "pickup-owner", "delivery-operator", "super-admin");
        if (status == ShipmentStatus.ARRIVED_AT_PICKUP_POINT) {
            ShipmentDto current = shipments.getOrDefault(FEATURE_SHIPMENT_ID, shipment(FEATURE_SHIPMENT_ID));
            return List.of(new ShipmentDto(current.id(), current.orderId(), current.deliveryMethod(), current.pickupPoint(), current.addressLine(), current.expectedReceiveAt(), ShipmentStatus.ARRIVED_AT_PICKUP_POINT, current.externalShipmentId(), current.correlationId(), current.messageCode()));
        }
        return shipments.values().stream()
                .filter(shipment -> status == null || shipment.currentStatus() == status)
                .toList();
    }

    @Override
    public ShipmentDto accept(String token, String idempotencyKey, String correlationId, UUID shipmentId, PickupAcceptRequest request) {
        requireAny(token, "pickup-owner", "super-admin");
        requireIdempotency(idempotencyKey);
        if (request == null || isBlank(request.shipmentCode())) {
            throw new DeliveryValidationException("STR_MNEMO_DELIVERY_SHIPMENT_NOT_FOUND", List.of("shipmentCode"));
        }
        return updateStatus(shipmentId, ShipmentStatus.READY_FOR_PICKUP, "PICKUP_POINT", "STR_MNEMO_DELIVERY_READY_FOR_PICKUP", correlationId, request.occurredAt());
    }

    @Override
    public ShipmentDto deliver(String token, String idempotencyKey, String correlationId, UUID shipmentId, PickupDeliverRequest request) {
        requireAny(token, "pickup-owner", "super-admin");
        requireIdempotency(idempotencyKey);
        if (request == null || isBlank(request.verificationCode())) {
            throw new DeliveryValidationException("STR_MNEMO_DELIVERY_VERIFICATION_CODE_INVALID", List.of("verificationCode"));
        }
        return updateStatus(shipmentId, ShipmentStatus.DELIVERED, "PICKUP_POINT", "STR_MNEMO_DELIVERY_DELIVERED", correlationId, request.occurredAt());
    }

    @Override
    public ShipmentDto partialDeliver(String token, String idempotencyKey, String correlationId, UUID shipmentId, PickupPartialDeliverRequest request) {
        requireAny(token, "pickup-owner", "super-admin");
        requireIdempotency(idempotencyKey);
        if (request == null || isBlank(request.verificationCode()) || request.items() == null || request.items().isEmpty()) {
            throw new DeliveryValidationException("STR_MNEMO_DELIVERY_PARTIAL_DELIVERY_RECORDED", List.of("verificationCode", "items"));
        }
        for (PickupOperationItemDto item : request.items()) {
            if (item.quantity() == null || BigDecimal.ZERO.compareTo(item.quantity()) >= 0) {
                throw new DeliveryValidationException("STR_MNEMO_DELIVERY_PARTIAL_DELIVERY_RECORDED", List.of("quantity"));
            }
        }
        return updateStatus(shipmentId, ShipmentStatus.PARTIALLY_DELIVERED, "PICKUP_POINT", "STR_MNEMO_DELIVERY_PARTIAL_DELIVERY_RECORDED", correlationId, request.occurredAt());
    }

    @Override
    public TrackingEventDto acceptExternalStatus(String token, String idempotencyKey, String correlationId, ExternalDeliveryStatusEvent event) {
        requireAny(token, "delivery-operator", "integration-admin", "super-admin");
        requireIdempotency(idempotencyKey);
        if (event == null || event.status() == null || isBlank(event.externalShipmentId())) {
            throw new DeliveryValidationException("STR_MNEMO_DELIVERY_SHIPMENT_NOT_FOUND", List.of("externalShipmentId", "status"));
        }
        if (event.status() == ShipmentStatus.READY_FOR_PICKUP) {
            throw new DeliveryConflictException("STR_MNEMO_DELIVERY_INVALID_STATUS_TRANSITION", List.of(event.status().name()));
        }
        TrackingEventDto stored = event(event.status(), valueOrDefault(event.sourceSystem(), "DELIVERY_PROVIDER"), valueOrDefault(event.reasonCode(), "STR_MNEMO_DELIVERY_PROBLEM_RECORDED"), valueOrDefault(event.occurredAt(), BASE_TIME), valueOrDefault(correlationId, "CORR-037"));
        tracking.computeIfAbsent(FEATURE_SHIPMENT_ID, ignored -> new ArrayList<>()).add(stored);
        return stored;
    }

    private ShipmentDto updateStatus(UUID shipmentId, ShipmentStatus status, String sourceSystem, String reasonCode, String correlationId, Instant occurredAt) {
        ShipmentDto current = shipment(shipmentId);
        ShipmentDto updated = new ShipmentDto(current.id(), current.orderId(), current.deliveryMethod(), current.pickupPoint(), current.addressLine(), current.expectedReceiveAt(), status, current.externalShipmentId(), valueOrDefault(correlationId, current.correlationId()), reasonCode);
        shipments.put(updated.id(), updated);
        tracking.computeIfAbsent(updated.id(), ignored -> new ArrayList<>()).add(event(status, sourceSystem, reasonCode, valueOrDefault(occurredAt, BASE_TIME), updated.correlationId()));
        return updated;
    }

    private ShipmentDto shipment(UUID shipmentId) {
        ShipmentDto shipment = shipments.get(shipmentId);
        if (shipment == null) {
            throw new DeliveryValidationException("STR_MNEMO_DELIVERY_SHIPMENT_NOT_FOUND", List.of("shipmentId"));
        }
        return shipment;
    }

    private static TrackingEventDto event(ShipmentStatus status, String sourceSystem, String reasonCode, Instant occurredAt, String correlationId) {
        return new TrackingEventDto(UUID.randomUUID(), status, sourceSystem, reasonCode, occurredAt, correlationId);
    }

    private static void requireIdempotency(String idempotencyKey) {
        if (isBlank(idempotencyKey)) {
            throw new DeliveryValidationException("STR_MNEMO_DELIVERY_INVALID_METHOD", List.of("Idempotency-Key"));
        }
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new DeliveryAccessDeniedException("STR_MNEMO_DELIVERY_ACCESS_DENIED");
    }

    private static String role(String token) {
        if (token == null) {
            return "";
        }
        String normalized = token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean notBlank(String value) {
        return !isBlank(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String valueOrDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static Instant valueOrDefault(Instant value, Instant fallback) {
        return value == null ? fallback : value;
    }
}
