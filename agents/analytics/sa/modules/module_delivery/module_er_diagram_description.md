# module_delivery ER diagram description

## Назначение модуля
`module_delivery` владеет доменной моделью доставки Best Ori Gin: выбором способа доставки, справочником точек выдачи, отправлениями, лентой tracking statuses, операциями владельца ПВЗ и интеграционными событиями внешней доставки. Модуль не владеет заказом, оплатой, WMS-остатками, сервисными кейсами или бонусами; для этих контуров он публикует структурированные события.

## Версионный baseline
На дату старта feature #37, 28.04.2026, module_delivery должен использовать текущий стабильный baseline репозитория: Java 21, Spring Boot 3.x, Maven 3.9.x, Hibernate 6.x, Liquibase 4.x, MapStruct 1.6.x, Lombok 1.18.x, PostgreSQL 16.x. Версии управляются централизованно backend build baseline и не дублируются в модуле.

## Package ownership
- `api` - REST DTO, request/response models, enum contracts.
- `domain` - только JPA entities и repository interfaces.
- `db` - только Liquibase XML changelog-и module_delivery.
- `impl/controller` - Spring MVC controllers.
- `impl/service` - use cases, transaction boundaries, lifecycle validation, idempotency.
- `impl/mapper` - MapStruct mappers между domain и api DTO.
- `impl/validator` - validation helpers for delivery method, pickup point and status transitions.
- `impl/event` - outbound domain events for order, WMS, service, bonus and admin platform.
- `impl/security` - role/scope checks for customer, partner, pickup-owner, delivery-operator, employee and admin.
- `impl/exception` - mnemonic error mapping with `STR_MNEMO_DELIVERY_*`.

## Таблицы

### `delivery_pickup_point`
Справочник ПВЗ. Ключевые ограничения: уникальный `code`, `storage_limit_days >= 1`, `max_orders_capacity >= 0`, status из `ACTIVE`, `TEMPORARILY_CLOSED`, `DISABLED`. Индексы `(city, status)` и `(region, status)` поддерживают поиск в checkout.

### `delivery_shipment`
Отправление по заказу. Хранит `order_id`, `customer_id`, optional `partner_id`, `delivery_method`, optional `pickup_point_id`, address/window fields, `expected_receive_at`, `current_status`, `external_shipment_id`, `idempotency_key`, `correlation_id`, `version`. Ограничения: unique `order_id`, unique `idempotency_key`, обязательность `pickup_point_id` для `PICKUP_POINT`, обязательность `address_line` для `HOME_DELIVERY` и `COURIER_DELIVERY`. Индексы поддерживают customer/partner history, очередь ПВЗ, внешний идентификатор и трассировку.

### `delivery_tracking_event`
Immutable timeline отправления. Каждое событие содержит `shipment_id`, `status`, `source_system`, `reason_code`, `occurred_at`, `correlation_id`, optional `external_event_id`. Unique `(source_system, external_event_id)` защищает от дублей внешних событий. События не редактируются, исправления создаются новым tracking event.

### `delivery_pickup_operation`
Операции ПВЗ: `ACCEPT`, `DELIVER`, `PARTIAL_DELIVER`, `NOT_COLLECTED`, `PROBLEM_REPORTED`. Содержит actor, reason, idempotency, correlation and occurredAt. Проверочный код получателя хранится только как `verification_code_hash`.

### `delivery_pickup_operation_item`
Состав частичной выдачи или возврата. Каждая строка ссылается на operation, order item, SKU, quantity и result: `DELIVERED`, `RETURN_TO_LOGISTICS`, `DAMAGED`, `MISSING`. `quantity` должен быть больше 0.

## Lifecycle
Основная цепочка: `ORDER_CONFIRMED` -> `ASSEMBLY_STARTED` -> `ASSEMBLY_COMPLETED` -> `SHIPPED` -> `IN_TRANSIT` -> `ARRIVED_AT_PICKUP_POINT` -> `READY_FOR_PICKUP` -> `DELIVERED`.

Разветвления: `PARTIALLY_DELIVERED`, `NOT_COLLECTED`, `RETURNED_TO_LOGISTICS`, `DELIVERY_PROBLEM`. Некорректные переходы не меняют shipment и возвращают `STR_MNEMO_DELIVERY_INVALID_STATUS_TRANSITION`.

## Интеграционные события
Модуль публикует `DeliveryShipmentCreated`, `DeliveryTrackingStatusChanged`, `PickupPointShipmentAccepted`, `PickupPointShipmentDelivered`, `PickupPointShipmentPartiallyDelivered`, `PickupPointShipmentNotCollected`, `DeliveryProblemReported`. Все события содержат orderId, shipmentId, actor/source, reasonCode, idempotencyKey и correlationId.
