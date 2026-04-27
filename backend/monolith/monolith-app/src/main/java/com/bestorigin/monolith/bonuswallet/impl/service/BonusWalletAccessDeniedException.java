package com.bestorigin.monolith.bonuswallet.impl.service;

public class BonusWalletAccessDeniedException extends RuntimeException {

    public BonusWalletAccessDeniedException(String message) {
        super(message);
    }
}
