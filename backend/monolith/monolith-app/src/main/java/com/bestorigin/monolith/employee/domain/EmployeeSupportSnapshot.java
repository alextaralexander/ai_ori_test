package com.bestorigin.monolith.employee.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EmployeeSupportSnapshot(
        UUID sessionId,
        String actorUserId,
        String targetCustomerId,
        String targetPartnerPersonNumber,
        String supportReasonCode,
        String sourceChannel,
        String orderNumber,
        UUID operatorOrderId,
        String actionType,
        String reasonCode,
        BigDecimal amount,
        boolean supervisorRequired,
        Instant occurredAt
) {
}
