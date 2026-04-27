package com.bestorigin.monolith.partneroffice.api;

import java.util.List;
import java.util.Map;

public final class PartnerOfficeDtos {

    private PartnerOfficeDtos() {
    }

    public record PartnerOfficeOrderPageResponse(List<PartnerOfficeOrderSummaryResponse> items, int page, int size, long totalElements) {
    }

    public record PartnerOfficeOrderSummaryResponse(
            String orderNumber,
            String officeId,
            String regionId,
            String partnerPersonNumber,
            String customerId,
            String campaignId,
            String supplyId,
            String pickupPointId,
            String orderStatus,
            String paymentStatus,
            String assemblyStatus,
            String deliveryStatus,
            boolean hasDeviation,
            String grandTotalAmount,
            String currency
    ) {
    }

    public record PartnerOfficeSupplyPageResponse(List<PartnerOfficeSupplySummaryResponse> items, int page, int size, long totalElements) {
    }

    public record PartnerOfficeSupplySummaryResponse(
            String supplyId,
            String officeId,
            String regionId,
            String warehouseId,
            String externalWmsDocumentId,
            String status,
            String plannedShipmentAt,
            String plannedArrivalAt,
            String actualArrivalAt,
            int orderCount,
            int boxCount,
            int skuCount,
            int deviationCount
    ) {
    }

    public record PartnerOfficeSupplyDetailsResponse(
            PartnerOfficeSupplySummaryResponse supply,
            List<PartnerOfficeOrderSummaryResponse> orders,
            List<PartnerOfficeSupplyItemResponse> items,
            List<PartnerOfficeMovementResponse> movements,
            List<PartnerOfficeDeviationResponse> deviations,
            List<String> availableActions
    ) {
    }

    public record PartnerOfficeSupplyOrderDetailsResponse(
            PartnerOfficeOrderSummaryResponse order,
            List<PartnerOfficeSupplyItemResponse> items,
            List<PartnerOfficeMovementResponse> movements,
            List<PartnerOfficeDeviationResponse> deviations,
            Map<String, String> workflowLinks
    ) {
    }

    public record PartnerOfficeSupplyItemResponse(String sku, String productName, int expectedQuantity, int acceptedQuantity, String boxNumber) {
    }

    public record PartnerOfficeMovementResponse(String movementType, String sourceSystem, String externalReference, String occurredAt, String actorId) {
    }

    public record PartnerOfficeDeviationResponse(String deviationId, String deviationType, String sku, int quantity, String reasonCode, String comment, String claimId) {
    }

    public record PartnerOfficeSupplyTransitionRequest(String targetStatus, String reasonCode, String comment) {
    }

    public record PartnerOfficeDeviationCreateRequest(String supplyId, String deviationType, String sku, int quantity, String reasonCode, String comment) {
    }

    public record PartnerOfficeActionResponse(String messageCode, String correlationId) {
    }

    public record PartnerOfficeReportResponse(
            String officeId,
            String regionId,
            int supplyCount,
            int orderCount,
            int shortageCount,
            int damagedCount,
            String shipmentSlaPercent,
            String acceptanceSlaPercent,
            List<PartnerOfficeEscalationResponse> escalations
    ) {
    }

    public record PartnerOfficeEscalationResponse(String supplyId, String reasonCode, String ownerUserId, String dueAt, String status) {
    }

    public record PartnerOfficeErrorResponse(String code, List<PartnerOfficeValidationReasonResponse> details, Map<String, String> metadata) {
    }

    public record PartnerOfficeValidationReasonResponse(String code, String severity, String target) {
    }
}
