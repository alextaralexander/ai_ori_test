# OpenAPI description feature #38 / module admin-bonus

## Назначение API
`admin-bonus` предоставляет административный REST API `/api/admin/bonus-program` для правил компенсационного плана, квалификаций, расчетов начислений, payout batches и журналов интеграции. В monolith Swagger модуль должен иметь группу `admin-bonus`, URL `/v3/api-docs/admin-bonus` и `/swagger-ui/admin-bonus`.

## Endpoint-ы
- `GET /rules`: поиск правил по статусу и типу. Возвращает страницу `BonusRulePage` без пользовательских текстов из backend.
- `POST /rules`: создание draft-правила. Валидирует `ruleCode`, `ruleType`, `currency`, `rateValue`, `validFrom`, `validTo`, положительную ставку и непустой период.
- `POST /rules/{ruleId}/preview`: расчет preview без финансовых записей. Возвращает `calculationBase`, `expectedAmount`, `currency`, `appliedRestrictions`.
- `POST /rules/{ruleId}/activate`: активация правила после проверки конфликтов. При пересечении приоритета возвращает `STR_MNEMO_BONUS_RULE_PRIORITY_CONFLICT`.
- `POST /qualifications`: создание версии квалификации. Валидирует уникальность `(qualificationCode, version)`, непротиворечивость порогов и связку с бонусной ставкой.
- `POST /calculations`: запуск расчета за период. Возвращает `202 Accepted`, `calculationId`, `periodCode`, `status`, `correlationId`. Повторный запуск требует `recalculation=true`.
- `GET /accruals`: поиск начислений по периоду, партнеру и статусу для административной сверки.
- `POST /payout-batches`: создание batch только из начислений `PAYOUT_READY`.
- `POST /payout-batches/{batchId}/approve`: перевод batch в `APPROVED` при наличии финансового permission scope.
- `POST /payout-batches/{batchId}/send`: идемпотентная отправка во внешний bonus/payment contour по `externalId`.
- `GET /integrations/events`: журнал обмена с endpoint alias, retryCount, checksum, lastErrorCode, lastErrorMessageMnemonic и correlationId.

## DTO и validation
DTO должны находиться в `api` пакете module admin-bonus. Runtime классы: controllers в `impl/controller`, services в `impl/service`, exceptions в `impl/exception`, config в `impl/config`. JPA entities и repositories относятся к `domain`, Liquibase XML к `db` пакету.

## Mnemonic contract
Backend не возвращает hardcoded user-facing text. Предопределенные ошибки и результаты, которые frontend показывает пользователю, возвращаются только кодами `STR_MNEMO_ADMIN_BONUS_*`, например `STR_MNEMO_BONUS_RULE_PRIORITY_CONFLICT`, `STR_MNEMO_BONUS_PERIOD_CLOSED`, `STR_MNEMO_BONUS_PAYOUT_BATCH_EMPTY`, `STR_MNEMO_BONUS_INTEGRATION_RETRY_ACCEPTED`. Frontend обязан добавить локализации во все поддерживаемые словари.

## Безопасность и аудит
Все modifying endpoint-ы требуют административной роли и permission scope: `ADMIN_BONUS_RULE_MANAGE`, `ADMIN_BONUS_QUALIFICATION_MANAGE`, `ADMIN_BONUS_CALCULATION_RUN`, `ADMIN_BONUS_PAYOUT_MANAGE`, `ADMIN_BONUS_INTEGRATION_VIEW` или `ADMIN_BONUS_AUDIT_VIEW`. Каждое изменение сохраняет audit event с actorId, actorRole, actionCode, reasonCode и correlationId.