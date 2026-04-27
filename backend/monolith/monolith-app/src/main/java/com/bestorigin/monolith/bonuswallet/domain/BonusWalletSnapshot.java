package com.bestorigin.monolith.bonuswallet.domain;

import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletBalanceResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BonusWalletSnapshot {

    private final UUID walletId;
    private final String ownerUserId;
    private final String ownerRole;
    private final String currencyCode;
    private final List<BonusWalletBalanceResponse> balances = new ArrayList<>();
    private final List<BonusWalletTransactionResponse> transactions = new ArrayList<>();

    public BonusWalletSnapshot(UUID walletId, String ownerUserId, String ownerRole, String currencyCode) {
        this.walletId = walletId;
        this.ownerUserId = ownerUserId;
        this.ownerRole = ownerRole;
        this.currencyCode = currencyCode;
    }

    public UUID walletId() {
        return walletId;
    }

    public String ownerUserId() {
        return ownerUserId;
    }

    public String ownerRole() {
        return ownerRole;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public List<BonusWalletBalanceResponse> balances() {
        return balances;
    }

    public List<BonusWalletTransactionResponse> transactions() {
        return transactions;
    }
}
