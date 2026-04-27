# Feature 010. Module order. Описание ER-диаграммы

## Назначение модуля
`module_order` отвечает за оформление основной корзины и supplementary order после успешной validation корзины. Модуль владеет checkout draft, снимком строк, получателем, адресом, доставкой, оплатой, примененными выгодами, резервами, итоговым заказом и audit trail. Модуль не хранит каталог товаров, настройки промо, учетные записи и фактический WMS-остаток; эти данные приходят через контракты соседних модулей и внешних интеграций.

## Таблица `order_checkout_draft`
Черновик оформления заказа.

Поля:
- `id uuid PK` — внутренний идентификатор checkout draft.
- `owner_user_id varchar(64) not null` — владелец checkout.
- `checkout_type varchar(32) not null` — `MAIN` или `SUPPLEMENTARY`.
- `cart_id varchar(64) not null` — идентификатор валидированной корзины-источника.
- `campaign_id varchar(64) not null` — кампания каталога.
- `status varchar(32) not null` — `DRAFT`, `VALIDATION_REQUIRED`, `READY_TO_CONFIRM`, `CONFIRMED`, `BLOCKED`, `EXPIRED`.
- `version bigint not null` — optimistic locking для stale checkout.
- `partner_context_id varchar(64)` — партнерский контекст для supplementary/VIP/super order.
- `vip_mode boolean` — признак VIP-режима.
- `super_order_mode boolean` — признак super order.
- `subtotal_amount numeric(19,2)`, `delivery_amount numeric(19,2)`, `discount_amount numeric(19,2)`, `wallet_amount numeric(19,2)`, `cashback_amount numeric(19,2)`, `grand_total_amount numeric(19,2)` — денежные итоги draft.
- `created_at timestamptz not null`, `updated_at timestamptz not null`.

Ограничения и индексы:
- `checkout_type in ('MAIN','SUPPLEMENTARY')`.
- `version >= 0`.
- Индекс `idx_order_checkout_owner_status(owner_user_id, status)`.
- Индекс `idx_order_checkout_cart_type(cart_id, checkout_type)`.

## Таблица `order_checkout_item`
Снимок строк checkout на момент оформления.

Поля:
- `id uuid PK`.
- `checkout_id uuid FK -> order_checkout_draft.id`.
- `product_code varchar(64) not null`.
- `product_name_snapshot varchar(255) not null`.
- `quantity int not null`.
- `unit_price numeric(19,2) not null`.
- `total_price numeric(19,2) not null`.
- `availability_status varchar(32) not null`.
- `reserve_status varchar(32)`.
- `blocking_reason_mnemo varchar(128)`.

Ограничения и индексы:
- `quantity > 0`.
- Уникальность `uk_order_checkout_item_product(checkout_id, product_code)`.
- Индекс `idx_order_checkout_item_checkout(checkout_id)`.

## Таблица `order_checkout_recipient`
Получатель и контактные данные.

Поля:
- `checkout_id uuid PK FK -> order_checkout_draft.id`.
- `recipient_type varchar(32) not null` — `SELF` или `OTHER`.
- `full_name varchar(255) not null`.
- `phone varchar(64) not null`.
- `email varchar(255)`.

Ограничения:
- `recipient_type in ('SELF','OTHER')`.
- Формальная валидация телефона и email выполняется в `impl/validator`; БД хранит нормализованные значения.

## Таблица `order_checkout_address`
Адрес доставки или пункт выдачи.

Поля:
- `checkout_id uuid PK FK -> order_checkout_draft.id`.
- `address_id varchar(64)` — ссылка на сохраненный адрес пользователя.
- `pickup_point_id varchar(64)` — ссылка на пункт выдачи.
- `delivery_target_type varchar(32) not null` — `ADDRESS` или `PICKUP_POINT`.
- `country`, `region`, `city`, `street`, `house`, `apartment`, `postal_code` — снимок адреса.

Ограничения:
- Для `ADDRESS` обязателен набор адресных полей.
- Для `PICKUP_POINT` обязателен `pickup_point_id`.
- Принадлежность `address_id` пользователю проверяется сервисом, а не доверяется входному DTO.

## Таблица `order_checkout_delivery`
Выбранный способ доставки.

Поля:
- `checkout_id uuid PK FK -> order_checkout_draft.id`.
- `delivery_method_code varchar(64) not null`.
- `delivery_method_name varchar(255) not null`.
- `delivery_price numeric(19,2)`.
- `estimated_interval varchar(128)`.
- `unavailable_reason_mnemo varchar(128)`.

Ограничения:
- `delivery_price >= 0`.
- Недоступный способ доставки не может быть использован для подтверждения заказа.

## Таблица `order_checkout_payment`
Платежный выбор и платежная сессия.

Поля:
- `checkout_id uuid PK FK -> order_checkout_draft.id`.
- `payment_method_code varchar(64) not null`.
- `payment_session_id varchar(128)`.
- `payment_status varchar(32)` — `PENDING`, `PAID`, `FAILED`, `EXPIRED`, `CANCELLED`.
- `idempotency_key varchar(128)`.
- `amount_to_pay numeric(19,2)`.

Ограничения и индексы:
- Уникальный индекс `uk_order_payment_idempotency(idempotency_key)` для непустых значений.
- `amount_to_pay >= 0`.
- Полные платежные реквизиты не хранятся.

## Таблица `order_checkout_benefit`
Примененные выгоды.

Поля:
- `id uuid PK`.
- `checkout_id uuid FK -> order_checkout_draft.id`.
- `benefit_type varchar(32) not null` — `WALLET`, `CASHBACK`, `CATALOG_DISCOUNT`, `RETENTION`, `VIP`, `SUPER_ORDER`.
- `benefit_code varchar(64) not null`.
- `applied_amount numeric(19,2)`.
- `status varchar(32)` — `APPLIED`, `REJECTED`, `LIMITED`.
- `reason_mnemo varchar(128)`.

Ограничения:
- Уникальность `uk_order_checkout_benefit(checkout_id, benefit_type, benefit_code)`.
- `applied_amount >= 0`.

## Таблица `order_reservation`
Результат резервирования.

Поля:
- `id uuid PK`.
- `checkout_id uuid FK -> order_checkout_draft.id`.
- `checkout_item_id uuid FK -> order_checkout_item.id`.
- `reservation_status varchar(32) not null` — `RESERVED`, `PARTIAL`, `FAILED`, `RELEASED`.
- `reserved_quantity int`.
- `available_quantity int`.
- `external_reservation_id varchar(128)`.
- `reason_mnemo varchar(128)`.
- `created_at timestamptz`.

Ограничения:
- `reserved_quantity >= 0`, `available_quantity >= 0`.
- Индекс `idx_order_reservation_checkout(checkout_id)`.

## Таблица `order_order`
Зафиксированный заказ.

Поля:
- `id uuid PK`.
- `order_number varchar(64) unique not null`.
- `checkout_id uuid FK -> order_checkout_draft.id`.
- `owner_user_id varchar(64) not null`.
- `order_type varchar(32) not null` — `MAIN` или `SUPPLEMENTARY`.
- `campaign_id varchar(64) not null`.
- `status varchar(32) not null` — `CREATED`, `PAYMENT_PENDING`, `PAID`, `ASSEMBLY_PENDING`, `CANCELLED`.
- `payment_status varchar(32)`.
- `delivery_status varchar(32)`.
- `grand_total_amount numeric(19,2)`.
- `created_at timestamptz not null`.

Ограничения и индексы:
- Уникальность `order_number`.
- Уникальность `checkout_id`, чтобы один checkout не создавал несколько заказов.
- Индекс `idx_order_owner_created(owner_user_id, created_at)`.

## Таблица `order_audit_event`
Audit trail checkout и заказа.

Поля:
- `id uuid PK`.
- `aggregate_id uuid not null` — checkout или order aggregate.
- `event_type varchar(64) not null`.
- `actor_user_id varchar(64) not null`.
- `actor_role varchar(64)`.
- `mnemo_code varchar(128)`.
- `created_at timestamptz`.

События:
- `CHECKOUT_STARTED`, `RECIPIENT_UPDATED`, `ADDRESS_UPDATED`, `DELIVERY_SELECTED`, `PAYMENT_SELECTED`, `BENEFIT_APPLIED`, `VALIDATION_FAILED`, `ORDER_CONFIRMED`, `RESERVATION_FAILED`, `PAYMENT_STATUS_CHANGED`, `SUPPORT_VIEWED`.

## Backend package ownership
- `api`: DTO checkout/order endpoints, enums contract, request/response classes.
- `domain`: JPA entities и repository interfaces для таблиц `order_*`.
- `db`: Liquibase XML changelog feature #10.
- `impl/controller`: Spring MVC controller endpoints `/api/order/**`.
- `impl/service`: orchestration checkout, validation, reservation, payment session, order creation.
- `impl/validator`: валидация контактов, адреса, доставки, benefit-лимитов и stale version.
- `impl/client`: adapters к cart, WMS/1C, delivery и payment contracts.
- `impl/event`: публикация order/payment/audit events.

## Версионная база
Новые технологии не вводятся. Используется текущий Spring Boot monolith, Java/Maven baseline, Hibernate/JPA, Liquibase XML, MapStruct/Lombok при наличии в baseline, PostgreSQL-compatible schema и runtime Swagger через springdoc-openapi monolith grouping.
