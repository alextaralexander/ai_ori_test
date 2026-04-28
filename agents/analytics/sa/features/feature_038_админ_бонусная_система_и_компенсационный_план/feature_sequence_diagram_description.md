# Sequence description feature #38. Админ: бонусная система и компенсационный план

## Основной поток настройки правила
Администратор бонусной программы работает в admin frontend на маршрутах `/admin/bonus-program`. Frontend вызывает `admin-bonus API` по REST endpoint-ам `/rules`, `/rules/{ruleId}/preview` и `/rules/{ruleId}/activate`. Service layer валидирует обязательные поля, проверяет конфликт активных правил и пишет `admin_bonus_rule` и `admin_bonus_audit_event`. При конфликте backend возвращает mnemonic-код `STR_MNEMO_BONUS_RULE_PRIORITY_CONFLICT`, а frontend локализует сообщение из i18n dictionaries.

## Расчет начислений
MLM-менеджер запускает расчет через `POST /calculations`. `admin-bonus service` читает подтвержденные события заказов, оплат, доставки, возвратов и претензий из доменных контуров или read-model. Для периода создаются `admin_bonus_calculation_period` и `admin_bonus_accrual` со статусами `HOLD`, `ACCRUAL`, `PAYOUT_READY`, `REVERSED`, `ADJUSTED`. Расчет публикует агрегаты в `admin-platform KPI`, чтобы feature #36 показывала bonus accruals и problem rate.

## Payout batch и интеграция
Финансовый менеджер создает batch через `POST /payout-batches`; service выбирает только `PAYOUT_READY` начисления и создает `DRAFT`. После approve batch отправляется во внешний bonus/payment contour через `POST /payout-batches/{batchId}/send`. Отправка идемпотентна по `externalId`; повтор не создает дублей выплат. Результат обмена сохраняется в `admin_bonus_integration_event` с endpoint alias, checksum, retryCount, lastErrorCode, lastErrorMessageMnemonic и correlationId.

## Аудит и безопасность
Каждая state-changing операция пишет audit event с actorId, actorRole, actionCode, reasonCode, entityType/entityId и correlationId. Endpoint-ы защищены permission scopes `ADMIN_BONUS_RULE_MANAGE`, `ADMIN_BONUS_QUALIFICATION_MANAGE`, `ADMIN_BONUS_CALCULATION_RUN`, `ADMIN_BONUS_PAYOUT_MANAGE`, `ADMIN_BONUS_INTEGRATION_VIEW` и `ADMIN_BONUS_AUDIT_VIEW`. Backend package ownership: DTO в `api`, entities/repositories в `domain`, Liquibase XML в `db`, controllers/services/exceptions/config в `impl/<role>`.