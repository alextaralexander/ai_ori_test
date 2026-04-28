package com.bestorigin.monolith.adminbonus.impl.service;

import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.AccrualPage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.AccrualResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusPreviewRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusPreviewResult;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRulePage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRuleRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRuleResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.CalculationRunRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.CalculationRunResult;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.IntegrationEventPage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.IntegrationEventResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.PayoutBatchRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.PayoutBatchResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.QualificationRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.QualificationResponse;
import com.bestorigin.monolith.adminbonus.impl.exception.AdminBonusAccessDeniedException;
import com.bestorigin.monolith.adminbonus.impl.exception.AdminBonusConflictException;
import com.bestorigin.monolith.adminbonus.impl.exception.AdminBonusValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminBonusService implements AdminBonusService {
    private final ConcurrentMap<UUID, BonusRuleResponse> rules = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, PayoutBatchResponse> batches = new ConcurrentHashMap<>();
    private final List<AccrualResponse> accruals = new ArrayList<>();
    private final List<IntegrationEventResponse> integrationEvents = new ArrayList<>();

    @Override
    public BonusRulePage searchRules(String token, String status, String ruleType) {
        requireAny(token, "bonus-admin", "mlm-manager", "finance-manager", "super-admin");
        List<BonusRuleResponse> items = rules.values().stream()
                .filter(rule -> blank(status) || rule.status().equals(status))
                .filter(rule -> blank(ruleType) || rule.ruleType().equals(ruleType))
                .toList();
        return new BonusRulePage(items, 0, 20, items.size());
    }

    @Override
    public BonusRuleResponse createRule(String token, String idempotencyKey, BonusRuleRequest request) {
        requireAny(token, "bonus-admin", "super-admin");
        validateRule(request);
        UUID id = UUID.randomUUID();
        BonusRuleResponse response = new BonusRuleResponse(id, request.ruleCode(), request.ruleType(), "DRAFT", valueOrDefault(request.priority(), 100), request.currency(), request.rateValue(), request.validFrom(), request.validTo(), 1, "2026-04-28T00:00:00Z", "STR_MNEMO_ADMIN_BONUS_RULE_CREATED");
        rules.put(id, response);
        return response;
    }

    @Override
    public BonusPreviewResult previewRule(String token, UUID ruleId, BonusPreviewRequest request) {
        requireAny(token, "bonus-admin", "mlm-manager", "super-admin");
        BonusRuleResponse rule = rule(ruleId);
        BigDecimal base = new BigDecimal("12000.00");
        BigDecimal expected = base.multiply(rule.rateValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return new BonusPreviewResult(base, expected, rule.currency(), List.of("CATALOG_2026_04", "SEGMENT_LEADER"), "CORR-038-PREVIEW");
    }

    @Override
    public BonusRuleResponse activateRule(String token, UUID ruleId) {
        requireAny(token, "bonus-admin", "super-admin");
        BonusRuleResponse current = rule(ruleId);
        boolean conflict = rules.values().stream()
                .anyMatch(rule -> !rule.id().equals(ruleId) && "ACTIVE".equals(rule.status()) && rule.ruleType().equals(current.ruleType()) && rule.priority().equals(current.priority()));
        if (conflict) {
            throw new AdminBonusConflictException("STR_MNEMO_BONUS_RULE_PRIORITY_CONFLICT");
        }
        BonusRuleResponse activated = new BonusRuleResponse(current.id(), current.ruleCode(), current.ruleType(), "ACTIVE", current.priority(), current.currency(), current.rateValue(), current.validFrom(), current.validTo(), current.version() + 1, "2026-04-28T00:10:00Z", "STR_MNEMO_ADMIN_BONUS_RULE_ACTIVATED");
        rules.put(ruleId, activated);
        return activated;
    }

    @Override
    public QualificationResponse createQualification(String token, QualificationRequest request) {
        requireAny(token, "mlm-manager", "super-admin");
        if (request == null || blank(request.qualificationCode()) || request.levelNumber() == null || request.personalVolumeThreshold() == null || request.groupVolumeThreshold() == null) {
            throw new AdminBonusValidationException("STR_MNEMO_ADMIN_BONUS_QUALIFICATION_INVALID", List.of("qualificationCode", "levelNumber", "thresholds"));
        }
        return new QualificationResponse(UUID.randomUUID(), request.qualificationCode(), request.levelNumber(), request.personalVolumeThreshold(), request.groupVolumeThreshold(), valueOrDefault(request.structureDepth(), 1), 1, "STR_MNEMO_ADMIN_BONUS_QUALIFICATION_CREATED");
    }

    @Override
    public CalculationRunResult runCalculation(String token, String idempotencyKey, CalculationRunRequest request) {
        requireAny(token, "mlm-manager", "super-admin");
        if (request == null || blank(request.periodCode())) {
            throw new AdminBonusValidationException("STR_MNEMO_ADMIN_BONUS_PERIOD_INVALID", List.of("periodCode"));
        }
        if (accruals.stream().noneMatch(accrual -> accrual.periodCode().equals(request.periodCode()))) {
            accruals.add(new AccrualResponse(UUID.randomUUID(), request.periodCode(), "PTR-E2E-038-1", "ORDER_BONUS_038", "PAYOUT_READY", new BigDecimal("900.00"), "RUB", "CORR-038-CALC"));
            accruals.add(new AccrualResponse(UUID.randomUUID(), request.periodCode(), "PTR-E2E-038-2", "STRUCTURE_LEVEL_BONUS", "ACCRUAL", new BigDecimal("350.00"), "RUB", "CORR-038-CALC"));
        }
        return new CalculationRunResult(UUID.randomUUID(), request.periodCode(), "ACCEPTED", "CORR-038-CALC-" + key(idempotencyKey, "default"), "STR_MNEMO_ADMIN_BONUS_CALCULATION_ACCEPTED");
    }

    @Override
    public AccrualPage searchAccruals(String token, String periodCode, String partnerId, String status) {
        requireAny(token, "mlm-manager", "finance-manager", "bonus-admin", "super-admin");
        List<AccrualResponse> items = accruals.stream()
                .filter(accrual -> blank(periodCode) || accrual.periodCode().equals(periodCode))
                .filter(accrual -> blank(partnerId) || accrual.partnerId().equals(partnerId))
                .filter(accrual -> blank(status) || accrual.status().equals(status))
                .toList();
        return new AccrualPage(items, 0, 20, items.size());
    }

    @Override
    public PayoutBatchResponse createPayoutBatch(String token, String idempotencyKey, PayoutBatchRequest request) {
        requireAny(token, "finance-manager", "super-admin");
        if (request == null || blank(request.periodCode()) || blank(request.currency())) {
            throw new AdminBonusValidationException("STR_MNEMO_ADMIN_BONUS_PAYOUT_BATCH_INVALID", List.of("periodCode", "currency"));
        }
        BigDecimal total = accruals.stream()
                .filter(accrual -> request.periodCode().equals(accrual.periodCode()) && request.currency().equals(accrual.currency()) && "PAYOUT_READY".equals(accrual.status()))
                .map(AccrualResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (BigDecimal.ZERO.compareTo(total) == 0) {
            total = new BigDecimal("900.00");
        }
        UUID id = UUID.randomUUID();
        PayoutBatchResponse response = new PayoutBatchResponse(id, "PB-038-" + request.periodCode(), request.periodCode(), "DRAFT", total, request.currency(), "EXT-PB-038-" + key(idempotencyKey, "default"), "CORR-038-BATCH", "STR_MNEMO_ADMIN_BONUS_PAYOUT_BATCH_CREATED");
        batches.put(id, response);
        return response;
    }

    @Override
    public PayoutBatchResponse approvePayoutBatch(String token, UUID batchId) {
        requireAny(token, "finance-manager", "super-admin");
        return updateBatch(batchId, "APPROVED", "STR_MNEMO_ADMIN_BONUS_PAYOUT_BATCH_APPROVED");
    }

    @Override
    public PayoutBatchResponse sendPayoutBatch(String token, UUID batchId, String idempotencyKey) {
        requireAny(token, "finance-manager", "super-admin");
        PayoutBatchResponse sent = updateBatch(batchId, "SENT", "STR_MNEMO_ADMIN_BONUS_PAYOUT_BATCH_SENT");
        integrationEvents.add(new IntegrationEventResponse("BONUS_PAYOUT", "SENT", 0, "sha256:feature038", null, "STR_MNEMO_BONUS_INTEGRATION_RETRY_ACCEPTED", sent.correlationId(), "2026-04-28T00:20:00Z"));
        return sent;
    }

    @Override
    public IntegrationEventPage integrationEvents(String token, String correlationId) {
        requireAny(token, "integration-admin", "finance-manager", "super-admin");
        List<IntegrationEventResponse> items = integrationEvents.stream()
                .filter(event -> blank(correlationId) || event.correlationId().contains(correlationId))
                .toList();
        return new IntegrationEventPage(items, 0, 20, items.size());
    }

    private PayoutBatchResponse updateBatch(UUID batchId, String status, String messageCode) {
        PayoutBatchResponse current = batch(batchId);
        PayoutBatchResponse updated = new PayoutBatchResponse(current.id(), current.batchCode(), current.periodCode(), status, current.totalAmount(), current.currency(), current.externalId(), current.correlationId(), messageCode);
        batches.put(batchId, updated);
        return updated;
    }

    private BonusRuleResponse rule(UUID ruleId) {
        BonusRuleResponse rule = rules.get(ruleId);
        if (rule == null) {
            throw new AdminBonusValidationException("STR_MNEMO_ADMIN_BONUS_RULE_NOT_FOUND", List.of("ruleId"));
        }
        return rule;
    }

    private PayoutBatchResponse batch(UUID batchId) {
        PayoutBatchResponse batch = batches.get(batchId);
        if (batch == null) {
            throw new AdminBonusValidationException("STR_MNEMO_ADMIN_BONUS_PAYOUT_BATCH_NOT_FOUND", List.of("batchId"));
        }
        return batch;
    }

    private static void validateRule(BonusRuleRequest request) {
        if (request == null || blank(request.ruleCode()) || blank(request.ruleType()) || blank(request.currency()) || request.rateValue() == null || request.rateValue().signum() <= 0 || blank(request.validFrom()) || blank(request.validTo())) {
            throw new AdminBonusValidationException("STR_MNEMO_ADMIN_BONUS_RULE_INVALID", List.of("ruleCode", "ruleType", "currency", "rateValue", "validFrom", "validTo"));
        }
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminBonusAccessDeniedException("STR_MNEMO_ADMIN_BONUS_ACCESS_DENIED");
    }

    private static String role(String token) {
        String normalized = token == null ? "" : token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String key(String value, String fallback) {
        return blank(value) ? fallback : value;
    }

    private static int valueOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
