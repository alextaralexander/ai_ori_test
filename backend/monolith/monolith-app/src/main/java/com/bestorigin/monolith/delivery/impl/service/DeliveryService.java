package com.bestorigin.monolith.delivery.impl.service;

import com.bestorigin.monolith.delivery.api.DeliveryDtos.CreateShipmentRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.DeliveryOptionsResponse;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ExternalDeliveryStatusEvent;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupAcceptRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupDeliverRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupPartialDeliverRequest;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.PickupPointDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ShipmentDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.ShipmentStatus;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.TrackingEventDto;
import com.bestorigin.monolith.delivery.api.DeliveryDtos.TrackingTimelineDto;
import java.util.List;
import java.util.UUID;

public interface DeliveryService {

    DeliveryOptionsResponse options(String token, UUID orderDraftId, String city);

    List<PickupPointDto> pickupPoints(String token, String city, String region);

    ShipmentDto createShipment(String token, String idempotencyKey, String correlationId, CreateShipmentRequest request);

    TrackingTimelineDto tracking(String token, UUID shipmentId);

    List<ShipmentDto> pickupOwnerShipments(String token, ShipmentStatus status);

    ShipmentDto accept(String token, String idempotencyKey, String correlationId, UUID shipmentId, PickupAcceptRequest request);

    ShipmentDto deliver(String token, String idempotencyKey, String correlationId, UUID shipmentId, PickupDeliverRequest request);

    ShipmentDto partialDeliver(String token, String idempotencyKey, String correlationId, UUID shipmentId, PickupPartialDeliverRequest request);

    TrackingEventDto acceptExternalStatus(String token, String idempotencyKey, String correlationId, ExternalDeliveryStatusEvent event);
}
