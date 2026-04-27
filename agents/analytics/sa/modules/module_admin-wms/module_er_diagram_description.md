# ER description. Module admin-wms

## Назначение модуля
`admin-wms` отвечает за административные данные WMS/1C: склады, складские зоны, остатки SKU, availability rules, поставки, межскладские перемещения, складские движения, резервы под заказы, sync runs/messages, quarantine, расхождения и immutable audit trail. Модуль является владельцем складской доступности, которую потребляют публичный каталог, карточка товара, корзина, checkout, партнерские офлайн-заказы, партнерский офис и employee-поддержка заказов.

## Package ownership
Backend module должен соблюдать обязательную структуру:
- `com.bestorigin.monolith.adminwms.api` — REST DTO, request/response contracts, enum contracts и module-facing API types.
- `com.bestorigin.monolith.adminwms.domain` — JPA entities и repository interfaces только.
- `com.bestorigin.monolith.adminwms.db` — Liquibase XML changelog ownership и changelog registration metadata.
- `com.bestorigin.monolith.adminwms.impl` — controller, service, validator, mapper, exception, config, security, scheduler, client и orchestration code в role-specific подпакетах.

## Таблицы и поля

### admin_wms_warehouse
Агрегат склада.
- `warehouse_id uuid PK` — технический идентификатор.
- `warehouse_code varchar(80) UK` — бизнес-код склада.
- `name varchar(255)` — административное название.
- `warehouse_type`, `region_code`, `status`, `source_system` — тип, регион, lifecycle и источник.
- `external_warehouse_id varchar(120)` — идентификатор WMS/1C.
- `sales_channels jsonb` — разрешенные каналы продаж.
- `receiving_sla_hours`, `shipping_sla_hours`, `sync_schedule` — операционные настройки.
- `version`, `created_at`, `updated_at` — optimistic locking и audit metadata.

### admin_wms_stock_item
Снимок доступности SKU по складу, каталогу и каналу.
- `stock_item_id uuid PK`.
- `warehouse_id uuid FK`.
- `product_id`, `sku`, `catalog_period_code`, `channel_code`.
- `physical_qty`, `sellable_qty`, `reserved_qty`, `blocked_qty`, `inbound_qty`, `backorder_qty`.
- `availability_status` — `SELLABLE`, `RESERVED_ONLY`, `BACKORDER_ALLOWED`, `NOT_SELLABLE`, `QUALITY_HOLD`.
- `last_sync_at`, `version`.

### admin_wms_availability_rule
Правило доступности SKU.
- `rule_id uuid PK`.
- `warehouse_id uuid FK`, `sku`, `channel_code`.
- `policy`, `reason_code`, `active_from`, `active_to`, `version`.

### admin_wms_supply и admin_wms_supply_line
Поставка и ее строки.
- `supply_id uuid PK`, `supply_code UK`, `warehouse_id FK`, `source_system`, `supplier_code`, `external_document_id`.
- `status` — `PLANNED`, `CONFIRMED`, `IN_TRANSIT`, `ARRIVED`, `ACCEPTANCE_IN_PROGRESS`, `ACCEPTED`, `PARTIALLY_ACCEPTED`, `CANCELLED`.
- `expected_at`, `arrived_at`, `idempotency_key`, `version`.
- Строка содержит `external_line_id`, `sku`, `planned_qty`, `accepted_qty`, `damaged_qty`, `shortage_qty`, `surplus_qty`, `reason_code`.

### admin_wms_stock_movement
Неизменяемое складское движение.
- `movement_id uuid PK`.
- `warehouse_id`, `sku`, `movement_type`, `quantity_delta`.
- `source_document_type`, `source_document_id`, `external_document_id`.
- `idempotency_key`, `correlation_id`, `occurred_at`.

### admin_wms_reservation
Резерв под заказ.
- `reservation_id uuid PK`, `order_id`, `warehouse_id`, `sku`, `quantity`.
- `status` — `HELD`, `CONFIRMED`, `RELEASED`, `EXPIRED`.
- `hold_expires_at`, `idempotency_key UK`, `version`.

### admin_wms_sync_run и admin_wms_sync_message
Запуск синхронизации и сообщения обмена WMS/1C.
- Sync run содержит source system, warehouse filter, SKU filter, document type, status, counters, correlationId и timestamps.
- Sync message содержит external document/line ids, idempotency key, message status, payload checksum, reason code, affected SKU, retry count и receivedAt.
- Quarantine реализуется через `message_status=QUARANTINED` и reason code конфликта.

### admin_wms_discrepancy
Расхождение приемки, отгрузки или синхронизации.
- `discrepancy_id uuid PK`, `warehouse_id`, `sku`, `supply_id`.
- `discrepancy_type`, `severity`, `quantity_delta`, `status`, `reason_code`, `owner_user_id`, `correlation_id`.

### admin_wms_audit_event
Immutable audit trail.
- `audit_event_id uuid PK`, `actor_user_id`, `action_code`, `entity_type`, `entity_id`.
- `old_value jsonb`, `new_value jsonb`, `reason_code`, `idempotency_key`, `correlation_id`, `occurred_at`.

## Ограничения и индексы
- Unique: `warehouse_code` для складов.
- Unique: `warehouse_id + sku + catalog_period_code + channel_code` для stock snapshot.
- Unique: `source_system + external_document_id + external_line_id + idempotency_key` для sync message и stock movement idempotency.
- Unique: `idempotency_key` для reservations.
- Index: `warehouse_id + sku`, `sku + availability_status`, `catalog_period_code + channel_code` для storefront availability.
- Index: `supply_code`, `external_document_id`, `status` для поставок.
- Index: `correlation_id`, `entity_type + entity_id`, `actor_user_id`, `occurred_at` для audit search.

## Валидации
- Нельзя создать sellable reservation, если склад заблокирован или sellable quantity недостаточен без backorder policy.
- Нельзя подтвердить приемку поставки повторно с тем же idempotency key так, чтобы остаток увеличился второй раз.
- Нельзя применить конфликтное WMS/1C сообщение к stock snapshot без выхода из quarantine.
- Ручная корректировка требует permission scope, reason code и документ-основание.
- Backend возвращает предопределенные сообщения только mnemonic-кодами `STR_MNEMO_ADMIN_WMS_*`.

## Версионная база
Артефакт рассчитан на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Hibernate/JPA, Liquibase XML, PostgreSQL JSONB и Springdoc OpenAPI runtime generation для monolith module `admin-wms`. Отклонение от baseline требует отдельного compatibility decision и follow-up задачи.
