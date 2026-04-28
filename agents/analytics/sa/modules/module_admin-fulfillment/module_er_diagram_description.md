# Полное описание ER модуля admin-fulfillment

## Назначение
Модуль `admin-fulfillment` является владельцем административных данных для конвейера сборки, служб доставки, SLA, точек выдачи, операций владельца ПВЗ, интеграционного журнала и audit trail. Он не хранит master-data пользователей, заказов, платежей, бонусов или складских остатков как свои authoritative данные; такие идентификаторы сохраняются ссылками (`order_id`, `shipment_id`, `owner_user_id`, `warehouse_code`, `source_system`, `correlation_id`).

## Version baseline
На дату старта задачи 28.04.2026 целевой latest-stable baseline: Java 26 / 26.0.1 CPU, Spring Boot 4.0.6, PostgreSQL 18.3, OpenAPI 3.0.3. Если текущий monolith build baseline не совместим, реализация фиксирует fallback централизованно и не дублирует версии внутри модуля.

## Package ownership
- `api`: REST DTO, request/response contracts, enum-like API constants.
- `domain`: JPA entities и repository interfaces для всех таблиц `admin_fulfillment_*`, `admin_delivery_*`, `admin_pickup_*`.
- `db`: Liquibase XML changelog `feature-039-admin-fulfillment.xml`.
- `impl/controller`: Spring MVC controllers.
- `impl/service`: orchestration и application services.
- `impl/validator`: stage transition, tariff, SLA, pickup point, pickup owner scope validators.
- `impl/mapper`: MapStruct mappers.
- `impl/client`: clients/adapters к `admin-wms`, `order`, `admin-order`, `delivery`, `admin-bonus`, `admin-platform`.
- `impl/security`: permission scopes и owner checks.
- `impl/config`: MonolithModule registration и OpenAPI group.

## Таблицы и ключи
- `admin_fulfillment_task`: primary operational task. Primary key `id`; unique `task_code`; indexes `(stage, status, sla_deadline_at)`, `(order_id)`, `(shipment_id)`, `(correlation_id)`.
- `admin_fulfillment_event`: immutable timeline. Primary key `id`; foreign key `task_id`; unique external event key `(source_system, idempotency_key)` when idempotency key is present; indexes `(task_id, occurred_at)`, `(order_id, occurred_at)`, `(shipment_id, occurred_at)`.
- `admin_delivery_service`: delivery service dictionary. Primary key `id`; unique `service_code`; optimistic locking through `version`.
- `admin_delivery_tariff`: tariff by service, zone, method and period. Foreign key `service_id`; checks `base_amount >= 0`, `currency` length 3; indexes `(service_id, zone_code, delivery_method)`.
- `admin_delivery_sla_rule`: SLA rule by service, zone and stage. Foreign key `service_id`; check `duration_minutes > 0`; index `(service_id, zone_code, stage, status)`.
- `admin_pickup_point`: pickup point card. Primary key `id`; unique `pickup_point_code`; indexes `(status)`, `(owner_user_id)`.
- `admin_pickup_point_schedule`: pickup point weekly schedule. Foreign key `pickup_point_id`; unique `(pickup_point_id, day_of_week)`; check `day_of_week between 1 and 7`.
- `admin_pickup_shipment`: shipment state inside pickup point. Primary key `id`; unique `shipment_id`; indexes `(pickup_point_id, status, storage_expires_at)`, `(order_id)`.
- `admin_fulfillment_integration_event`: integration journal. Primary key `id`; unique `(source_system, external_id, idempotency_key)` for state-changing messages; indexes `(status, created_at)`, `(correlation_id)`.
- `admin_fulfillment_audit_event`: append-only audit trail. Primary key `id`; indexes `(entity_type, entity_id, created_at)`, `(actor_id, created_at)`, `(correlation_id)`.

## Статусы
Fulfillment stages: `PICK_PENDING`, `PICK_IN_PROGRESS`, `PACK_PENDING`, `PACK_IN_PROGRESS`, `SORT_PENDING`, `READY_TO_SHIP`, `SHIPPED`, `EXCEPTION`, `RETURN_IN_PROGRESS`, `RETURNED`.

Pickup shipment statuses: `EXPECTED`, `ARRIVED_AT_PICKUP_POINT`, `ACCEPTED`, `READY_FOR_PICKUP`, `DELIVERED`, `PARTIALLY_DELIVERED`, `NOT_COLLECTED`, `DAMAGED`, `RETURN_IN_PROGRESS`, `RETURNED`.

Delivery service statuses: `DRAFT`, `ACTIVE`, `SUSPENDED`, `ARCHIVED`.

Pickup point statuses: `DRAFT`, `ACTIVE`, `TEMPORARILY_CLOSED`, `SUSPENDED`, `ARCHIVED`.

## Data protection
`recipient_check_code_hash` хранит только hash проверочного кода. Секреты delivery endpoints не сохраняются в таблицах модуля; используется `endpoint_alias`. `before_summary` и `after_summary` не должны содержать токены, платежные данные, документы или полные персональные данные. Предопределенные пользовательские сообщения представлены mnemonic-кодами `STR_MNEMO_FULFILLMENT_*` и `STR_MNEMO_DELIVERY_*`.
