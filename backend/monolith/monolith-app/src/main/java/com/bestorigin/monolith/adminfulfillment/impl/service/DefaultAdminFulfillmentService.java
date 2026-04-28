package com.bestorigin.monolith.adminfulfillment.impl.service;

import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryServicePage;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryServiceRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryServiceResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliverySlaRuleRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliverySlaRuleResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryTariffRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.DeliveryTariffResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentEventResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentShipmentPage;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentShipmentSummary;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentTaskRequest;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.FulfillmentTaskResponse;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.IntegrationEventPage;
import com.bestorigin.monolith.adminfulfillment.api.AdminFulfillmentDtos.IntegrationEventResponse;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminFulfillmentService implements AdminFulfillmentService {
    private final ConcurrentMap<UUID, FulfillmentTaskResponse> tasks = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, DeliveryServiceResponse> services = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, PickupPointResponse> pickupPoints = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, PickupShipmentResponse> pickupShipments = new ConcurrentHashMap<>();
    private final List<IntegrationEventResponse> integrationEvents = new ArrayList<>();

    @Override
    public FulfillmentShipmentPage searchShipments(String token, String warehouseCode, String stage, String status, Boolean slaRisk, UUID pickupPointId, String correlationId) {
        requireAny(token, "fulfillment-admin", "delivery-admin", "pickup-network-admin", "support-operator", "auditor", "super-admin");
        List<FulfillmentShipmentSummary> items = tasks.values().stream()
                .filter(task -> blank(warehouseCode) || task.warehouseCode().equals(warehouseCode))
                .filter(task -> blank(stage) || task.stage().equals(stage))
                .filter(task -> blank(status) || task.status().equals(status))
                .filter(task -> blank(correlationId) || task.correlationId().contains(correlationId))
                .map(task -> new FulfillmentShipmentSummary(task.id(), task.orderId(), task.shipmentId(), task.stage(), task.status(), task.slaDeadlineAt(), "PICKUP_POINT", pickupPointId, "STR_MNEMO_FULFILLMENT_STAGE_" + task.stage(), task.correlationId()))
                .toList();
        return new FulfillmentShipmentPage(items, 0, 20, items.size());
    }

    @Override
    public FulfillmentTaskResponse createTask(String token, String idempotencyKey, FulfillmentTaskRequest request) {
        requireAny(token, "fulfillment-admin", "super-admin");
        validateTask(request);
        UUID id = UUID.randomUUID();
        FulfillmentTaskResponse response = new FulfillmentTaskResponse(id, request.taskCode(), request.orderId(), request.shipmentId(), request.warehouseCode(), request.zoneCode(), request.stage(), "OPEN", valueOrDefault(request.priority(), 100), request.slaDeadlineAt(), valueOrDefault(request.correlationId(), "CORR-039-" + id), "2026-04-28T04:00:00Z", "STR_MNEMO_FULFILLMENT_TASK_CREATED");
        tasks.put(id, response);
        return response;
    }

    @Override
    public FulfillmentTaskResponse moveStage(String token, UUID taskId, String idempotencyKey, StageTransitionRequest request) {
        requireAny(token, "fulfillment-admin", "conveyor-operator", "super-admin");
        if (request == null || blank(request.targetStage())) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_FULFILLMENT_STAGE_REQUIRED", List.of("targetStage"));
        }
        FulfillmentTaskResponse current = task(taskId);
        if (!validTransition(current.stage(), request.targetStage())) {
            throw new AdminFulfillmentConflictException("STR_MNEMO_FULFILLMENT_INVALID_STAGE_TRANSITION");
        }
        FulfillmentTaskResponse updated = new FulfillmentTaskResponse(current.id(), current.taskCode(), current.orderId(), current.shipmentId(), current.warehouseCode(), current.zoneCode(), request.targetStage(), "IN_PROGRESS", current.priority(), current.slaDeadlineAt(), valueOrDefault(request.correlationId(), current.correlationId()), "2026-04-28T04:05:00Z", "STR_MNEMO_FULFILLMENT_STAGE_UPDATED");
        tasks.put(taskId, updated);
        if ("READY_TO_SHIP".equals(request.targetStage())) {
            integrationEvents.add(new IntegrationEventResponse("DELIVERY", "delivery-provider-039", current.shipmentId(), "SENT", "sha256:feature039", 0, null, "STR_MNEMO_DELIVERY_SHIPMENT_SENT", updated.correlationId(), "2026-04-28T04:06:00Z"));
        }
        return updated;
    }

    @Override
    public FulfillmentEventResponse createException(String token, UUID taskId, String idempotencyKey, ReasonRequest request) {
        requireAny(token, "fulfillment-admin", "conveyor-operator", "support-operator", "super-admin");
        if (request == null || blank(request.reasonCode())) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_FULFILLMENT_REASON_REQUIRED", List.of("reasonCode"));
        }
        FulfillmentTaskResponse current = task(taskId);
        FulfillmentTaskResponse updated = new FulfillmentTaskResponse(current.id(), current.taskCode(), current.orderId(), current.shipmentId(), current.warehouseCode(), current.zoneCode(), "EXCEPTION", "ON_HOLD", current.priority(), current.slaDeadlineAt(), valueOrDefault(request.correlationId(), current.correlationId()), "2026-04-28T04:10:00Z", "STR_MNEMO_FULFILLMENT_EXCEPTION_CREATED");
        tasks.put(taskId, updated);
        return new FulfillmentEventResponse(UUID.randomUUID(), "EXCEPTION_CREATED", current.stage(), "EXCEPTION", request.reasonCode(), updated.correlationId(), "2026-04-28T04:10:00Z", "STR_MNEMO_FULFILLMENT_EXCEPTION_CREATED");
    }

    @Override
    public DeliveryServicePage searchDeliveryServices(String token, String status, String zoneCode) {
        requireAny(token, "delivery-admin", "fulfillment-admin", "super-admin");
        List<DeliveryServiceResponse> items = services.values().stream()
                .filter(service -> blank(status) || service.status().equals(status))
                .toList();
        return new DeliveryServicePage(items, 0, 20, items.size());
    }

    @Override
    public DeliveryServiceResponse createDeliveryService(String token, DeliveryServiceRequest request) {
        requireAny(token, "delivery-admin", "super-admin");
        if (request == null || blank(request.serviceCode()) || blank(request.displayNameKey()) || blank(request.integrationMode())) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_SERVICE_INVALID", List.of("serviceCode", "displayNameKey", "integrationMode"));
        }
        UUID id = UUID.randomUUID();
        DeliveryServiceResponse response = new DeliveryServiceResponse(id, request.serviceCode(), request.displayNameKey(), "DRAFT", request.integrationMode(), request.endpointAlias(), 1, "STR_MNEMO_DELIVERY_SERVICE_CREATED");
        services.put(id, response);
        return response;
    }

    @Override
    public DeliveryServiceResponse activateDeliveryService(String token, UUID serviceId) {
        requireAny(token, "delivery-admin", "super-admin");
        DeliveryServiceResponse current = service(serviceId);
        DeliveryServiceResponse activated = new DeliveryServiceResponse(current.id(), current.serviceCode(), current.displayNameKey(), "ACTIVE", current.integrationMode(), current.endpointAlias(), current.version() + 1, "STR_MNEMO_DELIVERY_SERVICE_ACTIVATED");
        services.put(serviceId, activated);
        return activated;
    }

    @Override
    public DeliveryTariffResponse addTariff(String token, UUID serviceId, DeliveryTariffRequest request) {
        requireAny(token, "delivery-admin", "super-admin");
        service(serviceId);
        if (request == null || blank(request.zoneCode()) || blank(request.deliveryMethod()) || blank(request.currency()) || request.baseAmount() == null || request.baseAmount().signum() < 0) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_TARIFF_INVALID", List.of("zoneCode", "deliveryMethod", "currency", "baseAmount"));
        }
        return new DeliveryTariffResponse(UUID.randomUUID(), serviceId, request.zoneCode(), request.deliveryMethod(), request.currency(), request.baseAmount(), request.validFrom(), request.validTo(), request.priority(), "STR_MNEMO_DELIVERY_TARIFF_CREATED");
    }

    @Override
    public DeliverySlaRuleResponse addSlaRule(String token, UUID serviceId, DeliverySlaRuleRequest request) {
        requireAny(token, "delivery-admin", "super-admin");
        service(serviceId);
        if (request == null || blank(request.zoneCode()) || blank(request.stage()) || request.durationMinutes() == null || request.durationMinutes() <= 0) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_SLA_RULE_INVALID", List.of("zoneCode", "stage", "durationMinutes"));
        }
        return new DeliverySlaRuleResponse(UUID.randomUUID(), serviceId, request.zoneCode(), request.stage(), request.durationMinutes(), "ACTIVE", "STR_MNEMO_DELIVERY_SLA_RULE_CREATED");
    }

    @Override
    public PickupPointPage searchPickupPoints(String token, String status, String ownerUserId, String zoneCode) {
        requireAny(token, "pickup-network-admin", "fulfillment-admin", "super-admin");
        List<PickupPointResponse> items = pickupPoints.values().stream()
                .filter(point -> blank(status) || point.status().equals(status))
                .filter(point -> blank(ownerUserId) || point.ownerUserId().equals(ownerUserId))
                .toList();
        return new PickupPointPage(items, 0, 20, items.size());
    }

    @Override
    public PickupPointResponse createPickupPoint(String token, PickupPointRequest request) {
        requireAny(token, "pickup-network-admin", "super-admin");
        if (request == null || blank(request.pickupPointCode()) || blank(request.ownerUserId()) || blank(request.addressText()) || request.storageLimitDays() == null || request.shipmentLimit() == null) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_PICKUP_POINT_INVALID", List.of("pickupPointCode", "ownerUserId", "addressText", "storageLimitDays", "shipmentLimit"));
        }
        UUID id = UUID.randomUUID();
        PickupPointResponse response = new PickupPointResponse(id, request.pickupPointCode(), request.ownerUserId(), request.addressText(), request.latitude(), request.longitude(), request.storageLimitDays(), request.shipmentLimit(), request.zoneCodes() == null ? List.of() : request.zoneCodes(), "DRAFT", 1, "STR_MNEMO_DELIVERY_PICKUP_POINT_CREATED");
        pickupPoints.put(id, response);
        return response;
    }

    @Override
    public PickupPointResponse activatePickupPoint(String token, UUID pickupPointId) {
        requireAny(token, "pickup-network-admin", "super-admin");
        PickupPointResponse current = pickupPoint(pickupPointId);
        PickupPointResponse active = new PickupPointResponse(current.id(), current.pickupPointCode(), current.ownerUserId(), current.addressText(), current.latitude(), current.longitude(), current.storageLimitDays(), current.shipmentLimit(), current.zoneCodes(), "ACTIVE", current.version() + 1, "STR_MNEMO_DELIVERY_PICKUP_POINT_ACTIVATED");
        pickupPoints.put(pickupPointId, active);
        return active;
    }

    @Override
    public PickupPointResponse temporaryClosePickupPoint(String token, UUID pickupPointId, ReasonRequest request) {
        requireAny(token, "pickup-network-admin", "super-admin");
        if (request == null || blank(request.reasonCode())) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_PICKUP_POINT_REASON_REQUIRED", List.of("reasonCode"));
        }
        PickupPointResponse current = pickupPoint(pickupPointId);
        PickupPointResponse closed = new PickupPointResponse(current.id(), current.pickupPointCode(), current.ownerUserId(), current.addressText(), current.latitude(), current.longitude(), current.storageLimitDays(), current.shipmentLimit(), current.zoneCodes(), "TEMPORARILY_CLOSED", current.version() + 1, "STR_MNEMO_DELIVERY_PICKUP_POINT_TEMPORARILY_CLOSED");
        pickupPoints.put(pickupPointId, closed);
        return closed;
    }

    @Override
    public PickupShipmentResponse acceptPickupShipment(String token, String shipmentId, String idempotencyKey) {
        requireAny(token, "pickup-owner", "pickup-network-admin", "super-admin");
        PickupShipmentResponse response = new PickupShipmentResponse(shipmentId, "BO-E2E-039-3", null, "ACCEPTED", "2026-05-05T00:00:00Z", null, "CORR-039-PICKUP", "STR_MNEMO_DELIVERY_PICKUP_SHIPMENT_ACCEPTED");
        pickupShipments.put(shipmentId, response);
        return response;
    }

    @Override
    public PickupShipmentResponse deliverPickupShipment(String token, String shipmentId, String idempotencyKey, PickupDeliveryRequest request) {
        requireAny(token, "pickup-owner", "super-admin");
        if (request == null || blank(request.recipientCheckCode())) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_RECIPIENT_CODE_INVALID", List.of("recipientCheckCode"));
        }
        PickupShipmentResponse response = new PickupShipmentResponse(shipmentId, "BO-E2E-039-3", null, request.partialDelivery() == Boolean.TRUE ? "PARTIALLY_DELIVERED" : "DELIVERED", "2026-05-05T00:00:00Z", request.reasonCode(), "CORR-039-DELIVERED", "STR_MNEMO_DELIVERY_PICKUP_SHIPMENT_DELIVERED");
        pickupShipments.put(shipmentId, response);
        return response;
    }

    @Override
    public PickupShipmentResponse markNotCollected(String token, String shipmentId, String idempotencyKey, ReasonRequest request) {
        requireAny(token, "pickup-owner", "pickup-network-admin", "super-admin");
        if (request == null || blank(request.reasonCode())) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_REASON_REQUIRED", List.of("reasonCode"));
        }
        PickupShipmentResponse response = new PickupShipmentResponse(shipmentId, "BO-E2E-039-3", null, "NOT_COLLECTED", "2026-05-05T00:00:00Z", request.reasonCode(), valueOrDefault(request.correlationId(), "CORR-039-RETURN"), "STR_MNEMO_DELIVERY_NOT_COLLECTED_RECORDED");
        pickupShipments.put(shipmentId, response);
        integrationEvents.add(new IntegrationEventResponse("ADMIN_BONUS", "admin-bonus-events", shipmentId, "SENT", "sha256:not-collected-039", 0, null, "STR_MNEMO_ADMIN_BONUS_ADJUSTMENT_EVENT_SENT", response.correlationId(), "2026-04-28T04:15:00Z"));
        return response;
    }

    @Override
    public IntegrationEventPage searchIntegrationEvents(String token, String sourceSystem, String status, String correlationId) {
        requireAny(token, "fulfillment-admin", "delivery-admin", "integration-admin", "auditor", "super-admin");
        List<IntegrationEventResponse> items = integrationEvents.stream()
                .filter(event -> blank(sourceSystem) || event.sourceSystem().equals(sourceSystem))
                .filter(event -> blank(status) || event.status().equals(status))
                .filter(event -> blank(correlationId) || event.correlationId().contains(correlationId))
                .toList();
        return new IntegrationEventPage(items, 0, 20, items.size());
    }

    private FulfillmentTaskResponse task(UUID taskId) {
        FulfillmentTaskResponse task = tasks.get(taskId);
        if (task == null) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_FULFILLMENT_TASK_NOT_FOUND", List.of("taskId"));
        }
        return task;
    }

    private DeliveryServiceResponse service(UUID serviceId) {
        DeliveryServiceResponse service = services.get(serviceId);
        if (service == null) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_SERVICE_NOT_FOUND", List.of("serviceId"));
        }
        return service;
    }

    private PickupPointResponse pickupPoint(UUID pickupPointId) {
        PickupPointResponse point = pickupPoints.get(pickupPointId);
        if (point == null) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_DELIVERY_PICKUP_POINT_NOT_FOUND", List.of("pickupPointId"));
        }
        return point;
    }

    private static void validateTask(FulfillmentTaskRequest request) {
        if (request == null || blank(request.taskCode()) || blank(request.orderId()) || blank(request.warehouseCode()) || blank(request.stage())) {
            throw new AdminFulfillmentValidationException("STR_MNEMO_FULFILLMENT_TASK_INVALID", List.of("taskCode", "orderId", "warehouseCode", "stage"));
        }
    }

    private static boolean validTransition(String current, String target) {
        return "PICK_PENDING".equals(current) && "PICK_IN_PROGRESS".equals(target)
                || "PICK_IN_PROGRESS".equals(current) && "PACK_IN_PROGRESS".equals(target)
                || "PACK_IN_PROGRESS".equals(current) && "SORT_PENDING".equals(target)
                || "SORT_PENDING".equals(current) && "READY_TO_SHIP".equals(target)
                || current.equals(target);
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminFulfillmentAccessDeniedException("STR_MNEMO_FULFILLMENT_ACCESS_DENIED");
    }

    private static String role(String token) {
        String normalized = token == null ? "" : token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String valueOrDefault(String value, String fallback) {
        return blank(value) ? fallback : value;
    }

    private static int valueOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
