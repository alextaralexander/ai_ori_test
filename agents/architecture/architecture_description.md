# Architecture description after feature 014

## Контекст
Платформа Best Ori Gin использует frontend web-shell и Spring Boot monolith. Feature #14 добавляет контур бонусного кошелька и транзакций. Основной owning backend module - `bonus-wallet`; он хранит wallet ledger, bucket balances, лимиты применения к заказу, экспорт истории и финансовые ручные корректировки.

## Компоненты
- `ProfileSettingsView` отвечает за маршруты `/profile-settings`, `/profile-settings/general`, `/profile-settings/contacts`, `/profile-settings/addresses`, `/profile-settings/documents`, `/profile-settings/security`.
- `BonusWalletView` отвечает за маршрут `/profile/transactions/:type`, сводку балансов, фильтры, список и детали транзакций.
- `Profile API` предоставляет REST endpoints `/api/profile/**`, runtime Swagger group `/v3/api-docs/profile` и `/swagger-ui/profile`.
- `Bonus Wallet API` предоставляет REST endpoints `/api/bonus-wallet/**`, runtime Swagger group `/v3/api-docs/bonus-wallet` и `/swagger-ui/bonus-wallet`.
- `Wallet Ledger Service` возвращает summary, историю, детали, export metadata и manual adjustments.
- `Wallet Limit Service` рассчитывает допустимую сумму применения кошелька к заказу без hold и expired операций.
- `ProfileReadinessService` рассчитывает готовность профиля для checkout, delivery и claim flows.
- `ProfileAuditService` фиксирует критичные изменения, support view/update и security-sensitive события.
- `OrderClaimsView` отвечает за создание претензии, историю претензий и детали кейса на маршрутах `/order/claims/claim-create`, `/order/claims/claims-history`, `/order/claims/claims-history/:claimId`.
- `Order claims API` расширяет module_order ресурсами `/api/order/claims`.
- `OrderClaimService` валидирует заказ и позиции, рассчитывает компенсацию, ведет audit trail и маршрутизирует проверку в склад, логистику или платежи.
- `i18n resources` содержат все новые пользовательские строки и mnemonic-коды для русского и английского языков, включая `STR_MNEMO_PROFILE_*` и `STR_MNEMO_BONUS_WALLET_*`.

## Связи
- `ProfileSettingsView -> Profile API`: HTTPS/JSON REST для чтения и изменения профиля.
- `BonusWalletView -> Bonus Wallet API`: HTTPS/JSON REST для summary, transaction search, details, limits and export.
- `Checkout API -> ProfileReadinessService`: in-process service call в монолите для проверки обязательных данных перед заказом.
- `Checkout API -> Wallet Limit Service`: in-process service call в монолите для лимита применения бонусного баланса.
- `Order claims API -> ProfileReadinessService`: in-process service call в монолите для проверки контакта, адреса возврата, документов и сервисных ограничений.
- `Order claims API -> Wallet Ledger Service`: in-process service call в монолите для reversal/manual adjustment при претензиях и возвратах.
- `Wallet Ledger Service -> Order history API`: in-process read model reference для связанных заказов и навигации из транзакций.
- `OrderClaimService -> Payment`: внешний HTTPS/API контракт для утвержденных возвратов.
- `OrderClaimService -> Warehouse and logistics`: внешний интеграционный контракт для проверки возврата, пересорта, недостачи или замены.
- `OrderClaimService -> S3 or MinIO attachments`: S3-compatible protocol для вложений претензий.

## Пакетная ownership-модель
Backend сохраняет `api/domain/db/impl`. Для `bonus-wallet`: DTO и API contracts в `bonuswallet/api`, domain snapshots/repository в `bonuswallet/domain`, Liquibase XML changelog в `bonuswallet/db`, runtime classes в `bonuswallet/impl/controller`, `bonuswallet/impl/service` и `bonuswallet/impl/config`. Для `profile` и `order` сохраняется прежняя ownership-модель.

## Версионная база
Новые технологии не вводятся. Реализация использует Java 25, Spring Boot 4.0.6, Maven, React latest, TypeScript latest и Ant Design latest-compatible patterns repository baseline. Backend не передает hardcoded user-facing сообщения во frontend; публичные предопределенные сообщения передаются только mnemonic-кодами `STR_MNEMO_*`.
