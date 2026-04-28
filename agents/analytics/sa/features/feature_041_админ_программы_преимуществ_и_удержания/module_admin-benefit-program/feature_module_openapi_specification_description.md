# OpenAPI-описание feature #41 для модуля admin-benefit-program

## Назначение API
API модуля `admin-benefit-program` обслуживает административный workspace `/admin/benefit-programs`. Runtime Swagger/OpenAPI должен генерироваться автоматически через springdoc-openapi для module key `admin-benefit-program`:
- OpenAPI JSON: `/v3/api-docs/admin-benefit-program`
- Swagger UI: `/swagger-ui/admin-benefit-program`

Контроллеры должны находиться под package prefix `com.bestorigin.monolith.adminbenefitprogram` и внутри `impl/controller`. Ручная регистрация endpoint-ов в Swagger не допускается.

## Endpoint group `AdminBenefitPrograms`

### `GET /api/admin/benefit-programs/programs`
Поиск программ преимуществ для реестра админки. Поддерживает фильтры `code`, `type`, `status`, `catalogId`, `ownerRole`, `page`, `size`.

Права:
- `ADMIN_BENEFIT_PROGRAM_VIEW`
- `ADMIN_BENEFIT_PROGRAM_MANAGE`
- `ADMIN_BENEFIT_PROGRAM_AUDIT_VIEW`

Ответ `200` возвращает `BenefitProgramPage` без пользовательских hardcoded сообщений.

### `POST /api/admin/benefit-programs/programs`
Создает программу в статусе `DRAFT` и первую версию правил.

DTO `BenefitProgramCreateRequest` содержит:
- `code`, `type`, `catalogId`, `activeFrom`, `activeTo`, `ownerRole`;
- `rules`: типоспецифичные настройки cashback, referral discount, welcome, reward shop, free shipping, reservation, subscription или retention;
- `eligibility`: роли, уровни, регионы, ББ, суммы заказов, referral source, channels, risk score;
- `compatibility`: priority, stackability, mutual exclusion, max benefit rules;
- `lifecycle`: expiration, grace period, carry-over, revoke, scheduled activation/deactivation.

Валидации:
- `activeTo > activeFrom`;
- уникальный `code`;
- допустимые `type` и `status`;
- обязательные поля для выбранного `type`;
- отсутствие отрицательных лимитов;
- валидная валюта в типоспецифичных правилах.

### `GET /api/admin/benefit-programs/programs/{programId}`
Возвращает карточку программы, текущий статус, catalogId, ownerRole, currentVersion и mnemonic результата, если он нужен frontend.

### `PUT /api/admin/benefit-programs/programs/{programId}`
Обновляет программу через новую версию правил. Требует `If-Match` для optimistic locking. Для активной программы не переписывает события и выгоды, уже созданные downstream-контурами.

### `POST /api/admin/benefit-programs/programs/{programId}/dry-run`
Выполняет dry-run применимости без побочных эффектов.

DTO `BenefitProgramDryRunRequest`:
- `partnerNumber` или `userId`;
- `catalogId`;
- `cartId` или `orderId`, если сценарий связан с cart/checkout;
- `scenario`: `CATALOG_VIEW`, `CART`, `CHECKOUT`, `REWARD_REDEMPTION`, `SUBSCRIPTION_RENEWAL`.

Ответ содержит:
- `applicable`: применимые program codes;
- `rejected`: program code и reason mnemonic;
- `calculatedDiscount`, `calculatedCashback`;
- `warnings`;
- `correlationId`.

Dry-run не создает financial transaction, reservation, redemption, notification и integration event.

### `POST /api/admin/benefit-programs/programs/{programId}/status`
Меняет статус программы по жизненному циклу `DRAFT -> READY_FOR_REVIEW -> SCHEDULED -> ACTIVE -> PAUSED -> ARCHIVED`. Требует `If-Match`, `targetStatus`, `reasonCode`, опционально `scheduledAt`.

Недопустимые переходы возвращают `409` и mnemonic `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_INVALID_STATUS_TRANSITION`.

### `PUT /api/admin/benefit-programs/programs/{programId}/budgets`
Обновляет финансовые лимиты. Доступно роли со scope `ADMIN_BENEFIT_PROGRAM_FINANCE`.

DTO:
- `currency`;
- `totalBudget`;
- `cashbackLimit`;
- `discountLimit`;
- `redemptionLimit`;
- `stopOnExhausted`.

### `POST /api/admin/benefit-programs/programs/{programId}/rewards`
Добавляет reward shop item к программе типа `REWARD_SHOP`.

DTO:
- `rewardCode`;
- `costAmount`, `currency`;
- `regionCode`, `warehouseCode`;
- `activeFrom`, `activeTo`;
- `redemptionLimit`.

### `POST /api/admin/benefit-programs/programs/{programId}/manual-adjustments`
Создает ручную корректировку cashback, reward eligibility, reservation или subscription status.

Валидации:
- указан `targetUserId` или `targetPartnerNumber`;
- указан `reasonCode`;
- указан `evidenceRef`;
- корректировка не создает отрицательный баланс и не обходит лимиты без approved exception.

Права:
- `ADMIN_BENEFIT_PROGRAM_FINANCE` для финансовых корректировок;
- `ADMIN_BENEFIT_PROGRAM_MANAGE` для нефинансовых корректировок.

## Endpoint group `AdminBenefitProgramAudit`

### `GET /api/admin/benefit-programs/programs/{programId}/audit-events`
Возвращает audit events по программе. Фильтры: `actionCode`, `correlationId`.

Событие содержит `actorUserId`, `actorRole`, `actionCode`, `entityType`, `entityId`, `beforeSummary`, `afterSummary`, `reasonCode`, `correlationId`, `createdAt`. Персональные данные маскируются в сервисном слое, если роль не имеет расширенного PII scope.

### `GET /api/admin/benefit-programs/programs/{programId}/integration-events`
Возвращает журнал исходящих событий в downstream-контуры.

Target contexts:
- `PARTNER_BENEFITS`
- `BONUS_WALLET`
- `CART`
- `CHECKOUT`
- `NOTIFICATION`
- `ADMIN_PLATFORM`

Событие содержит `idempotencyKey`, `payloadChecksum`, `status`, `retryCount`, `lastErrorMnemonic`, `correlationId`. Секреты, токены и пользовательские UI-тексты не возвращаются.

## Mnemonic-коды
Backend не возвращает hardcoded user-facing text. Для предопределенных сообщений используются mnemonic-коды:
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ACCESS_DENIED`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_VALIDATION_FAILED`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_NOT_FOUND`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_VERSION_CONFLICT`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_INVALID_STATUS_TRANSITION`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_BUDGET_EXHAUSTED`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ELIGIBILITY_DENIED`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_DRY_RUN_COMPLETED`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_PUBLISHED`
- `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_MANUAL_ADJUSTMENT_REQUIRES_APPROVAL`

Frontend обязан добавить эти коды во все поддерживаемые dictionaries.

## Ошибки и статусы
- `400`: ошибки валидации DTO, бизнес-параметров, period, currency, required type settings.
- `403`: нет нужного scope.
- `404`: program, reward, adjustment или audit entity не найдены.
- `409`: optimistic locking, idempotency conflict, invalid status transition, budget conflict, repeated manual adjustment.

## Идемпотентность и аудит
State-changing операции создают audit event с `actorUserId`, `actorRole`, `actionCode`, `entityType`, `entityId`, `before/after summary`, `reasonCode`, `correlationId`.

Публикация, интеграционная отправка, ручная корректировка и downstream event должны использовать idempotency key. Повторный запрос с тем же ключом не создает дубль budget usage, cashback adjustment, reservation или integration event.

## Версионная база
API проектируется для монолитной платформы Best Ori Gin на дату старта feature #41, 28.04.2026. Новая реализация должна использовать latest-stable baseline, совместимый с текущим монолитом, и следовать policy: backend package ownership, Liquibase XML, `STR_MNEMO_*`, frontend i18n и React return type policy.
