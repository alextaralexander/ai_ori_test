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
import java.util.UUID;

public interface AdminFulfillmentService {
    FulfillmentShipmentPage searchShipments(String token, String warehouseCode, String stage, String status, Boolean slaRisk, UUID pickupPointId, String correlationId);

    FulfillmentTaskResponse createTask(String token, String idempotencyKey, FulfillmentTaskRequest request);

    FulfillmentTaskResponse moveStage(String token, UUID taskId, String idempotencyKey, StageTransitionRequest request);

    FulfillmentEventResponse createException(String token, UUID taskId, String idempotencyKey, ReasonRequest request);

    DeliveryServicePage searchDeliveryServices(String token, String status, String zoneCode);

    DeliveryServiceResponse createDeliveryService(String token, DeliveryServiceRequest request);

    DeliveryServiceResponse activateDeliveryService(String token, UUID serviceId);

    DeliveryTariffResponse addTariff(String token, UUID serviceId, DeliveryTariffRequest request);

    DeliverySlaRuleResponse addSlaRule(String token, UUID serviceId, DeliverySlaRuleRequest request);

    PickupPointPage searchPickupPoints(String token, String status, String ownerUserId, String zoneCode);

    PickupPointResponse createPickupPoint(String token, PickupPointRequest request);

    PickupPointResponse activatePickupPoint(String token, UUID pickupPointId);

    PickupPointResponse temporaryClosePickupPoint(String token, UUID pickupPointId, ReasonRequest request);

    PickupShipmentResponse acceptPickupShipment(String token, String shipmentId, String idempotencyKey);

    PickupShipmentResponse deliverPickupShipment(String token, String shipmentId, String idempotencyKey, PickupDeliveryRequest request);

    PickupShipmentResponse markNotCollected(String token, String shipmentId, String idempotencyKey, ReasonRequest request);

    IntegrationEventPage searchIntegrationEvents(String token, String sourceSystem, String status, String correlationId);
}
