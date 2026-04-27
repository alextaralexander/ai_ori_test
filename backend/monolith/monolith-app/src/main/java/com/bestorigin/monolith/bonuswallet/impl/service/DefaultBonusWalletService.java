package com.bestorigin.monolith.bonuswallet.impl.service;

import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusBucket;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusOperationType;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusTransactionStatus;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletApplyLimitResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletBalanceResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletEventResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletExportRequest;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletExportResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletManualAdjustmentRequest;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletSummaryResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionDetailsResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionPageResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionResponse;
import com.bestorigin.monolith.bonuswallet.domain.BonusWalletRepository;
import com.bestorigin.monolith.bonuswallet.domain.BonusWalletSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultBonusWalletService implements BonusWalletService {

    private static final String ACCESS_DENIED = "STR_MNEMO_BONUS_WALLET_ACCESS_DENIED";
    private static final String NOT_FOUND = "STR_MNEMO_BONUS_WALLET_TRANSACTION_NOT_FOUND";
    private static final String EXPORT_READY = "STR_MNEMO_BONUS_WALLET_EXPORT_READY";
    private static final String ADJUSTMENT_CREATED = "STR_MNEMO_BONUS_WALLET_ADJUSTMENT_CREATED";
    private final BonusWalletRepository repository;
    private final ConcurrentMap<String, BonusWalletTransactionDetailsResponse> adjustmentByIdempotencyKey = new ConcurrentHashMap<>();

    public DefaultBonusWalletService(BonusWalletRepository repository) {
        this.repository = repository;
    }

    @Override
    public BonusWalletSummaryResponse summary(String userContextId, String type) {
        BonusWalletSnapshot wallet = wallet(userContextId);
        List<BonusWalletTransactionResponse> recent = filter(wallet, type, null, null, null, null).stream().limit(5).toList();
        return new BonusWalletSummaryResponse(wallet.walletId(), wallet.ownerUserId(), wallet.currencyCode(), List.copyOf(wallet.balances()), recent, orderLimit(userContextId, "ORD-011-MAIN"), false);
    }

    @Override
    public BonusWalletTransactionPageResponse transactions(String userContextId, String type, String status, String campaignId, String sourceType, String orderNumber, int page, int size) {
        BonusWalletSnapshot wallet = wallet(userContextId);
        List<BonusWalletTransactionResponse> filtered = filter(wallet, type, status, campaignId, sourceType, orderNumber);
        return new BonusWalletTransactionPageResponse(filtered, Math.max(page, 0), Math.max(size, 1), filtered.size(), false);
    }

    @Override
    public BonusWalletTransactionDetailsResponse details(String userContextId, String transactionId) {
        BonusWalletSnapshot wallet = wallet(userContextId);
        BonusWalletTransactionResponse transaction = wallet.transactions().stream()
                .filter(item -> item.transactionId().equals(transactionId))
                .findFirst()
                .orElseThrow(() -> new BonusWalletNotFoundException(NOT_FOUND));
        return details(transaction);
    }

    @Override
    public BonusWalletApplyLimitResponse orderLimit(String userContextId, String orderNumber) {
        BonusWalletSnapshot wallet = wallet(userContextId);
        BigDecimal available = wallet.balances().stream()
                .map(BonusWalletBalanceResponse::availableAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal max = available.min(money("500.00")).setScale(2, RoundingMode.HALF_UP);
        return new BonusWalletApplyLimitResponse(orderNumber, available, max, false, null);
    }

    @Override
    public BonusWalletExportResponse exportHistory(String userContextId, BonusWalletExportRequest request) {
        BonusWalletSnapshot wallet = wallet(userContextId);
        String campaign = request == null || blank(request.campaignId()) ? "CMP-2026-05" : request.campaignId();
        long rows = wallet.transactions().stream().filter(item -> campaign.equals(item.campaignId())).count();
        return new BonusWalletExportResponse("EXP-014-" + Math.abs((wallet.ownerUserId() + campaign).hashCode()), "READY", "CSV", (int) rows, EXPORT_READY);
    }

    @Override
    public BonusWalletSummaryResponse financeSummary(String userContextId, String targetUserId, String reason) {
        if (!"finance".equals(role(userContextId)) || blank(reason)) {
            throw new BonusWalletAccessDeniedException(ACCESS_DENIED);
        }
        BonusWalletSnapshot wallet = repository.findOrCreate(targetUserId, "customer");
        return new BonusWalletSummaryResponse(wallet.walletId(), wallet.ownerUserId(), wallet.currencyCode(), List.copyOf(wallet.balances()), List.copyOf(wallet.transactions()), orderLimit(wallet.ownerUserId(), "ORD-011-MAIN"), true);
    }

    @Override
    public BonusWalletTransactionDetailsResponse manualAdjustment(String userContextId, BonusWalletManualAdjustmentRequest request, String idempotencyKey) {
        if (!"finance".equals(role(userContextId))) {
            throw new BonusWalletAccessDeniedException(ACCESS_DENIED);
        }
        if (request == null || blank(request.targetUserId()) || request.amount() == null || request.bucket() == null) {
            throw new BonusWalletValidationException("STR_MNEMO_BONUS_WALLET_ADJUSTMENT_INVALID", 400);
        }
        String key = blank(idempotencyKey) ? userContextId + "-" + request.targetUserId() + "-" + request.reasonCode() : idempotencyKey;
        BonusWalletTransactionDetailsResponse existing = adjustmentByIdempotencyKey.get(key);
        if (existing != null) {
            return existing;
        }
        BonusWalletSnapshot wallet = repository.findOrCreate(request.targetUserId(), "customer");
        BonusWalletTransactionResponse transaction = new BonusWalletTransactionResponse(
                "TXN-014-MANUAL-" + key,
                request.bucket(),
                BonusOperationType.MANUAL_ADJUSTMENT,
                BonusTransactionStatus.ACTIVE,
                request.amount().setScale(2, RoundingMode.HALF_UP),
                wallet.currencyCode(),
                "FINANCE_MANUAL",
                request.reasonCode(),
                null,
                null,
                "CMP-2026-05",
                null,
                ADJUSTMENT_CREATED,
                "CORR-014-MANUAL-" + Math.abs(key.hashCode()),
                "2026-04-27T06:00:00Z"
        );
        wallet.transactions().add(0, transaction);
        repository.save(wallet);
        BonusWalletTransactionDetailsResponse details = details(transaction);
        adjustmentByIdempotencyKey.put(key, details);
        return details;
    }

    private BonusWalletSnapshot wallet(String userContextId) {
        return repository.findOrCreate(owner(userContextId), role(userContextId));
    }

    private static List<BonusWalletTransactionResponse> filter(BonusWalletSnapshot wallet, String type, String status, String campaignId, String sourceType, String orderNumber) {
        String normalizedType = normalizeType(type);
        return wallet.transactions().stream()
                .filter(item -> normalizedType == null || item.bucket().name().equals(normalizedType))
                .filter(item -> blank(status) || item.status().name().equalsIgnoreCase(status))
                .filter(item -> blank(campaignId) || campaignId.equals(item.campaignId()))
                .filter(item -> blank(sourceType) || sourceType.equalsIgnoreCase(item.sourceType()))
                .filter(item -> blank(orderNumber) || orderNumber.equals(item.orderNumber()))
                .toList();
    }

    private static String normalizeType(String type) {
        if (blank(type) || "all".equalsIgnoreCase(type)) {
            return null;
        }
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "cashback" -> BonusBucket.CASHBACK.name();
            case "referral" -> BonusBucket.REFERRAL_DISCOUNT.name();
            case "manual" -> BonusBucket.MANUAL_ADJUSTMENT.name();
            case "redemption" -> BonusBucket.ORDER_REDEMPTION.name();
            default -> type.toUpperCase(Locale.ROOT);
        };
    }

    private static BonusWalletTransactionDetailsResponse details(BonusWalletTransactionResponse transaction) {
        String linkedOrderUrl = blank(transaction.orderNumber()) ? null : "/order/order-history/" + transaction.orderNumber();
        String linkedClaimUrl = blank(transaction.claimId()) ? null : "/order/claims/claims-history/" + transaction.claimId();
        List<BonusWalletEventResponse> events = new ArrayList<>();
        events.add(new BonusWalletEventResponse(transaction.operationType().name() + "_CREATED", transaction.status().name(), transaction.sourceType(), transaction.publicMnemo(), transaction.createdAt()));
        events.add(new BonusWalletEventResponse("BALANCE_RECALCULATED", "POSTED", "bonus-wallet", "STR_MNEMO_BONUS_WALLET_BALANCE_RECALCULATED", "2026-04-27T06:00:10Z"));
        if (transaction.operationType() == BonusOperationType.MANUAL_ADJUSTMENT) {
            events.add(new BonusWalletEventResponse("AUDIT_RECORDED", "RECORDED", "finance", ADJUSTMENT_CREATED, "2026-04-27T06:00:20Z"));
        }
        return new BonusWalletTransactionDetailsResponse(transaction, linkedOrderUrl, linkedClaimUrl, List.copyOf(events), transaction.operationType() == BonusOperationType.MANUAL_ADJUSTMENT);
    }

    private static String owner(String userContextId) {
        String role = role(userContextId);
        if ("partner".equals(role)) {
            return "partner-014";
        }
        if ("finance".equals(role)) {
            return "finance-014";
        }
        return "customer-014";
    }

    private static String role(String userContextId) {
        String value = userContextId == null ? "" : userContextId.toLowerCase(Locale.ROOT);
        if (value.contains("finance")) {
            return "finance";
        }
        if (value.contains("partner")) {
            return "partner";
        }
        return "customer";
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }
}
