# Module admin-service OpenAPI description

## Runtime Swagger
Модуль `admin-service` должен быть зарегистрирован в monolith Swagger grouping как отдельный `MonolithModule` с `moduleKey=admin-service`. Канонические runtime URLs:
- OpenAPI JSON: `/v3/api-docs/admin-service`
- Swagger UI: `/swagger-ui/admin-service`

Все контроллеры должны находиться внутри package prefix `com.bestorigin.monolith.adminservice` и внутри `impl/controller`, чтобы springdoc автоматически включал endpoints в группу модуля без ручного списка endpoints.

## Base path
Все endpoints feature #34 находятся под `/api/admin/service`. Frontend admin workspace использует этот base path для очереди, карточки кейса, настроек, решений, supervisor board и audit/export.

## Security scopes
- `ADMIN_SERVICE_VIEW`: просмотр очередей, кейсов, SLA и read-only карточек.
- `ADMIN_SERVICE_MANAGE`: взятие в работу, transition lifecycle, сообщения, запросы информации, базовые решения.
- `ADMIN_SERVICE_REFUND_MANAGE`: создание refund action и работа с финансовыми ожиданиями.
- `ADMIN_SERVICE_REPLACEMENT_MANAGE`: создание replacement action и передача в WMS/логистику.
- `ADMIN_SERVICE_SUPERVISOR`: настройка очередей/SLA, supervisor board, escalation и override.
- `ADMIN_SERVICE_AUDIT_VIEW`: просмотр audit trail.
- `ADMIN_SERVICE_EXPORT`: экспорт service audit и analytics.

Frontend может скрывать недоступные actions, но backend остается источником истины и возвращает mnemonic-код `STR_MNEMO_ADMIN_SERVICE_FORBIDDEN_ACTION` при отказе.

## Endpoints

### `GET /cases`
Ищет сервисные кейсы. Фильтры: `search`, `caseStatus`, `slaStatus`, `claimType`, `queueId`, `warehouseId`, `page`, `size`. Возвращает `ServiceCasePage` со summary-полями для таблицы очереди. Поиск должен поддерживать claim number, source claim id, order id, customer id и partner id.

### `POST /cases`
Создает административный сервисный кейс из customer/employee claim flow или ручного admin action. Требует `Idempotency-Key`, `sourceClaimId`, `orderId`, `claimType`, `priority`, `reasonCode`. Backend применяет routing rules и SLA policy, создает audit event и возвращает карточку кейса.

### `GET /cases/{serviceCaseId}`
Возвращает карточку кейса: summary, messages, attachments, decisions, refund/replacement actions, WMS events, auditEvents и `allowedActions`.

### `POST /cases/{serviceCaseId}/assignment`
Взятие кейса в работу, назначение или release. `assignmentAction=TAKE|ASSIGN|RELEASE`, `ownerUserId`, `reasonCode`. Все изменения owner сохраняются в audit trail.

### `POST /cases/{serviceCaseId}/status-transition`
Перевод lifecycle: `NEW`, `ROUTED`, `IN_PROGRESS`, `WAITING_CUSTOMER`, `WAITING_WMS`, `WAITING_FINANCE`, `ESCALATED`, `RESOLVED`, `REJECTED`, `CLOSED`. Невалидный переход возвращает `STR_MNEMO_ADMIN_SERVICE_INVALID_STATUS_TRANSITION`.

### `POST /cases/{serviceCaseId}/messages`
Добавляет клиентское сообщение, внутреннюю заметку или system event. Customer-visible предопределенный результат должен передаваться через `customerVisibleMessageCode` с префиксом `STR_MNEMO_ADMIN_SERVICE_`; backend не отправляет hardcoded user-facing text.

### `GET /queues` и `POST /queues`
Чтение и настройка service queues. Создание/изменение очереди требует supervisor scope и audit reason через idempotent command. Routing/SLA policies в первой реализации могут быть частью service layer configuration и отдельного admin endpoint follow-up, но ER и Liquibase должны поддержать их сразу.

### `POST /cases/{serviceCaseId}/decisions`
Создает решение: `APPROVE_REFUND`, `APPROVE_REPLACEMENT`, `APPROVE_BONUS_COMPENSATION`, `REJECT`, `REQUEST_INFO`, `ESCALATE`. Решение требует `reasonCode`, может содержать `customerMessageCode` и проверяет текущий статус кейса, order context, payment/WMS status и compensation policy.

### `POST /decisions/{decisionId}/refund-actions`
Создает refund action из решения. Требует refund scope, idempotency key, сумму, валюту и reasonCode. Backend проверяет доступную к возврату сумму через order/payment context. Превышение лимита возвращает `STR_MNEMO_ADMIN_SERVICE_REFUND_AMOUNT_EXCEEDED`.

### `POST /decisions/{decisionId}/replacement-actions`
Создает replacement action из решения. Требует SKU, количество, склад и reasonCode. Backend проверяет WMS availability и отсутствие конфликтующего refund. Недоступность замены возвращает `STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_UNAVAILABLE`.

### `POST /wms-events`
Принимает WMS событие по service case: `RETURN_RECEIVED`, `INSPECTION_UPDATED`, `REPLACEMENT_SHIPPED`, `REPLACEMENT_DELIVERED`. Обработка идемпотентна по `sourceSystem`, `externalEventId` и `Idempotency-Key`.

### `GET /supervisor/sla-board`
Возвращает агрегаты supervisor board: active, at risk, breached и список кейсов для назначения, эскалации или override.

### `GET /audit-events` и `POST /audit-events/export`
Поиск и экспорт audit events. Экспорт должен учитывать текущие фильтры и rights masking, не раскрывая персональные, платежные и attachment данные сверх scope пользователя.

## DTO и validation
- `ServiceCaseSummary` содержит только данные для очереди и карточки preview.
- `ServiceCaseDetails` агрегирует вложенные DTO, но не является persistence model.
- `CreateDecisionRequest.customerMessageCode` обязан соответствовать pattern `^STR_MNEMO_ADMIN_SERVICE_`.
- Все state-changing endpoints принимают `Idempotency-Key`.
- `version` в `StatusTransitionRequest` нужен для optimistic locking.
- `ErrorResponse.code` всегда mnemonic, например `STR_MNEMO_ADMIN_SERVICE_INVALID_STATUS_TRANSITION`, `STR_MNEMO_ADMIN_SERVICE_FORBIDDEN_ACTION`, `STR_MNEMO_ADMIN_SERVICE_REFUND_AMOUNT_EXCEEDED`, `STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_UNAVAILABLE`, `STR_MNEMO_ADMIN_SERVICE_COMPENSATION_LIMIT_EXCEEDED`.

## Интеграции
- `admin-order`: order context, paid/refunded amounts, payment references, refund policy.
- `admin-wms`: warehouse availability, return inspection, replacement shipment events.
- `bonuswallet`: bonus compensation transaction.
- `platform-experience`: i18n/notification frontend contracts and analytics-safe message codes.
- `admin-rbac`: permission matrix and effective scopes.
- S3/MinIO: attachment metadata and storage keys.

## Версионный baseline
OpenAPI рассчитан на текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, springdoc-openapi runtime generation, Maven, Liquibase XML, PostgreSQL, React, TypeScript, Vite и Ant Design. Отклонение от baseline должно быть отражено в architecture artifacts до реализации.
