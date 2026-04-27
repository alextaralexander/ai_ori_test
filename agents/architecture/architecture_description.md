# Architecture description after feature 015

## Контекст
Платформа Best Ori Gin использует frontend web-shell и Spring Boot monolith. Feature #15 добавляет контур партнерской отчетности, комиссий, выплат и документов. Основной owning backend module - `partner-reporting`; он хранит отчетные периоды партнера, строки заказов для комиссионной сверки, удержания, выплаты, документы, export jobs и finance-view сверки.

## Компоненты
- `ProfileSettingsView` отвечает за маршруты `/profile-settings`, `/profile-settings/general`, `/profile-settings/contacts`, `/profile-settings/addresses`, `/profile-settings/documents`, `/profile-settings/security`.
- `BonusWalletView` отвечает за маршрут `/profile/transactions/:type`, сводку балансов, фильтры, список и детали транзакций.
- `PartnerReportsView` отвечает за маршруты `/report/order-history` и `/report/info-reciept`, сводку комиссий, фильтры, детализацию заказов, документы, скачивание, print-view и экспорт.
- `Profile API` предоставляет REST endpoints `/api/profile/**`, runtime Swagger group `/v3/api-docs/profile` и `/swagger-ui/profile`.
- `Bonus Wallet API` предоставляет REST endpoints `/api/bonus-wallet/**`, runtime Swagger group `/v3/api-docs/bonus-wallet` и `/swagger-ui/bonus-wallet`.
- `Partner Reporting API` предоставляет REST endpoints `/api/partner-reporting/**`, runtime Swagger group `/v3/api-docs/partner-reporting` и `/swagger-ui/partner-reporting`.
- `Partner Report Service` рассчитывает summary, строки заказов, комиссии, удержания, reconciliation status и export snapshots.
- `Partner Document Service` управляет документами выплат, версиями, checksum, signed download URLs и print-view metadata.
- `Partner Finance Reconciliation Service` предоставляет finance-view сверок, публикацию и отзыв документов с audit reason.
- `Wallet Ledger Service` возвращает summary, историю, детали, export metadata и manual adjustments.
- `Wallet Limit Service` рассчитывает допустимую сумму применения кошелька к заказу без hold и expired операций.
- `ProfileReadinessService` рассчитывает готовность профиля для checkout, delivery и claim flows.
- `ProfileAuditService` фиксирует критичные изменения, support view/update и security-sensitive события.
- `OrderClaimsView` отвечает за создание претензии, историю претензий и детали кейса на маршрутах `/order/claims/claim-create`, `/order/claims/claims-history`, `/order/claims/claims-history/:claimId`.
- `Order claims API` расширяет module_order ресурсами `/api/order/claims`.
- `OrderClaimService` валидирует заказ и позиции, рассчитывает компенсацию, ведет audit trail и маршрутизирует проверку в склад, логистику или платежи.
- `i18n resources` содержат все новые пользовательские строки и mnemonic-коды для русского и английского языков, включая `STR_MNEMO_PROFILE_*`, `STR_MNEMO_BONUS_WALLET_*` и `STR_MNEMO_PARTNER_REPORT_*`.

## Связи
- `ProfileSettingsView -> Profile API`: HTTPS/JSON REST для чтения и изменения профиля.
- `BonusWalletView -> Bonus Wallet API`: HTTPS/JSON REST для summary, transaction search, details, limits and export.
- `PartnerReportsView -> Partner Reporting API`: HTTPS/JSON REST для summary, order lines, commission details, documents, download, print-view and export.
- `Checkout API -> ProfileReadinessService`: in-process service call в монолите для проверки обязательных данных перед заказом.
- `Checkout API -> Wallet Limit Service`: in-process service call в монолите для лимита применения бонусного баланса.
- `Order claims API -> ProfileReadinessService`: in-process service call в монолите для проверки контакта, адреса возврата, документов и сервисных ограничений.
- `Order claims API -> Wallet Ledger Service`: in-process service call в монолите для reversal/manual adjustment при претензиях и возвратах.
- `Wallet Ledger Service -> Order history API`: in-process read model reference для связанных заказов и навигации из транзакций.
- `Partner Report Service -> Order history API`: in-process service/read-model call для заказов структуры, статусов и сумм.
- `Partner Report Service -> Wallet Ledger Service`: in-process service call для удержаний, reversals и bonus/referral событий.
- `Partner Report Service -> Partner Document Service`: in-process service call для связи отчетного периода с документами и экспортами.
- `Partner Finance Reconciliation Service -> Partner Report Service`: in-process service call для агрегированной сверки начислено, удержано, к выплате и выплачено.
- `Partner Finance Reconciliation Service -> Payment`: внешний HTTPS/API контракт для проверки статуса выплат.
- `Partner Document Service -> S3 or MinIO attachments`: S3-compatible protocol для хранения актов, квитанций, справок, отчетов и export files.
- `OrderClaimService -> Partner Report Service`: in-process service call для отражения возвратов и претензий в комиссионной базе и партнерских показателях.
- `OrderClaimService -> Payment`: внешний HTTPS/API контракт для утвержденных возвратов.
- `OrderClaimService -> Warehouse and logistics`: внешний интеграционный контракт для проверки возврата, пересорта, недостачи или замены.
- `OrderClaimService -> S3 or MinIO attachments`: S3-compatible protocol для вложений претензий.

## Пакетная ownership-модель
Backend сохраняет `api/domain/db/impl`. Для `partner-reporting`: DTO и API contracts в `partnerreporting/api`, JPA entities и repositories в `partnerreporting/domain`, Liquibase XML changelog в `partnerreporting/db`, runtime classes в `partnerreporting/impl/controller`, `partnerreporting/impl/service`, `partnerreporting/impl/security`, `partnerreporting/impl/validator`, `partnerreporting/impl/mapper` и `partnerreporting/impl/config`. Для `bonus-wallet`, `profile` и `order` сохраняется прежняя ownership-модель.

## Версионная база
Новые технологии не вводятся. Реализация использует Java 25, Spring Boot 4.0.6, Maven, React latest, TypeScript latest, Ant Design latest-compatible patterns repository baseline и S3/MinIO latest stable для файлов документов и экспортов. Backend не передает hardcoded user-facing сообщения во frontend; публичные предопределенные сообщения передаются только mnemonic-кодами `STR_MNEMO_*`.
