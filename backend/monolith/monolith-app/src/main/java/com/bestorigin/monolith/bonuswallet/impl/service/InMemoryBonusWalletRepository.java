package com.bestorigin.monolith.bonuswallet.impl.service;

import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusBucket;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusOperationType;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusTransactionStatus;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletBalanceResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionResponse;
import com.bestorigin.monolith.bonuswallet.domain.BonusWalletRepository;
import com.bestorigin.monolith.bonuswallet.domain.BonusWalletSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBonusWalletRepository implements BonusWalletRepository {

    private static final String CAMPAIGN = "CMP-2026-05";
    private final ConcurrentMap<String, BonusWalletSnapshot> wallets = new ConcurrentHashMap<>();

    @Override
    public BonusWalletSnapshot findOrCreate(String ownerUserId, String ownerRole) {
        return wallets.computeIfAbsent(ownerUserId, key -> seed(ownerUserId, ownerRole));
    }

    @Override
    public BonusWalletSnapshot save(BonusWalletSnapshot wallet) {
        wallets.put(wallet.ownerUserId(), wallet);
        return wallet;
    }

    @Override
    public Optional<BonusWalletSnapshot> findByOwnerUserId(String ownerUserId) {
        return Optional.ofNullable(wallets.get(ownerUserId));
    }

    private static BonusWalletSnapshot seed(String ownerUserId, String ownerRole) {
        BonusWalletSnapshot wallet = new BonusWalletSnapshot(UUID.nameUUIDFromBytes(ownerUserId.getBytes(StandardCharsets.UTF_8)), ownerUserId, ownerRole, "RUB");
        wallet.balances().add(new BonusWalletBalanceResponse(BonusBucket.CASHBACK, money("780.00"), money("120.00"), money("120.00"), "RUB"));
        wallet.balances().add(new BonusWalletBalanceResponse(BonusBucket.REFERRAL_DISCOUNT, roleIsPartner(ownerRole) ? money("1350.00") : money("0.00"), money("0.00"), money("250.00"), "RUB"));
        wallet.balances().add(new BonusWalletBalanceResponse(BonusBucket.MANUAL_ADJUSTMENT, money("150.00"), money("0.00"), money("0.00"), "RUB"));
        wallet.balances().add(new BonusWalletBalanceResponse(BonusBucket.ORDER_REDEMPTION, money("0.00"), money("200.00"), money("0.00"), "RUB"));
        wallet.transactions().add(new BonusWalletTransactionResponse("TXN-014-CASHBACK-ACCRUAL", BonusBucket.CASHBACK, BonusOperationType.ACCRUAL, BonusTransactionStatus.ACTIVE, money("450.00"), "RUB", "ORDER", "ORD-011-MAIN", "ORD-011-MAIN", null, CAMPAIGN, "2026-05-18", "STR_MNEMO_BONUS_WALLET_ACCRUAL_ACTIVE", "CORR-014-001", "2026-04-12T09:20:00Z"));
        wallet.transactions().add(new BonusWalletTransactionResponse("TXN-014-REDEMPTION", BonusBucket.ORDER_REDEMPTION, BonusOperationType.REDEMPTION, BonusTransactionStatus.REDEEMED, money("-200.00"), "RUB", "ORDER", "ORD-011-MAIN", "ORD-011-MAIN", null, CAMPAIGN, null, "STR_MNEMO_BONUS_WALLET_REDEEMED", "CORR-014-002", "2026-04-13T11:05:00Z"));
        wallet.transactions().add(new BonusWalletTransactionResponse("TXN-014-EXPIRE", BonusBucket.CASHBACK, BonusOperationType.EXPIRE, BonusTransactionStatus.EXPIRED, money("-120.00"), "RUB", "CAMPAIGN", CAMPAIGN, null, null, CAMPAIGN, "2026-04-25", "STR_MNEMO_BONUS_WALLET_EXPIRED", "CORR-014-003", "2026-04-25T00:00:00Z"));
        if (roleIsPartner(ownerRole)) {
            wallet.transactions().add(new BonusWalletTransactionResponse("TXN-014-REFERRAL-ACCRUAL", BonusBucket.REFERRAL_DISCOUNT, BonusOperationType.ACCRUAL, BonusTransactionStatus.ACTIVE, money("650.00"), "RUB", "REFERRAL", "REF-008-MARIA", "ORD-011-SUPP", null, CAMPAIGN, "2026-06-01", "STR_MNEMO_BONUS_WALLET_REFERRAL_ACTIVE", "CORR-014-004", "2026-04-14T12:40:00Z"));
        }
        return wallet;
    }

    private static boolean roleIsPartner(String role) {
        return "partner".equals(role);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }
}
