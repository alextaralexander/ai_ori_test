package com.bestorigin.monolith.bonuswallet.domain;

import java.util.Optional;

public interface BonusWalletRepository {

    BonusWalletSnapshot findOrCreate(String ownerUserId, String ownerRole);

    BonusWalletSnapshot save(BonusWalletSnapshot wallet);

    Optional<BonusWalletSnapshot> findByOwnerUserId(String ownerUserId);
}
