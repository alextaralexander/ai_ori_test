# Описание OpenAPI feature #39 для модуля admin-fulfillment

## Назначение API
API модуля `admin-fulfillment` обслуживает административные и операционные экраны `/admin/fulfillment`: сводную панель заказов и отправлений, рабочее место конвейера сборки, настройки служб доставки, тарифы, SLA, сеть ПВЗ, операции владельца ПВЗ, проблемные кейсы и интеграционный журнал.

Canonical Swagger для monolith module:
- OpenAPI JSON: `/v3/api-docs/admin-fulfillment`
- Swagger UI: `/swagger-ui/admin-fulfillment`

Контроллеры размещаются в `com.bestorigin.monolith.adminfulfillment.impl.controller`; DTO и API contracts размещаются в `api`; сервисы, validators, mappers, integration clients, security wiring и config размещаются в role-specific подпакетах `impl`.

## Version baseline на 28.04.2026
- Java latest stable: 26 / 26.0.1 CPU baseline.
- Spring Boot latest stable: 4.0.6.
- PostgreSQL latest stable major: 18 с актуальным minor 18.3 для managed baseline.
- OpenAPI: спецификация 3.0.3, runtime документация генерируется из Spring MVC controllers через springdoc-openapi.

Если текущий monolith baseline не совместим с latest stable, implementation stream обязан зафиксировать fallback в backend build baseline и завести upgrade follow-up, не дублируя версии внутри модулей.

## Endpoint groups

### Dashboard
- `GET /api/admin/fulfillment/dashboard/shipments` возвращает страницу заказов и отправлений для операционной панели.
- Фильтры: `warehouseCode`, `stage`, `status`, `slaRisk`, `pickupPointId`, `correlationId`.
- Ответ содержит `orderId`, `shipmentId`, `stage`, `status`, `slaDeadlineAt`, delivery method, pickup point, reason mnemonic и `correlationId`.
- Доступ: логистический администратор, администратор доставки, администратор сети ПВЗ, оператор поддержки, аудитор в read-only режиме.

### Fulfillment tasks
- `POST /tasks` создает задание конвейера по orderId/shipmentId, складу, зоне, начальному этапу и SLA.
- `POST /tasks/{taskId}/stage` переводит задание на следующий этап. Header `Idempotency-Key` обязателен. Сервис валидирует последовательность pick -> pack -> sort -> ready-to-ship -> shipped.
- `POST /tasks/{taskId}/exceptions` создает exception, hold, reroute, return или lost event с обязательным reasonCode.

Ошибки:
- `STR_MNEMO_FULFILLMENT_INVALID_STAGE_TRANSITION` для недопустимого перехода.
- `STR_MNEMO_FULFILLMENT_TASK_NOT_FOUND` для отсутствующего задания.
- `STR_MNEMO_FULFILLMENT_IDEMPOTENCY_CONFLICT` для повторного ключа с другим payload.
- `STR_MNEMO_FULFILLMENT_REASON_REQUIRED` для problem action без причины.

### Delivery services
- `GET /delivery-services` ищет службы доставки по статусу и зоне.
- `POST /delivery-services` создает draft службы доставки. `displayNameKey` является frontend i18n key, не готовым UI-текстом.
- `POST /delivery-services/{serviceId}/activate` публикует службу после проверки активных тарифов и SLA.
- `POST /delivery-services/{serviceId}/tariffs` добавляет тариф по зоне, способу доставки, валюте и периоду.
- `POST /delivery-services/{serviceId}/sla-rules` добавляет SLA-правило по зоне и этапу.

Ошибки:
- `STR_MNEMO_DELIVERY_SERVICE_CODE_CONFLICT`.
- `STR_MNEMO_DELIVERY_TARIFF_PERIOD_CONFLICT`.
- `STR_MNEMO_DELIVERY_SLA_RULE_REQUIRED`.
- `STR_MNEMO_DELIVERY_SERVICE_CANNOT_ACTIVATE`.

### Pickup points
- `GET /pickup-points` ищет ПВЗ по статусу, владельцу и зоне.
- `POST /pickup-points` создает draft ПВЗ с адресом, координатами, владельцем, лимитами и зонами.
- `POST /pickup-points/{pickupPointId}/activate` публикует ПВЗ после проверки владельца, адреса, графика, лимитов и зон.
- `POST /pickup-points/{pickupPointId}/temporary-close` временно закрывает ПВЗ с обязательным reasonCode. Новые назначения блокируются, существующие отправления остаются видимыми ответственным ролям.

Ошибки:
- `STR_MNEMO_DELIVERY_PICKUP_POINT_CODE_CONFLICT`.
- `STR_MNEMO_DELIVERY_PICKUP_POINT_OWNER_REQUIRED`.
- `STR_MNEMO_DELIVERY_PICKUP_POINT_SCHEDULE_REQUIRED`.
- `STR_MNEMO_DELIVERY_PICKUP_POINT_TEMPORARILY_CLOSED`.
- `STR_MNEMO_DELIVERY_PICKUP_POINT_CAPACITY_EXCEEDED`.

### Pickup shipments
- `POST /pickup-shipments/{shipmentId}/accept` фиксирует приемку отправления в ПВЗ. Header `Idempotency-Key` обязателен.
- `POST /pickup-shipments/{shipmentId}/deliver` выдает отправление по `recipientCheckCode`. Код сравнивается только с hash, исходное значение не хранится.
- `POST /pickup-shipments/{shipmentId}/not-collected` фиксирует невыкуп и запускает возвратную логистику.

Операции владельца ПВЗ ограничены только принадлежащими ему точками. Частичная выдача передает список выданных SKU и reasonCode, а для возвращаемых позиций публикуются структурированные события в orders, service, payment, delivery, WMS и bonus.

Ошибки:
- `STR_MNEMO_DELIVERY_PICKUP_SHIPMENT_NOT_FOUND`.
- `STR_MNEMO_DELIVERY_PICKUP_OWNER_SCOPE_DENIED`.
- `STR_MNEMO_DELIVERY_RECIPIENT_CODE_INVALID`.
- `STR_MNEMO_DELIVERY_STORAGE_NOT_EXPIRED`.

### Integration events
- `GET /integration-events` возвращает журнал обменов с WMS/1C, конвейером, delivery contour, service, orders/payments, bonus и admin-platform.
- Фильтры: `sourceSystem`, `status`, `correlationId`.
- Ответ содержит endpoint alias, externalId, checksum, retryCount, lastErrorCode, `lastErrorMessageMnemonic` и `correlationId`.
- Секреты, токены, полные адреса получателей и платежные данные в ответ не попадают.

## DTO и validation
- Все state-changing запросы используют `Idempotency-Key`, если операция может быть повторена из UI или integration retry.
- Все пользовательские сообщения, которые backend может вернуть во frontend, представлены mnemonic-кодами `STR_MNEMO_FULFILLMENT_*` или `STR_MNEMO_DELIVERY_*`.
- DTO не содержат hardcoded user-facing text. Поля `displayNameKey` и `lastErrorMessageMnemonic` являются ключами для frontend i18n dictionaries.
- Даты и время передаются в ISO-8601 date-time с timezone.
- Денежные значения в тарифах используют decimal number и currency ISO-4217 длиной 3 символа.

## Security
- Административные endpoint-ы требуют authenticated employee/admin session и permission scopes:
  - `ADMIN_FULFILLMENT_VIEW`
  - `ADMIN_FULFILLMENT_MANAGE`
  - `ADMIN_FULFILLMENT_CONVEYOR_OPERATE`
  - `ADMIN_DELIVERY_MANAGE`
  - `ADMIN_PICKUP_POINT_MANAGE`
  - `PICKUP_POINT_OWNER_OPERATE`
  - `ADMIN_FULFILLMENT_AUDIT_VIEW`
- ПВЗ owner scope проверяется server-side по `ownerUserId` и назначенным точкам.
- Audit trail создается для просмотра операционной панели, изменения этапов, exception actions, публикации служб доставки, изменения ПВЗ, приемки, выдачи, невыкупа, retry integration и экспорта журнала.

## Интеграционные контракты
- `admin-wms` публикует warehouse reserve, stock discrepancy, return acceptance и warehouse task events.
- `order` и `admin-order` публикуют order readiness, payment status, supplementary order, cancellation и return events.
- `delivery` получает shipment lifecycle и возвращает tracking statuses.
- `admin-bonus` получает events `NOT_COLLECTED`, `PARTIALLY_DELIVERED`, `RETURNED` для reversal/adjustment.
- `admin-platform` получает KPI events: fulfillment SLA, delivery SLA, pickup load, problem rate, integration health.
