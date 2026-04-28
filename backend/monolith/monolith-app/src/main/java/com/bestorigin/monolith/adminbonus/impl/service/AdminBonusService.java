package com.bestorigin.monolith.adminbonus.impl.service;

import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.AccrualPage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusPreviewRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusPreviewResult;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRulePage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRuleRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRuleResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.CalculationRunRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.CalculationRunResult;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.IntegrationEventPage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.PayoutBatchRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.PayoutBatchResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.QualificationRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.QualificationResponse;
import java.util.UUID;

public interface AdminBonusService {
    BonusRulePage searchRules(String token, String status, String ruleType);

    BonusRuleResponse createRule(String token, String idempotencyKey, BonusRuleRequest request);

    BonusPreviewResult previewRule(String token, UUID ruleId, BonusPreviewRequest request);

    BonusRuleResponse activateRule(String token, UUID ruleId);

    QualificationResponse createQualification(String token, QualificationRequest request);

    CalculationRunResult runCalculation(String token, String idempotencyKey, CalculationRunRequest request);

    AccrualPage searchAccruals(String token, String periodCode, String partnerId, String status);

    PayoutBatchResponse createPayoutBatch(String token, String idempotencyKey, PayoutBatchRequest request);

    PayoutBatchResponse approvePayoutBatch(String token, UUID batchId);

    PayoutBatchResponse sendPayoutBatch(String token, UUID batchId, String idempotencyKey);

    IntegrationEventPage integrationEvents(String token, String correlationId);
}
