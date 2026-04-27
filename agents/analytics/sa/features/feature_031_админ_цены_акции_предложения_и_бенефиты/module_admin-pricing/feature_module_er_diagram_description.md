# ER description. Feature 031 / module admin-pricing

## Назначение модуля
`admin-pricing` отвечает за административные данные pricing/promotions: price lists, базовые цены, promo prices, segment rules, promotions, shopping offers, bundle items, gift rules, threshold benefits, import/export jobs и immutable audit trail. Модуль является владельцем коммерческих правил, которые затем потребляют публичный каталог, цифровой каталог, корзина, checkout и партнерские офлайн-заказы.

## Package ownership
Backend module должен соблюдать обязательную структуру:
- `com.bestorigin.monolith.adminpricing.api` — REST DTO, request/response contracts, enum contracts и module-facing API types.
- `com.bestorigin.monolith.adminpricing.domain` — JPA entities и repository interfaces только.
- `com.bestorigin.monolith.adminpricing.db` — Liquibase XML changelog ownership и changelog registration metadata.
- `com.bestorigin.monolith.adminpricing.impl` — controller, service, validator, mapper, exception, config, security и orchestration code в role-specific подпакетах.

## Таблицы и поля

### admin_pricing_price_list
Главная агрегатная сущность прайс-листа.
- `price_list_id uuid PK` — технический идентификатор.
- `price_list_code varchar(80) UK` — бизнес-код, уникален среди неархивных прайс-листов.
- `name varchar(255)` — административное название.
- `campaign_id varchar(80)` — связь с 21-дневной кампанией feature #030.
- `country_code varchar(2)`, `currency_code varchar(3)`, `channel_code varchar(40)` — коммерческий контекст.
- `status varchar(32)` — `DRAFT`, `READY_FOR_REVIEW`, `ACTIVE`, `PAUSED`, `ARCHIVED`.
- `active_from`, `active_to timestamptz` — окно действия.
- `version bigint` — optimistic locking.
- `created_by uuid`, `created_at`, `updated_at` — audit metadata.

### admin_pricing_price
Базовая цена SKU/productId внутри price list.
- `price_id uuid PK`.
- `price_list_id uuid FK` — ссылка на `admin_pricing_price_list`.
- `product_id varchar(80)`, `sku varchar(80)` — ссылка на published PIM-товар feature #29.
- `base_price numeric(19,4)` — базовая цена в валюте price list.
- `tax_mode varchar(32)` — налоговый режим.
- `status varchar(32)` — жизненный цикл цены.
- `active_from`, `active_to timestamptz` — окно цены.
- `version bigint` — optimistic locking.

### admin_pricing_promo_price
Временная или сегментная промо-цена.
- `promo_price_id uuid PK`.
- `price_list_id uuid FK`.
- `sku varchar(80)`.
- `promo_price numeric(19,4)`.
- `discount_type varchar(32)` — `FIXED_PRICE`, `PERCENT`, `AMOUNT`.
- `discount_value numeric(19,4)`.
- `segment_code varchar(80)`, `reason_code varchar(80)`.
- `status`, `active_from`, `active_to`.

### admin_pricing_segment_rule
Правило применимости цены или промо для роли/сегмента.
- `rule_id uuid PK`.
- `campaign_id varchar(80)`.
- `segment_code varchar(80)`, `role_code varchar(80)`.
- `partner_level`, `customer_type`, `region_code`.
- `priority integer`.
- `status`, `active_from`, `active_to`.

### admin_pricing_promotion
Акция, объединяющая offers, gifts и benefits.
- `promotion_id uuid PK`.
- `promotion_code varchar(80) UK`.
- `name_key varchar(160)` — i18n key, backend не хранит hardcoded UI-текст.
- `campaign_id varchar(80)`, `audience varchar(80)`, `channel_code varchar(40)`.
- `status`, `active_from`, `active_to`, `version`.

### admin_pricing_shopping_offer
Коммерческое предложение для корзины и checkout.
- `offer_id uuid PK`.
- `promotion_id uuid FK`.
- `offer_code varchar(80) UK`.
- `offer_type varchar(40)` — `CROSS_SELL`, `UPSELL`, `BUNDLE`, `GIFT`, `WELCOME_BENEFIT`, `RETENTION_BENEFIT`, `THRESHOLD_BENEFIT`.
- `title_key`, `description_key` — i18n keys.
- `priority integer`, `stackable boolean`, `mutually_exclusive_group varchar(80)`.
- `status varchar(32)`.

### admin_pricing_offer_item
Позиции bundle/cross-sell/up-sell offer.
- `offer_item_id uuid PK`.
- `offer_id uuid FK`.
- `sku varchar(80)`.
- `quantity integer`.
- `item_role varchar(32)` — `REQUIRED`, `OPTIONAL`, `RECOMMENDED`.
- `sort_order integer`.

### admin_pricing_gift_rule
Подарочная механика.
- `gift_rule_id uuid PK`.
- `promotion_id uuid FK`.
- `gift_sku varchar(80)` — published PIM SKU подарка.
- `trigger_type varchar(40)` — `CART_AMOUNT`, `SKU_PRESENT`, `CATEGORY_AMOUNT`, `SEGMENT`.
- `threshold_amount numeric(19,4)`, `trigger_sku varchar(80)`.
- `max_gift_quantity integer`.
- `status`, `active_from`, `active_to`.

### admin_pricing_threshold_benefit
Пороговая выгода и progress strategy.
- `benefit_id uuid PK`.
- `promotion_id uuid FK`.
- `benefit_type varchar(40)`.
- `threshold_amount numeric(19,4)`, `threshold_quantity integer`.
- `progress_strategy varchar(80)`.
- `reward_code varchar(80)`.
- `status varchar(32)`.

### admin_pricing_import_job
Массовый импорт/экспорт pricing data.
- `job_id uuid PK`.
- `source_file_name varchar(255)`.
- `idempotency_key varchar(120) UK`.
- `dry_run boolean`.
- `status varchar(32)` — `UPLOADED`, `VALIDATED`, `APPLIED`, `FAILED`.
- `row_count`, `error_count integer`.
- `checksum varchar(128)`.
- `created_by uuid`, `created_at timestamptz`.

### admin_pricing_audit_event
Immutable audit trail.
- `audit_event_id uuid PK`.
- `actor_user_id uuid`.
- `action_code varchar(80)`.
- `entity_type varchar(80)`, `entity_id varchar(80)`.
- `old_value jsonb`, `new_value jsonb`.
- `reason_code varchar(80)`.
- `idempotency_key varchar(120)`.
- `correlation_id varchar(120)`.
- `occurred_at timestamptz`.

## Ограничения и индексы
- Unique: `price_list_code` для активных/неархивных price lists.
- Unique: `promotion_code` для активных/неархивных promotions.
- Unique: `offer_code` для активных/неархивных offers.
- Unique: `idempotency_key` в import jobs.
- Index: `price_list_id + sku + active_from + active_to` для проверки пересечений цены.
- Index: `campaign_id + status + active_from + active_to` для поиска активных правил кампании.
- Index: `promotion_id + status + priority` для выдачи offers.
- Index: `actor_user_id`, `entity_type + entity_id`, `correlation_id`, `occurred_at` для audit search.

## Валидации
- Нельзя активировать price или gift rule, если SKU отсутствует или не опубликован в PIM.
- Нельзя публиковать price list с пересекающимися активными периодами цены для одного SKU и сегмента.
- Валюта `base_price` и `promo_price` наследуется от price list.
- Promo price ниже min margin threshold требует approval reason.
- Offer compatibility должна детерминированно выбирать примененные и отклоненные offers.
- Backend возвращает предопределенные сообщения только mnemonic-кодами `STR_MNEMO_ADMIN_PRICING_*`.

## Версионная база
Артефакт рассчитан на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Hibernate/JPA, Liquibase XML, PostgreSQL JSONB и Springdoc OpenAPI runtime generation для monolith module `admin-pricing`.
