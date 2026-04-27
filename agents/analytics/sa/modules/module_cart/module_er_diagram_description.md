# Module cart. Полное описание ER-диаграммы

## Назначение модели
Модуль `cart` хранит активные корзины пользователей Best Ori Gin, строки корзины, примененные промо-предложения, последний расчет итогов и audit trail действий. Модель отделяет основную корзину (`MAIN`) от партнерского дозаказа (`SUPPLEMENTARY`), чтобы строки, итоги, промо-состояния и validation не смешивались между разными типами будущего заказа.

## Таблица `cart`
- `cart_id uuid` - первичный ключ корзины.
- `owner_user_id varchar(80)` - идентификатор владельца корзины из текущей security-модели.
- `cart_type varchar(32)` - тип корзины: `MAIN` или `SUPPLEMENTARY`.
- `campaign_id varchar(80)` - текущая трехнедельная кампания каталога, по которой считаются цены и офферы.
- `role_segment varchar(32)` - сегмент роли: `CUSTOMER`, `PARTNER`, `SUPPORT`.
- `partner_context_id varchar(80)` - партнерский context для бонусной и комиссионной логики; nullable для обычного покупателя.
- `status varchar(32)` - `ACTIVE`, `BLOCKED`, `READY_FOR_CHECKOUT`, `ARCHIVED`.
- `currency varchar(3)` - валюта расчета.
- `version integer` - optimistic locking/version для защиты от конфликтов пересчета.
- `created_at timestamptz`, `updated_at timestamptz` - аудит времени.

Рекомендуемые ограничения: уникальность активной корзины по `(owner_user_id, cart_type, campaign_id)`, `cart_type in ('MAIN','SUPPLEMENTARY')`, `version >= 0`.

## Таблица `cart_line`
- `cart_line_id uuid` - первичный ключ строки.
- `cart_id uuid` - ссылка на `cart.cart_id`.
- `product_code varchar(64)` - код товара из module `catalog`; валидируется через catalog contract.
- `source varchar(32)` - источник добавления: `SEARCH_RESULT`, `PRODUCT_CARD`, `SHOPPING_OFFER`, `SUPPLEMENTARY_OFFER`.
- `quantity integer` - количество, минимум 1.
- `unit_price numeric(12,2)` - базовая цена на момент последнего пересчета.
- `promo_unit_price numeric(12,2)` - промо-цена, nullable.
- `line_total numeric(12,2)` - итог строки после количества и скидок.
- `availability_status varchar(32)` - `AVAILABLE`, `LOW_STOCK`, `RESERVED`, `PARTIALLY_AVAILABLE`, `UNAVAILABLE`, `REMOVED_FROM_CAMPAIGN`.
- `blocking_message_code varchar(120)` - `STR_MNEMO_*` причина блокировки, nullable.
- `reserved_quantity integer` - количество в резерве, если резерв применим.
- `max_allowed_quantity integer` - максимальное допустимое количество по складу, кампании и роли.
- `created_at timestamptz`, `updated_at timestamptz` - аудит времени.

Рекомендуемые ограничения: `quantity > 0`, уникальность `(cart_id, product_code, source)` для недублирования одинаковой строки, индекс по `(cart_id)`, индекс по `(product_code)`.

## Таблица `cart_applied_offer`
- `applied_offer_id uuid` - первичный ключ применения оффера.
- `cart_id uuid` - ссылка на корзину.
- `offer_id varchar(80)` - идентификатор промо-правила кампании.
- `offer_type varchar(32)` - `BUNDLE`, `GIFT`, `CROSS_SELL`, `UPSELL`, `RETENTION`, `FREE_DELIVERY_PRODUCT`.
- `status varchar(32)` - `APPLIED`, `PENDING_CONDITION`, `UNAVAILABLE`, `REMOVED`.
- `benefit_amount numeric(12,2)` - расчетная выгода оффера.
- `gift_product_code varchar(64)` - код подарка, nullable.
- `message_code varchar(120)` - `STR_MNEMO_*` статус или причина недоступности.
- `applied_at timestamptz` - момент применения.

Рекомендуемые ограничения: индекс по `(cart_id, offer_id)`, запрет применения оффера к неподходящему `cart_type` на уровне service validation.

## Таблица `cart_totals_snapshot`
- `cart_id uuid` - первичный ключ и ссылка на корзину.
- `subtotal numeric(12,2)` - сумма строк до скидок.
- `discount_total numeric(12,2)` - сумма скидок.
- `benefit_total numeric(12,2)` - суммарная выгода промо и подарков в денежной оценке.
- `shipping_threshold_remaining numeric(12,2)` - остаток до условия бесплатной доставки, nullable по смыслу.
- `grand_total numeric(12,2)` - итоговая сумма.
- `recalculated_at timestamptz` - время последнего расчета.

Snapshot хранит последний согласованный расчет, чтобы frontend мог повторить действие после временной ошибки без потери уже подтвержденного состояния.

## Таблица `cart_audit_event`
- `audit_event_id uuid` - первичный ключ audit event.
- `cart_id uuid` - ссылка на корзину.
- `actor_user_id varchar(80)` - пользователь или сотрудник поддержки, выполнивший действие.
- `actor_role varchar(64)` - роль актера.
- `action varchar(64)` - `CREATE_CART`, `ADD_LINE`, `CHANGE_QUANTITY`, `REMOVE_LINE`, `APPLY_OFFER`, `VALIDATION_FAILED`, `SUPPORT_VIEW`, `SUPPORT_CHANGE`.
- `request_id varchar(120)` - correlation/idempotency key.
- `details_json text` - структурированные технические детали без лишних персональных данных.
- `created_at timestamptz` - время события.

## Связи и ownership
- `cart` владеет строками, примененными офферами, totals snapshot и audit event.
- `product_code` связан с module `catalog` контрактно, без обязательного физического FK между bounded contexts.
- Модуль `cart` не рассчитывает бонусы compensation plan и не создает order; он готовит валидный состав для будущего checkout/order модуля.

## Версионная база
Новые runtime-технологии не вводятся. Модель должна быть реализована в существующем Spring Boot monolith с Java/Maven baseline проекта, Hibernate/JPA и Liquibase XML changelog в пакете `db` модуля `cart`.
