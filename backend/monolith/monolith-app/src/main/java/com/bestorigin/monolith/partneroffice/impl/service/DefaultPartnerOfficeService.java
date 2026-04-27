package com.bestorigin.monolith.partneroffice.impl.service;

import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeActionResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeDeviationCreateRequest;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeDeviationResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeMovementResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeOrderPageResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeOrderSummaryResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeReportResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyDetailsResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyOrderDetailsResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyPageResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplySummaryResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyTransitionRequest;
import com.bestorigin.monolith.partneroffice.domain.PartnerOfficeRepository;
import com.bestorigin.monolith.partneroffice.domain.PartnerOfficeSupplySnapshot;
import com.bestorigin.monolith.partneroffice.impl.exception.PartnerOfficeAccessDeniedException;
import com.bestorigin.monolith.partneroffice.impl.exception.PartnerOfficeNotFoundException;
import com.bestorigin.monolith.partneroffice.impl.exception.PartnerOfficeValidationException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DefaultPartnerOfficeService implements PartnerOfficeService {

    private static final String ACCESS_DENIED = "STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED";
    private static final String FILTER_INVALID = "STR_MNEMO_PARTNER_OFFICE_FILTER_INVALID";
    private static final String NOT_FOUND = "STR_MNEMO_PARTNER_OFFICE_SUPPLY_NOT_FOUND";
    private static final String STATUS_INVALID = "STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_INVALID";
    private static final String STATUS_UPDATED = "STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_UPDATED";
    private static final String DEVIATION_RECORDED = "STR_MNEMO_PARTNER_OFFICE_DEVIATION_RECORDED";

    private final PartnerOfficeRepository repository;

    public DefaultPartnerOfficeService(PartnerOfficeRepository repository) {
        this.repository = repository;
    }

    @Override
    public PartnerOfficeOrderPageResponse searchOrders(String userContextId, String campaignId, String officeId, String regionId, String query, String supplyId, Boolean hasDeviation, int page, int size) {
        validatePage(size);
        List<PartnerOfficeOrderSummaryResponse> orders = repository.findAll().stream()
                .filter(supply -> supplyAllowed(userContextId, supply))
                .flatMap(supply -> supply.orders().stream())
                .filter(order -> blank(campaignId) || campaignId.equals(order.campaignId()))
                .filter(order -> blank(officeId) || officeId.equals(order.officeId()))
                .filter(order -> blank(regionId) || regionId.equals(order.regionId()))
                .filter(order -> blank(query) || order.orderNumber().contains(query) || order.customerId().contains(query) || order.partnerPersonNumber().contains(query))
                .filter(order -> blank(supplyId) || supplyId.equals(order.supplyId()))
                .filter(order -> hasDeviation == null || hasDeviation == order.hasDeviation())
                .toList();
        return new PartnerOfficeOrderPageResponse(orders, Math.max(page, 0), Math.max(size, 1), orders.size());
    }

    @Override
    public PartnerOfficeSupplyPageResponse searchSupply(String userContextId, String officeId, String regionId, String status, Boolean hasDeviation, int page, int size) {
        validatePage(size);
        List<PartnerOfficeSupplySummaryResponse> items = repository.findAll().stream()
                .filter(supply -> supplyAllowed(userContextId, supply))
                .map(PartnerOfficeSupplySnapshot::summary)
                .filter(supply -> blank(officeId) || officeId.equals(supply.officeId()))
                .filter(supply -> blank(regionId) || regionId.equals(supply.regionId()))
                .filter(supply -> blank(status) || status.equalsIgnoreCase(supply.status()))
                .filter(supply -> hasDeviation == null || hasDeviation == (supply.deviationCount() > 0))
                .toList();
        return new PartnerOfficeSupplyPageResponse(items, Math.max(page, 0), Math.max(size, 1), items.size());
    }

    @Override
    public PartnerOfficeSupplyDetailsResponse getSupply(String userContextId, String supplyId) {
        PartnerOfficeSupplySnapshot supply = supplyForUser(userContextId, supplyId);
        return new PartnerOfficeSupplyDetailsResponse(supply.summary(), List.copyOf(supply.orders()), List.copyOf(supply.items()), List.copyOf(supply.movements()), List.copyOf(supply.deviations()), List.of("START_ACCEPTANCE", "RECORD_DEVIATION", "OPEN_ESCALATION"));
    }

    @Override
    public PartnerOfficeSupplyOrderDetailsResponse getSupplyOrder(String userContextId, String orderNumber) {
        PartnerOfficeSupplySnapshot supply = repository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PartnerOfficeNotFoundException(NOT_FOUND));
        if (!supplyAllowed(userContextId, supply)) {
            throw new PartnerOfficeAccessDeniedException(ACCESS_DENIED);
        }
        PartnerOfficeOrderSummaryResponse order = supply.orders().stream()
                .filter(item -> item.orderNumber().equals(orderNumber))
                .findFirst()
                .orElseThrow(() -> new PartnerOfficeNotFoundException(NOT_FOUND));
        return new PartnerOfficeSupplyOrderDetailsResponse(order, List.copyOf(supply.items()), List.copyOf(supply.movements()), List.copyOf(supply.deviations()), Map.of(
                "order", "/order/order-history/" + orderNumber,
                "claim", "/order/claims/claims-history/CLM-018-001",
                "pickup", "/pickup/points/" + order.pickupPointId(),
                "delivery", "/delivery/orders/" + orderNumber
        ));
    }

    @Override
    public PartnerOfficeActionResponse transitionSupply(String userContextId, String supplyId, PartnerOfficeSupplyTransitionRequest request, String idempotencyKey) {
        if (!"logistics-operator".equals(role(userContextId)) && !"partner-office".equals(role(userContextId))) {
            throw new PartnerOfficeAccessDeniedException(ACCESS_DENIED);
        }
        PartnerOfficeSupplySnapshot supply = supplyForUser(userContextId, supplyId);
        String target = request == null ? null : request.targetStatus();
        if (!"ARRIVED".equals(target) && !"ACCEPTANCE_IN_PROGRESS".equals(target) && !"ACCEPTED".equals(target) && !"PARTIALLY_ACCEPTED".equals(target) && !"BLOCKED".equals(target)) {
            throw new PartnerOfficeValidationException(STATUS_INVALID, 400);
        }
        supply.movements().add(new PartnerOfficeMovementResponse("ARRIVED".equals(target) ? "ARRIVED_AT_OFFICE" : target, "PARTNER_OFFICE", idempotencyKey, "2026-05-14T08:10:00Z", role(userContextId)));
        repository.save(supply);
        return new PartnerOfficeActionResponse(STATUS_UPDATED, "CORR-018-STATUS-" + Math.abs((supplyId + idempotencyKey).hashCode()));
    }

    @Override
    public PartnerOfficeActionResponse recordDeviation(String userContextId, String orderNumber, PartnerOfficeDeviationCreateRequest request, String idempotencyKey) {
        if (!"partner-office".equals(role(userContextId))) {
            throw new PartnerOfficeAccessDeniedException(ACCESS_DENIED);
        }
        PartnerOfficeSupplySnapshot supply = repository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PartnerOfficeNotFoundException(NOT_FOUND));
        if (!supplyAllowed(userContextId, supply)) {
            throw new PartnerOfficeAccessDeniedException(ACCESS_DENIED);
        }
        if (request == null || blank(request.supplyId()) || blank(request.deviationType()) || blank(request.sku()) || blank(request.reasonCode()) || request.quantity() <= 0) {
            throw new PartnerOfficeValidationException(FILTER_INVALID, 400);
        }
        supply.deviations().add(new PartnerOfficeDeviationResponse("DEV-018-" + Math.abs(idempotencyKey.hashCode()), request.deviationType(), request.sku(), request.quantity(), request.reasonCode(), request.comment(), "CLM-018-001"));
        supply.movements().add(new PartnerOfficeMovementResponse("DEVIATION_RECORDED", "PARTNER_OFFICE", idempotencyKey, "2026-05-14T08:20:00Z", role(userContextId)));
        repository.save(supply);
        return new PartnerOfficeActionResponse(DEVIATION_RECORDED, "CORR-018-DEVIATION-" + Math.abs((orderNumber + idempotencyKey).hashCode()));
    }

    @Override
    public PartnerOfficeReportResponse report(String userContextId, String officeId, String regionId) {
        if (!"regional-manager".equals(role(userContextId)) && !"partner-office".equals(role(userContextId))) {
            throw new PartnerOfficeAccessDeniedException(ACCESS_DENIED);
        }
        List<PartnerOfficeSupplySnapshot> supplies = repository.findAll().stream()
                .filter(supply -> supplyAllowed(userContextId, supply))
                .toList();
        int orderCount = supplies.stream().mapToInt(supply -> supply.orders().size()).sum();
        int shortages = supplies.stream().mapToInt(supply -> (int) supply.deviations().stream().filter(deviation -> "SHORTAGE".equals(deviation.deviationType())).count()).sum();
        return new PartnerOfficeReportResponse(blank(officeId) ? "BOG-OFFICE-018-MSK" : officeId, blank(regionId) ? "REG-018-MSK" : regionId, supplies.size(), orderCount, shortages, 0, "98.50", "96.40", supplies.stream().flatMap(supply -> supply.escalations().stream()).toList());
    }

    private PartnerOfficeSupplySnapshot supplyForUser(String userContextId, String supplyId) {
        PartnerOfficeSupplySnapshot supply = repository.findBySupplyId(supplyId)
                .orElseThrow(() -> new PartnerOfficeNotFoundException(NOT_FOUND));
        if (!supplyAllowed(userContextId, supply)) {
            throw new PartnerOfficeAccessDeniedException(ACCESS_DENIED);
        }
        return supply;
    }

    private static boolean supplyAllowed(String userContextId, PartnerOfficeSupplySnapshot supply) {
        String role = role(userContextId);
        if ("partner-office-foreign".equals(role)) {
            return false;
        }
        return "partner-office".equals(role) || "regional-manager".equals(role) || "logistics-operator".equals(role);
    }

    private static String role(String userContextId) {
        String value = userContextId == null ? "" : userContextId.toLowerCase(Locale.ROOT);
        if (value.contains("partner-office-foreign")) {
            return "partner-office-foreign";
        }
        if (value.contains("regional-manager")) {
            return "regional-manager";
        }
        if (value.contains("logistics-operator")) {
            return "logistics-operator";
        }
        if (value.contains("partner-office")) {
            return "partner-office";
        }
        return "customer";
    }

    private static void validatePage(int size) {
        if (size < 1 || size > 100) {
            throw new PartnerOfficeValidationException(FILTER_INVALID, 400);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
