# ER description. Feature #40. Module partner-benefits

## Назначение модуля
`partner-benefits` владеет пользовательским runtime-контуром программы преимуществ партнера: персональные выгоды, прогресс условий, referral link, reward shop, redemption, удерживающие офферы, support-view и audit событий. Модуль не владеет master-data товаров, цен, каталогов, MLM-структуры или административными правилами программ; эти данные читаются через contracts с `catalog`, `admin-pricing`, `admin-referral`, `bonus-wallet`, `admin-bonus`, `delivery`, `order` и `platform-experience`.

## Package ownership
- `api`: REST DTO, response/request records, enums, mnemonic error contracts.
- `domain`: JPA entities и repository interfaces для таблиц программы преимуществ.
- `db`: Liquibase XML changelog feature #40.
- `impl/controller`: Spring MVC controllers для `/api/partner-benefits`.
- `impl/service`: orchestration, eligibility, referral, reward redemption, support query.
- `impl/security`: partner/customer/support permission checks и active partner ownership.
- `impl/mapper`: MapStruct mappers entity -> DTO.
- `impl/event`: outbound events для checkout, wallet, fulfillment, notification и audit.
- `impl/client`: adapters к catalog/pricing/referral/wallet/order/delivery/notification контурам.

## Таблицы и ограничения
### partner_benefit_account
Аккаунт программы преимуществ для partner/user пары.
- `account_id uuid primary key`.
- `partner_id uuid not null`, `user_id uuid not null`.
- `partner_number varchar(80)` для поиска support-view.
- `account_status varchar(40) not null`: `ACTIVE`, `SUSPENDED`, `ARCHIVED`.
- `active_catalog_id varchar(80)` связывает состояние с текущим 21-дневным каталогом.
- `current_tier varchar(60)` хранит пользовательский tier программы, не MLM-ранг.
- `reward_balance`, `cashback_pending`, `cashback_confirmed numeric(19,2) not null default 0`.
- `version bigint not null` для optimistic locking.
- Unique: `(partner_id)`, `(user_id)`, `(partner_number)` where not null.
- Индексы: `account_status`, `active_catalog_id`, `updated_at`.

### partner_benefit_grant
Персональная выгода партнера.
- `benefit_id uuid primary key`, `account_id uuid not null`.
- `benefit_type varchar(40)`: `WELCOME`, `CASHBACK`, `FREE_DELIVERY`, `RECOMMENDATION_DISCOUNT`, `REWARD`, `RETENTION`.
- `benefit_status varchar(40)`: `AVAILABLE`, `PENDING`, `CONSUMED`, `EXPIRED`, `REVOKED`, `SUSPENDED`.
- `source_system`, `source_ref` связывают выгоду с order, pricing, referral, wallet или CRM.
- `catalog_id`, `starts_at`, `expires_at`, `consumed_at`.
- `amount_value numeric(19,2)`, `currency_code varchar(3)`.
- `application_target varchar(40)`: `CART`, `CHECKOUT`, `REWARD_SHOP`, `WALLET`, `NOTIFICATION_ONLY`.
- `mnemonic_code varchar(120)` содержит объяснение состояния для frontend localization.
- Индексы: `(account_id, benefit_status, expires_at)`, `(account_id, catalog_id)`, `(source_system, source_ref)`.

### partner_benefit_progress
Состояние выполнения условий выгоды.
- `progress_id uuid primary key`, `benefit_id uuid not null`.
- `condition_code varchar(80)`, `required_value numeric(19,2)`, `current_value numeric(19,2)`.
- `progress_status varchar(40)`: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`.
- `last_event_ref varchar(120)` связывает прогресс с order/referral/wallet event.
- Unique: `(benefit_id, condition_code)`.

### partner_referral_link
Персональная referral link партнера.
- `referral_link_id uuid primary key`, `account_id uuid not null`.
- `referral_code varchar(80) not null unique`.
- `campaign_id varchar(80)`, `source_channel varchar(40)`, `link_status varchar(40)`.
- `qr_payload_hash varchar(128)` хранит hash payload, не картинку QR.
- `starts_at`, `expires_at`, `rotated_at`.
- Индексы: `(account_id, link_status)`, `(campaign_id, link_status)`.

### partner_referral_event
События привлеченного пользователя.
- `referral_event_id uuid primary key`, `referral_link_id uuid not null`.
- `referred_user_id uuid`, `masked_contact varchar(160)` без раскрытия PII.
- `event_status varchar(40)`: `INVITED`, `REGISTERED`, `FIRST_ORDER_PLACED`, `QUALIFIED`, `REJECTED`, `REWARD_GRANTED`.
- `qualifying_action_ref varchar(120)`, `reward_benefit_id uuid`, `rejection_mnemonic varchar(120)`.
- `correlation_id varchar(120)` для расследования.
- Индексы: `(referral_link_id, occurred_at)`, `(event_status, occurred_at)`, `(correlation_id)`.

### partner_reward_catalog_item
Runtime-витрина reward shop, синхронизированная из admin/pricing/catalog контуров.
- `reward_id uuid primary key`, `reward_code varchar(80) unique`.
- `reward_status varchar(40)`, `catalog_id varchar(80)`, `title_i18n_key varchar(160)`.
- `cost_points numeric(19,2)`, `region_code varchar(40)`, `warehouse_id uuid`, `available_quantity integer`.
- `starts_at`, `expires_at`.
- Индексы: `(catalog_id, reward_status)`, `(region_code, reward_status)`, `(warehouse_id)`.

### partner_reward_redemption
Заявка на получение награды.
- `redemption_id uuid primary key`, `account_id uuid not null`, `reward_id uuid not null`.
- `redemption_status varchar(40)`: `RESERVED`, `FULFILLMENT_PENDING`, `FULFILLED`, `CANCELLED`, `REJECTED`.
- `cost_points numeric(19,2)`.
- `fulfillment_ref varchar(120)` ссылка на delivery/fulfillment задачу.
- `idempotency_key varchar(120) not null unique`.
- `version bigint not null` для защиты от двойного списания.
- Индексы: `(account_id, created_at)`, `(reward_id, redemption_status)`, `(correlation_id)`.

### partner_retention_offer
Персональный удерживающий offer.
- `retention_offer_id uuid primary key`, `account_id uuid not null`.
- `offer_code`, `audience_code`, `risk_reason_code`, `offer_status`, `catalog_id`.
- `priority integer`, `stackability_group varchar(80)`, `starts_at`, `expires_at`.
- Индексы: `(account_id, offer_status, expires_at)`, `(catalog_id, audience_code)`.

### partner_benefit_audit_event
Immutable audit trail по выгодам, referral, redemption и support actions.
- `audit_event_id uuid primary key`.
- `account_id`, `subject_ref`, `actor_user_id`, `actor_role`, `action_code`, `reason_code`, `source_system`.
- `old_value_json jsonb`, `new_value_json jsonb` без секретов и лишней PII.
- `correlation_id`, `occurred_at`.
- Индексы: `(account_id, occurred_at)`, `(action_code, occurred_at)`, `(correlation_id)`, `(actor_user_id, occurred_at)`.

## Версионная база
Фича стартует 28.04.2026. Для новых частей используется latest-stable baseline на дату старта: Java 25, Spring Boot 4.0.6, Hibernate, MapStruct, Lombok, PostgreSQL jsonb, Liquibase XML. Если runtime-монолит остается на существующей версии из-за совместимости сборки, это фиксируется как compatibility constraint в architecture/module artifacts, но формат changelog остается XML.
