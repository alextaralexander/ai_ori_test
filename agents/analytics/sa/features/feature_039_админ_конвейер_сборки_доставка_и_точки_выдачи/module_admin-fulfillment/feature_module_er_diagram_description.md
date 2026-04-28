# Описание ER-изменений feature #39 для модуля admin-fulfillment

## Назначение модуля
`admin-fulfillment` является monolith module с module key `admin-fulfillment` и package prefix `com.bestorigin.monolith.adminfulfillment`. Модуль владеет административным контуром `/admin/fulfillment`: операционной панелью сборки, этапами pick/pack/sort/ship, настройками служб доставки, сетью ПВЗ, приемкой/выдачей отправлений, проблемными статусами, возвратной логистикой, интеграционным журналом и audit trail.

## Version baseline на 28.04.2026
- Java: latest stable Java 26 / 26.0.1 CPU baseline; если monolith runtime пока не совместим, код фиксирует compatibility fallback в backend baseline и отдельную upgrade-задачу.
- Spring Boot: latest stable 4.0.6; OpenAPI генерируется `springdoc-openapi` по правилам monolith Swagger policy.
- PostgreSQL: latest stable major 18 с актуальным minor 18.3 для managed PostgreSQL baseline; Liquibase changeset создается отдельным XML-файлом модуля.
- Backend package policy: `api` содержит DTO и contracts, `domain` содержит JPA entities и repositories, `db` содержит только Liquibase XML, `impl` содержит controllers, services, validators, mappers, integration clients, security и config в role-specific subpackages.

## Основные сущности

### `admin_fulfillment_task`
Хранит операционное задание конвейера для заказа или отправления.
- `id uuid` primary key.
- `task_code varchar(80)` человекочитаемый код задания, уникален.
- `order_id varchar(80)` внешний идентификатор заказа из order/admin-order контура, обязателен.
- `shipment_id varchar(80)` идентификатор отправления, nullable до создания shipment.
- `warehouse_code varchar(64)` склад WMS/1C, обязателен.
- `zone_code varchar(64)` зона хранения или линия конвейера.
- `stage varchar(40)` один из `PICK_PENDING`, `PICK_IN_PROGRESS`, `PACK_PENDING`, `PACK_IN_PROGRESS`, `SORT_PENDING`, `READY_TO_SHIP`, `SHIPPED`, `EXCEPTION`, `RETURN_IN_PROGRESS`, `RETURNED`.
- `status varchar(32)` `OPEN`, `IN_PROGRESS`, `COMPLETED`, `ON_HOLD`, `CANCELLED`.
- `priority int` сортировка очереди; индексируется вместе с `sla_deadline_at`.
- `sla_deadline_at timestamptz` контрольный срок этапа.
- `assigned_actor_id varchar(80)` оператор или администратор.
- `idempotency_key varchar(120)` ключ последней state-changing операции.
- `correlation_id varchar(80)` общий trace заказа/отправления.
- `created_at`, `updated_at timestamptz`.

Ограничения: `task_code` unique, `order_id` not null, `stage` и `status` check constraints, индекс `(stage, status, sla_deadline_at)`, индекс `(order_id)`, индекс `(shipment_id)`, индекс `(correlation_id)`.

### `admin_fulfillment_event`
Immutable timeline операционных событий.
- `id uuid` primary key.
- `task_id uuid` foreign key на `admin_fulfillment_task`.
- `order_id`, `shipment_id` дублируются для поиска без join.
- `event_type varchar(64)` `STAGE_CHANGED`, `SCAN_ACCEPTED`, `EXCEPTION_CREATED`, `SHIPMENT_SENT`, `RETURN_CREATED`, `REDELIVERY_REQUESTED`.
- `stage_from`, `stage_to varchar(40)` переход этапа.
- `source_system varchar(64)` `ADMIN_UI`, `CONVEYOR`, `WMS_1C`, `DELIVERY`, `PICKUP_POINT`.
- `actor_id varchar(80)` пользователь или service account.
- `reason_code varchar(80)` обязателен для exception, hold, reroute, return и cancellation.
- `idempotency_key varchar(120)` unique для внешних событий в рамках source system.
- `correlation_id varchar(80)`, `occurred_at timestamptz`.

Ограничения: внешние события уникальны по `(source_system, idempotency_key)`, индекс `(task_id, occurred_at)`, индекс `(order_id, occurred_at)`, индекс `(shipment_id, occurred_at)`.

### `admin_delivery_service`
Справочник служб доставки и их интеграционных режимов.
- `service_code varchar(64)` unique business key.
- `display_name_key varchar(120)` i18n key, не пользовательский текст.
- `status varchar(24)` `DRAFT`, `ACTIVE`, `SUSPENDED`, `ARCHIVED`.
- `integration_mode varchar(32)` `INTERNAL`, `EXTERNAL_API`, `MANUAL`.
- `endpoint_alias varchar(80)` alias внешнего endpoint без секретов.
- `version int` optimistic locking.

### `admin_delivery_tariff`
Тарифы доставки по зоне, способу и периоду.
- `service_id uuid` foreign key на `admin_delivery_service`.
- `zone_code varchar(64)`, `delivery_method varchar(32)`, `currency varchar(3)`, `base_amount numeric(19,4)`.
- `valid_from`, `valid_to timestamptz`, `priority int`.

Ограничения: `base_amount >= 0`, `currency` длиной 3, индекс `(service_id, zone_code, delivery_method)`. Пересечение активных тарифов с одинаковой зоной, способом и приоритетом запрещается service validation.

### `admin_delivery_sla_rule`
Правила SLA для этапов delivery/fulfillment.
- `service_id uuid` foreign key.
- `zone_code varchar(64)`, `stage varchar(40)`, `duration_minutes int`, `status varchar(24)`, `valid_from`, `valid_to`.
- `duration_minutes > 0`, индекс `(service_id, zone_code, stage, status)`.

### `admin_pickup_point`
Карточка точки выдачи.
- `pickup_point_code varchar(64)` unique.
- `owner_user_id varchar(80)` ссылка на identity/admin-identity subject.
- `status varchar(32)` `DRAFT`, `ACTIVE`, `TEMPORARILY_CLOSED`, `SUSPENDED`, `ARCHIVED`.
- `address_text varchar(500)`, `latitude numeric(10,7)`, `longitude numeric(10,7)`.
- `storage_limit_days int`, `shipment_limit int`, `version int`, timestamps.

Ограничения: публикация требует владельца, адрес, график, лимит хранения и зону обслуживания; `storage_limit_days > 0`, `shipment_limit > 0`, индекс `(status)`, индекс `(owner_user_id)`.

### `admin_pickup_point_schedule`
График ПВЗ.
- `pickup_point_id uuid` foreign key.
- `day_of_week int` 1-7.
- `open_time`, `close_time time`.
- `temporary_closed boolean`, `reason_code varchar(80)`.

Ограничения: unique `(pickup_point_id, day_of_week)`, `close_time > open_time` для рабочих дней.

### `admin_pickup_shipment`
Операционное состояние отправления в ПВЗ.
- `pickup_point_id uuid` foreign key.
- `shipment_id varchar(80)` unique business key.
- `order_id varchar(80)`.
- `status varchar(40)` `EXPECTED`, `ARRIVED_AT_PICKUP_POINT`, `ACCEPTED`, `READY_FOR_PICKUP`, `DELIVERED`, `PARTIALLY_DELIVERED`, `NOT_COLLECTED`, `DAMAGED`, `RETURN_IN_PROGRESS`, `RETURNED`.
- `recipient_check_code_hash varchar(160)` hash проверочного кода, исходный код не хранится.
- `storage_expires_at timestamptz`, `last_reason_code`, `correlation_id`, timestamps.

Ограничения: unique `(shipment_id)`, индекс `(pickup_point_id, status, storage_expires_at)`, индекс `(order_id)`.

### `admin_fulfillment_integration_event`
Журнал интеграций с WMS/1C, конвейером, delivery contour и bonus/service callbacks.
- `direction varchar(16)` `INBOUND` или `OUTBOUND`.
- `source_system`, `endpoint_alias`, `external_id`, `status`, `checksum`, `retry_count`.
- `last_error_code`, `last_error_message_mnemonic` только технические коды без hardcoded UI-текста.
- `idempotency_key`, `correlation_id`, `created_at`.

Ограничения: unique `(source_system, external_id, idempotency_key)` для state-changing сообщений, индекс `(status, created_at)`, индекс `(correlation_id)`.

### `admin_fulfillment_audit_event`
Неизменяемый audit trail.
- `actor_id`, `actor_role`, `action_code`, `entity_type`, `entity_id`, `reason_code`, `source_system`.
- `before_summary`, `after_summary` содержат безопасные summary без секретов и лишних персональных данных.
- `correlation_id`, `created_at`.

Ограничения: append-only на уровне сервиса; индекс `(entity_type, entity_id, created_at)`, индекс `(actor_id, created_at)`, индекс `(correlation_id)`.

## Связи с другими модулями
- `order` и `admin-order`: передают order lifecycle, payment readiness, supplementary order и return events по структурированным DTO.
- `admin-wms`: передает warehouse reserve, stock discrepancy, return acceptance и warehouse task events.
- `delivery`: получает shipment lifecycle и возвращает delivery tracking events.
- `admin-bonus`: получает события невыкупа, частичной выдачи, возврата и ручной корректировки для reversal/adjustment.
- `admin-platform`: получает KPI-события fulfillment SLA, delivery SLA, pickup load, problem rate и integration health.
- `admin-identity`: предоставляет subject/owner bindings для владельцев ПВЗ и операторов.
