# OpenAPI description. Feature #40. Module partner-benefits

## Runtime Swagger
Модуль `partner-benefits` должен иметь отдельную OpenAPI group в монолите:
- JSON: `/v3/api-docs/partner-benefits`
- Swagger UI: `/swagger-ui/partner-benefits`
- `module-key`: `partner-benefits`
- package prefix: `com.bestorigin.monolith.partnerbenefits`

Springdoc генерирует Swagger из controllers в `com.bestorigin.monolith.partnerbenefits.impl.controller`; ручные per-endpoint Swagger registries запрещены.

## Endpoint ownership
### `GET /api/partner-benefits/me/summary`
Возвращает персональную сводку программы преимуществ текущего active partner. DTO включает account status, current catalog, reward balance, cashback pending/confirmed, список benefit grants, referral link и retention offers. Backend проверяет ownership через session user + active partner.

### `POST /api/partner-benefits/me/benefits/{benefitId}/apply-preview`
Дает backend preview применимости выгоды перед cart/checkout. Frontend не применяет выгоду самостоятельно: итоговая проверка выполняется в checkout/order контуре по `benefitId`, owner, status, catalogId, expiry и compatibility constraints.

### `GET /api/partner-benefits/me/referral-link`
Возвращает активную referral link, QR payload, campaignId и expiry. Повторный запрос не ротирует код. Ротация должна быть отдельной административной или support-командой с аудитом.

### `GET /api/partner-benefits/me/referral-events`
Возвращает referral activity с маскированными контактами и статусами `INVITED`, `REGISTERED`, `FIRST_ORDER_PLACED`, `QUALIFIED`, `REJECTED`, `REWARD_GRANTED`. PII не раскрывается без отдельного permission.

### `GET /api/partner-benefits/me/rewards`
Возвращает reward shop для текущего партнера с учетом роли, региона, склада, catalogId, периода действия, лимитов и баланса. Недоступные награды возвращаются с availability mnemonic, а не пользовательским текстом.

### `POST /api/partner-benefits/me/rewards/{rewardId}/redemptions`
Создает reward redemption. Обязателен `Idempotency-Key`. Баланс списывается один раз через optimistic locking. Повторный запрос с тем же ключом возвращает существующий результат, а конфликт версии или недостаточный баланс возвращает HTTP 409.

### `GET /api/partner-benefits/support/accounts/{partnerNumber}/timeline`
Support endpoint для сотрудника поддержки партнера. Возвращает события начисления, применения, отмены, истечения, referral, redemption и ручных корректировок. Ответ маскирует PII и показывает только действия, разрешенные permission scope.

## Permission scopes
- `PARTNER_BENEFITS_VIEW`
- `PARTNER_BENEFITS_REFERRAL_VIEW`
- `PARTNER_BENEFITS_REWARD_VIEW`
- `PARTNER_BENEFITS_REWARD_REDEEM`
- `PARTNER_BENEFITS_SUPPORT_VIEW`
- `PARTNER_BENEFITS_SUPPORT_ACTION`
- `PARTNER_BENEFITS_AUDIT_VIEW`

## Mnemonic-коды
Backend не отправляет hardcoded user-facing text во frontend. Предопределенные сообщения возвращаются только кодами:
- `STR_MNEMO_PARTNER_BENEFITS_ACCESS_DENIED`
- `STR_MNEMO_PARTNER_BENEFITS_VALIDATION_FAILED`
- `STR_MNEMO_PARTNER_BENEFITS_NOT_FOUND`
- `STR_MNEMO_PARTNER_BENEFITS_EXPIRED`
- `STR_MNEMO_PARTNER_BENEFITS_ALREADY_CONSUMED`
- `STR_MNEMO_PARTNER_BENEFITS_OWNER_MISMATCH`
- `STR_MNEMO_PARTNER_BENEFITS_CATALOG_MISMATCH`
- `STR_MNEMO_PARTNER_BENEFITS_REWARD_UNAVAILABLE`
- `STR_MNEMO_PARTNER_BENEFITS_REWARD_BALANCE_INSUFFICIENT`
- `STR_MNEMO_PARTNER_BENEFITS_VERSION_CONFLICT`
- `STR_MNEMO_PARTNER_BENEFITS_REFERRAL_REJECTED`
- `STR_MNEMO_PARTNER_BENEFITS_SUPPORT_ACTION_FORBIDDEN`

Все новые mnemonic должны быть добавлены во frontend i18n dictionaries для всех поддерживаемых языков в рамках реализации.

## Интеграции
- `catalog` и `admin-catalog`: catalogId, период текущего и следующего каталога.
- `admin-pricing`: compatibility rules для offers/gifts/free delivery.
- `cart` и `order`: apply-preview и финальная проверка в checkout.
- `bonus-wallet` и `admin-bonus`: cashback, reward balance, reversal/adjustment.
- `admin-referral` и `mlm-structure`: referral attribution и partner ownership.
- `delivery` и `admin-fulfillment`: fulfillment reward redemption.
- `platform-experience`: notification, i18n, offline, analytics consent.
- `admin-platform`: audit/KPI/integration health.

## Ошибки и валидации
Все ошибки содержат `code` и `correlationId`; field-level validation возвращает `fieldErrors`. Для чужого benefit/reward/referral backend возвращает 403 или 404 без раскрытия идентификаторов. Конкурентные изменения benefit/reward/account используют optimistic locking и возвращают HTTP 409.

## Версионная база
Фича стартует 28.04.2026. Целевой baseline: Java 25, Spring Boot 4.0.6, springdoc-openapi для монолитных групп, Maven, Liquibase XML, PostgreSQL, MapStruct и Lombok на latest stable, применимый на дату старта. Если существующий monolith runtime требует совместимого fallback, причина должна быть отражена в architecture artifacts и follow-up upgrade task.
