package com.bestorigin.monolith.partneroffice.impl.service;

import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeDeviationResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeEscalationResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeMovementResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeOrderSummaryResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyItemResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplySummaryResponse;
import com.bestorigin.monolith.partneroffice.domain.PartnerOfficeRepository;
import com.bestorigin.monolith.partneroffice.domain.PartnerOfficeSupplySnapshot;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPartnerOfficeRepository implements PartnerOfficeRepository {

    private final Map<String, PartnerOfficeSupplySnapshot> supplies = new LinkedHashMap<>();

    public InMemoryPartnerOfficeRepository() {
        PartnerOfficeSupplySnapshot supply = new PartnerOfficeSupplySnapshot(new PartnerOfficeSupplySummaryResponse(
                "BOG-SUP-018-001",
                "BOG-OFFICE-018-MSK",
                "REG-018-MSK",
                "WMS-MSK-01",
                "WMS-1C-DOC-018-001",
                "PARTIALLY_ACCEPTED",
                "2026-05-12T08:00:00Z",
                "2026-05-14T08:00:00Z",
                "2026-05-14T07:45:00Z",
                2,
                4,
                3,
                1
        ));
        supply.orders().add(new PartnerOfficeOrderSummaryResponse("BOG-ORD-018-001", "BOG-OFFICE-018-MSK", "REG-018-MSK", "BOG-016-002", "VIP-018-001", "CAT-2026-05", "BOG-SUP-018-001", "PICKUP-018-MSK-01", "READY_FOR_PICKUP", "PAID", "ASSEMBLED", "ARRIVED_AT_PICKUP", false, "5400.00", "RUB"));
        supply.orders().add(new PartnerOfficeOrderSummaryResponse("BOG-ORD-018-002", "BOG-OFFICE-018-MSK", "REG-018-MSK", "BOG-016-004", "VIP-018-002", "CAT-2026-05", "BOG-SUP-018-001", "PICKUP-018-MSK-01", "ACCEPTANCE_BLOCKED", "PAID", "ASSEMBLED", "ARRIVED_AT_PICKUP", true, "3100.00", "RUB"));
        supply.items().add(new PartnerOfficeSupplyItemResponse("SKU-018-LIP-001", "Lip care SKU snapshot", 2, 1, "BOX-018-01"));
        supply.items().add(new PartnerOfficeSupplyItemResponse("SKU-018-CREAM-002", "Cream SKU snapshot", 1, 1, "BOX-018-02"));
        supply.movements().add(new PartnerOfficeMovementResponse("SHIPPED", "WMS_1C", "WMS-1C-DOC-018-001", "2026-05-12T09:00:00Z", "wms-system"));
        supply.movements().add(new PartnerOfficeMovementResponse("ARRIVED_AT_OFFICE", "DELIVERY", "DLV-018-001", "2026-05-14T07:45:00Z", "logistics-operator"));
        supply.deviations().add(new PartnerOfficeDeviationResponse("DEV-018-001", "SHORTAGE", "SKU-018-LIP-001", 1, "SHORT_PACKED", "Detected during office acceptance", "CLM-018-001"));
        supply.escalations().add(new PartnerOfficeEscalationResponse("BOG-SUP-018-001", "SHORTAGE_SLA_CONTROL", "regional-manager-018", "2026-05-15T12:00:00Z", "OPEN"));
        supplies.put(supply.summary().supplyId(), supply);
    }

    @Override
    public Collection<PartnerOfficeSupplySnapshot> findAll() {
        return supplies.values();
    }

    @Override
    public Optional<PartnerOfficeSupplySnapshot> findBySupplyId(String supplyId) {
        return Optional.ofNullable(supplies.get(supplyId));
    }

    @Override
    public Optional<PartnerOfficeSupplySnapshot> findByOrderNumber(String orderNumber) {
        return supplies.values().stream()
                .filter(supply -> supply.orders().stream().anyMatch(order -> order.orderNumber().equals(orderNumber)))
                .findFirst();
    }

    @Override
    public void save(PartnerOfficeSupplySnapshot supply) {
        supplies.put(supply.summary().supplyId(), supply);
    }
}
