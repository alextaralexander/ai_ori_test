# OpenAPI описание feature 014 для module bonus-wallet

## Endpoint summary
- `GET /api/bonus-wallet/summary` возвращает сводку кошелька текущего пользователя.
- `GET /api/bonus-wallet/transactions` возвращает страницу операций с фильтрами по bucket, status, campaign, source и order number.
- `GET /api/bonus-wallet/transactions/{transactionId}` возвращает детали ledger operation и связанные events.
- `GET /api/bonus-wallet/limits/order/{orderNumber}` возвращает лимит применения кошелька к заказу.
- `POST /api/bonus-wallet/exports` создает metadata CSV export.
- `GET /api/bonus-wallet/finance/{targetUserId}` доступен только финансовому оператору и требует reason.
- `POST /api/bonus-wallet/finance/adjustments` создает ручную корректировку с idempotency key.

## DTOs
- `BonusWalletSummaryResponse`: wallet id, owner user id, currency, balances, recent transactions, application limit, audit flag.
- `BonusWalletBalanceResponse`: bucket, available, hold, expiring soon, currency.
- `BonusWalletTransactionResponse`: transaction id, bucket, operation type, status, signed amount, source type, source ref, order number, claim id, campaign id, expires at, public mnemonic, correlation id, created at.
- `BonusWalletTransactionDetailsResponse`: transaction plus events and linked resources.
- `BonusWalletApplyLimitResponse`: order number, available amount, max applicable amount, blocked flag, reason mnemonic.
- `BonusWalletExportResponse`: export id, status, format, rows count, message mnemonic.
- `BonusWalletManualAdjustmentRequest`: target user id, bucket, amount, reason code.

## Validation and STR_MNEMO
- `STR_MNEMO_BONUS_WALLET_ACCESS_DENIED`
- `STR_MNEMO_BONUS_WALLET_TRANSACTION_NOT_FOUND`
- `STR_MNEMO_BONUS_WALLET_LIMIT_EXCEEDED`
- `STR_MNEMO_BONUS_WALLET_EXPORT_READY`
- `STR_MNEMO_BONUS_WALLET_ADJUSTMENT_CREATED`
- `STR_MNEMO_BONUS_WALLET_EMPTY`

Backend не возвращает hardcoded пользовательский текст: frontend локализует все mnemonic через `resources_ru.ts` и `resources_en.ts`.
