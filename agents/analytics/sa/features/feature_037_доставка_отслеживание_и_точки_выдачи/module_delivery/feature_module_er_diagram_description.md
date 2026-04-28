# Feature 037 module_delivery ER description

## Версионный baseline
На дату старта задачи 28.04.2026 для реализации module_delivery используется актуальный стабильный baseline репозитория: Java 21, Spring Boot 3.x, Maven 3.9.x, Hibernate 6.x, Liquibase 4.x, MapStruct 1.6.x, Lombok 1.18.x, PostgreSQL 16.x. Если центральный backend baseline репозитория уже зафиксирован более новой совместимой версией, реализация должна использовать его без локального дублирования версий.

## Backend package ownership
- `com.bestorigin.monolith.delivery.api` содержит DTO внешнего REST API: выбор доставки, карточки ПВЗ, tracking timeline, команды приемки/выдачи и integration events.
- `com.bestorigin.monolith.delivery.domain` содержит только JPA entities и repository interfaces.
- `com.bestorigin.monolith.delivery.db` содержит только Liquibase XML changelog-и module_delivery.
- `com.bestorigin.monolith.delivery.impl` содержит controllers, services, mappers, validators, security, exception handling, event publishers и integration clients в role-specific subpackages.

## Таблицы и ограничения

### `delivery_pickup_point`
Справочник точек выдачи, доступных checkout и кабинету владельца ПВЗ.

Поля:
- `id uuid primary key` - внутренний идентификатор ПВЗ.
- `code varchar(64) not null unique` - стабильный код точки выдачи.
- `name varchar(255) not null` - отображаемое имя точки; для frontend используется как бизнес-данные справочника.
- `address_line varchar(500) not null`, `city varchar(128) not null`, `region varchar(128) not null` - адресная часть для поиска и карточки ПВЗ.
- `contact_phone varchar(64)` - контактный телефон точки.
- `work_schedule varchar(500) not null` - человекочитаемый график точки как справочные данные.
- `storage_limit_days integer not null` - срок хранения заказа, минимум 1.
- `max_orders_capacity integer not null` - операционный лимит одновременно хранимых отправлений.
- `status varchar(32) not null` - `ACTIVE`, `TEMPORARILY_CLOSED`, `DISABLED`.
- `version bigint not null` - optimistic locking.
- `created_at timestamptz not null`, `updated_at timestamptz not null`.

Индексы: unique `code`, btree `(city, status)`, btree `(region, status)`.

### `delivery_shipment`
Основная сущность отправления по заказу.

Поля:
- `id uuid primary key`.
- `order_id uuid not null` - ссылка на заказ из order/adminorder контуров.
- `customer_id uuid not null` - владелец пользовательского заказа.
- `partner_id uuid` - партнерский контекст, если заказ партнерский или офлайн-клиентский.
- `delivery_method varchar(32) not null` - `HOME_DELIVERY`, `COURIER_DELIVERY`, `PICKUP_POINT`.
- `pickup_point_id uuid null references delivery_pickup_point(id)` - обязательна только для `PICKUP_POINT`.
- `address_line varchar(500)` - обязательна для домашней и курьерской доставки.
- `delivery_window_start timestamptz`, `delivery_window_end timestamptz` - выбранный интервал доставки.
- `expected_receive_at timestamptz` - расчетная дата получения или готовности к выдаче.
- `current_status varchar(48) not null` - текущий status из controlled lifecycle.
- `external_shipment_id varchar(128)` - идентификатор внешней доставки.
- `idempotency_key varchar(128) not null` - защита create/update команд от дублей.
- `correlation_id varchar(128) not null` - трассировка между order, WMS, delivery и service.
- `version bigint not null`, `created_at timestamptz not null`, `updated_at timestamptz not null`.

Ограничения: unique `(order_id)`, unique `(idempotency_key)`, partial check для обязательности `pickup_point_id` или `address_line` по delivery_method. Индексы: `(customer_id, created_at)`, `(partner_id, created_at)`, `(pickup_point_id, current_status)`, `(external_shipment_id)`, `(correlation_id)`.

### `delivery_tracking_event`
Неизменяемая лента статусов отправления.

Поля:
- `id uuid primary key`.
- `shipment_id uuid not null references delivery_shipment(id)`.
- `status varchar(48) not null` - статус lifecycle.
- `source_system varchar(64) not null` - `BEST_ORI_GIN`, `WMS_1C`, `ASSEMBLY`, `DELIVERY_PROVIDER`, `PICKUP_POINT`.
- `reason_code varchar(128)` - machine-readable причина, например `STR_MNEMO_DELIVERY_DELAYED`.
- `occurred_at timestamptz not null` - фактическое время события.
- `correlation_id varchar(128) not null`.
- `external_event_id varchar(128)` - idempotency key внешнего события.
- `created_at timestamptz not null`.

Ограничения: unique `(source_system, external_event_id)` для непустого external_event_id; индексы `(shipment_id, occurred_at)`, `(status, occurred_at)`, `(correlation_id)`.

### `delivery_pickup_operation`
Операции владельца ПВЗ: приемка, выдача, частичная выдача, невыкуп, проблемный кейс.

Поля:
- `id uuid primary key`.
- `shipment_id uuid not null references delivery_shipment(id)`.
- `pickup_point_id uuid not null references delivery_pickup_point(id)`.
- `operation_type varchar(48) not null` - `ACCEPT`, `DELIVER`, `PARTIAL_DELIVER`, `NOT_COLLECTED`, `PROBLEM_REPORTED`.
- `actor_user_id uuid not null` - пользователь-владелец/оператор ПВЗ.
- `verification_code_hash varchar(255)` - хэш проверочного кода получателя, исходный код не хранится.
- `reason_code varchar(128)` - machine-readable причина.
- `idempotency_key varchar(128) not null`.
- `correlation_id varchar(128) not null`.
- `occurred_at timestamptz not null`, `created_at timestamptz not null`.

Ограничения: unique `(idempotency_key)`, индексы `(pickup_point_id, occurred_at)`, `(shipment_id, operation_type)`, `(actor_user_id, occurred_at)`.

### `delivery_pickup_operation_item`
Состав частичной выдачи или возврата.

Поля:
- `id uuid primary key`.
- `pickup_operation_id uuid not null references delivery_pickup_operation(id)`.
- `order_item_id uuid not null`.
- `sku varchar(128) not null`.
- `quantity numeric(19,3) not null`.
- `item_result varchar(32) not null` - `DELIVERED`, `RETURN_TO_LOGISTICS`, `DAMAGED`, `MISSING`.

Ограничения: check `quantity > 0`, unique `(pickup_operation_id, order_item_id, item_result)`.

## Lifecycle и интеграции
Допустимые статусы shipment: `ORDER_CONFIRMED`, `ASSEMBLY_STARTED`, `ASSEMBLY_COMPLETED`, `SHIPPED`, `IN_TRANSIT`, `ARRIVED_AT_PICKUP_POINT`, `READY_FOR_PICKUP`, `DELIVERED`, `PARTIALLY_DELIVERED`, `NOT_COLLECTED`, `RETURNED_TO_LOGISTICS`, `DELIVERY_PROBLEM`.

module_delivery публикует доменные события для order, service, WMS/1C, bonus и admin platform KPI: `DeliveryShipmentCreated`, `DeliveryTrackingStatusChanged`, `PickupPointShipmentAccepted`, `PickupPointShipmentDelivered`, `PickupPointShipmentPartiallyDelivered`, `PickupPointShipmentNotCollected`, `DeliveryProblemReported`.
