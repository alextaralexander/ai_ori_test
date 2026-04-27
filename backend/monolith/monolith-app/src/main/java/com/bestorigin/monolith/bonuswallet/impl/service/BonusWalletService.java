package com.bestorigin.monolith.bonuswallet.impl.service;

import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletApplyLimitResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletExportRequest;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletExportResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletManualAdjustmentRequest;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletSummaryResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionDetailsResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionPageResponse;

public interface BonusWalletService {

    BonusWalletSummaryResponse summary(String userContextId, String type);

    BonusWalletTransactionPageResponse transactions(String userContextId, String type, String status, String campaignId, String sourceType, String orderNumber, int page, int size);

    BonusWalletTransactionDetailsResponse details(String userContextId, String transactionId);

    BonusWalletApplyLimitResponse orderLimit(String userContextId, String orderNumber);

    BonusWalletExportResponse exportHistory(String userContextId, BonusWalletExportRequest request);

    BonusWalletSummaryResponse financeSummary(String userContextId, String targetUserId, String reason);

    BonusWalletTransactionDetailsResponse manualAdjustment(String userContextId, BonusWalletManualAdjustmentRequest request, String idempotencyKey);
}
