# ER описание feature 014 для module bonus-wallet

## Назначение
Модуль `bonus-wallet` владеет ledger бонусного кошелька: аккаунтом пользователя, расчетными балансами по bucket-ам, транзакциями и ручными корректировками финансового оператора.

## Таблица bonus_wallet_account
- `wallet_id uuid` - первичный ключ.
- `owner_user_id varchar` - владелец кошелька, индекс для быстрого поиска по пользователю.
- `owner_role varchar` - customer, partner или internal finance context.
- `currency_code varchar` - валюта балансов, в текущем baseline `RUB`.
- `status varchar` - ACTIVE, SUSPENDED.
- `created_at`, `updated_at timestamp` - технические даты.

## Таблица bonus_wallet_balance
- `balance_id uuid` - первичный ключ.
- `wallet_id uuid` - внешний ключ на `bonus_wallet_account`.
- `bucket varchar` - CASHBACK, REFERRAL_DISCOUNT, MANUAL_ADJUSTMENT, ORDER_REDEMPTION.
- `available_amount numeric(19,2)` - доступно к применению.
- `hold_amount numeric(19,2)` - удержано под заказом или проверкой.
- `expiring_soon_amount numeric(19,2)` - истекает в ближайшие 14 дней.
- `currency_code varchar` - валюта bucket.
- Уникальный индекс: `(wallet_id, bucket)`.

## Таблица bonus_wallet_transaction
- `transaction_id varchar` - бизнес-идентификатор ledger operation.
- `wallet_id uuid` - внешний ключ на кошелек.
- `bucket varchar` - корзина выгоды.
- `operation_type varchar` - ACCRUAL, HOLD, REDEMPTION, REVERSAL, EXPIRE, MANUAL_ADJUSTMENT.
- `status varchar` - ACTIVE, HOLD, REDEEMED, REVERSED, EXPIRED.
- `amount numeric(19,2)` - signed amount.
- `currency_code varchar` - валюта операции.
- `source_type varchar` - ORDER, CLAIM, REFERRAL, QUALIFICATION, FINANCE_MANUAL.
- `source_ref varchar` - внешний reference источника.
- `order_number varchar`, `claim_id varchar`, `campaign_id varchar` - связи с заказом, претензией и кампанией.
- `expires_at date` - срок действия выгоды.
- `public_mnemo varchar` - predefined frontend message code с префиксом `STR_MNEMO_`.
- `correlation_id varchar` - трассировка интеграционного события.
- `created_at timestamp` - дата операции.
- Индексы: `(wallet_id, created_at)`, `(wallet_id, bucket)`, `(order_number)`, `(campaign_id)`.

## Таблица bonus_wallet_adjustment
- `adjustment_id uuid` - первичный ключ.
- `transaction_id varchar` - связанная manual adjustment transaction.
- `target_user_id varchar` - пользователь, чей кошелек корректируется.
- `reason_code varchar` - причина финансовой корректировки.
- `actor_user_id varchar` - финансовый оператор.
- `idempotency_key varchar` - уникальный ключ защиты от дублей.
- `audit_recorded boolean` - признак записи audit trail.

## Package ownership
- `api` содержит DTO и enum contracts.
- `domain` содержит snapshot и repository interface.
- `db` содержит XML changelog `feature_014_bonus_wallet.xml`.
- `impl/controller`, `impl/service`, `impl/config` содержат runtime-код.

## Version baseline
Java 25, Spring Boot 4.0.6, Maven, XML Liquibase changeset, PostgreSQL-compatible numeric/date types.
