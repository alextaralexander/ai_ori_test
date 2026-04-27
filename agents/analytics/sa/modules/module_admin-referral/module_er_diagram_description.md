# Module ER diagram description. Module admin-referral

## Назначение модели
Модуль `admin-referral` хранит административные настройки marketing/referral funnel: landing variants, версии лендингов, registration funnels, referral codes, attribution policy, conversion events и immutable audit trail. Модель обслуживает feature #28 и интегрируется с публичной регистрацией feature #008, benefit-лендингами feature #007, RBAC feature #26 и analytics layer feature #25.

## Таблицы

### `admin_ref_landing_variant`
Корневая таблица административного лендинга.
- `landing_id uuid PK` - технический идентификатор лендинга.
- `landing_type varchar(32)` - `BEAUTY`, `BUSINESS`, `CUSTOMER_REFERRAL`.
- `locale_code varchar(8)` - локаль render model, например `ru` или `en`.
- `slug varchar(180)` - публичный slug; уникален в сочетании с `locale_code` и пересекающимся active window для активных записей.
- `name varchar(240)` - внутреннее имя для админки.
- `campaign_code varchar(80)` - код кампании для отчетности и registration context.
- `status varchar(32)` - `DRAFT`, `IN_REVIEW`, `ACTIVE`, `PAUSED`, `SCHEDULED`, `ARCHIVED`.
- `owner_user_id uuid` - владелец настройки.
- `active_version_id uuid FK` - активная версия.
- `active_from`, `active_to timestamptz` - окно активности.
- `created_at`, `updated_at timestamptz` - технические даты.
- `version_number bigint` - optimistic locking.

Индексы: unique active slug index по `slug`, `locale_code`, active-window constraint; btree по `status`, `landing_type`, `campaign_code`, `active_from`, `active_to`.

### `admin_ref_landing_version`
Снимок версии лендинга.
- `version_id uuid PK`.
- `landing_id uuid FK`.
- `version_number int`.
- `status varchar(32)`.
- `hero_json jsonb`, `seo_json jsonb`, `campaign_context_json jsonb` - структурированные данные без произвольного HTML.
- `created_by_user_id`, `activated_by_user_id uuid`.
- `created_at`, `activated_at timestamptz`.

Ограничения: unique `landing_id + version_number`; `campaign_context_json` не должен содержать секреты или персональные данные кандидата.

### `admin_ref_landing_block`
Структурированные блоки версии лендинга.
- `block_id uuid PK`.
- `version_id uuid FK`.
- `block_type varchar(40)` - `HERO`, `BENEFIT`, `TESTIMONIAL`, `CTA`, `FAQ`, `LEGAL_NOTICE`.
- `sort_order int`.
- `payload_json jsonb`.
- `created_at timestamptz`.

Ограничения: unique `version_id + sort_order`; при активации обязательны `HERO`, хотя бы один `BENEFIT`, `CTA` и `LEGAL_NOTICE`.

### `admin_ref_registration_funnel`
Корневая таблица registration funnel.
- `funnel_id uuid PK`.
- `funnel_code varchar(100)` - стабильный код funnel.
- `scenario varchar(40)` - `BEAUTY_PARTNER`, `BUSINESS_PARTNER`, `CUSTOMER_REFERRAL`.
- `status varchar(32)`.
- `active_version_id uuid FK`.
- `created_at`, `updated_at timestamptz`.
- `version_number bigint`.

Индексы: unique `funnel_code`; btree `scenario`, `status`.

### `admin_ref_funnel_version`
Версия funnel rules.
- `funnel_version_id uuid PK`.
- `funnel_id uuid FK`.
- `version_number int`.
- `steps_json jsonb` - шаги registration flow.
- `consent_codes_json jsonb` - обязательные юридические согласия.
- `validation_rules_json jsonb` - правила валидации.
- `default_context_json jsonb` - default landing/campaign context.
- `created_by_user_id`, `activated_by_user_id uuid`.
- `created_at`, `activated_at timestamptz`.

Ограничения: unique `funnel_id + version_number`; активная версия должна содержать обязательные consent codes.

### `admin_ref_referral_code`
Административные referral/invite codes.
- `referral_code_id uuid PK`.
- `public_code varchar(80)` - код, видимый в ссылках.
- `code_type varchar(40)` - `PERSONAL_SINGLE_USE`, `PERSONAL_MULTI_USE`, `CAMPAIGN_SINGLE_USE`, `CAMPAIGN_MULTI_USE`.
- `status varchar(32)` - `DRAFT`, `ACTIVE`, `USED`, `EXPIRED`, `REVOKED`, `LOCKED`.
- `campaign_code varchar(80)`.
- `owner_partner_id uuid` - партнер-спонсор или null для campaign code.
- `landing_type varchar(32)`.
- `active_from`, `active_to timestamptz`.
- `max_usage_count int`, `usage_count int`.
- `constraints_json jsonb` - регион, канал, сценарий, лимиты.
- `idempotency_key varchar(120)` - защита генерации от дублей.
- `created_by_user_id uuid`.
- `created_at`, `updated_at timestamptz`.

Индексы: unique `public_code`; unique nullable `idempotency_key`; btree `status`, `campaign_code`, `owner_partner_id`, `active_to`. `usage_count <= max_usage_count`, если лимит задан.

### `admin_ref_attribution_policy`
Активная policy выбора sponsor attribution.
- `policy_id uuid PK`.
- `policy_code varchar(100)`.
- `status varchar(32)`.
- `priority_sources_json jsonb` - упорядоченный список `URL_REFERRAL_CODE`, `MANUAL_CODE`, `SESSION_CONTEXT`, `CAMPAIGN_DEFAULT_SPONSOR`, `CRM_OVERRIDE`.
- `conflict_strategy varchar(40)`.
- `created_by_user_id uuid`.
- `created_at`, `updated_at timestamptz`.

Индексы: unique `policy_code`; partial unique active policy per scenario допускается при расширении модели.

### `admin_ref_attribution_event`
История решений attribution.
- `attribution_event_id uuid PK`.
- `policy_id uuid FK`.
- `referral_code_id uuid FK nullable`.
- `registration_id uuid` - ссылка на регистрацию feature #008.
- `selected_source varchar(40)`.
- `rejected_sources_json jsonb`.
- `sponsor_partner_id uuid`.
- `reason_code varchar(80)`.
- `comment varchar(1000)`.
- `actor_user_id uuid nullable` - заполнен для CRM override.
- `correlation_id varchar(120)`.
- `occurred_at timestamptz`.

Индексы: btree `registration_id`, `sponsor_partner_id`, `reason_code`, `occurred_at`.

### `admin_ref_conversion_event`
События conversion analytics.
- `conversion_event_id uuid PK`.
- `event_type varchar(64)` - `LANDING_VIEWED`, `CTA_CLICKED`, `REGISTRATION_STARTED`, `INVITE_CODE_VALIDATED`, `APPLICATION_SUBMITTED`, `CONTACT_CONFIRMED`, `PARTNER_ACTIVATED`, `ATTRIBUTION_OVERRIDDEN`.
- `landing_id`, `funnel_id`, `referral_code_id uuid FK nullable`.
- `campaign_code varchar(80)`.
- `source_channel varchar(80)`.
- `sponsor_partner_id uuid`.
- `registration_id uuid`.
- `metadata_json jsonb`.
- `occurred_at timestamptz`.

Индексы: btree `event_type`, `campaign_code`, `source_channel`, `sponsor_partner_id`, `occurred_at`; composite `landing_id + occurred_at`, `funnel_id + occurred_at`.

### `admin_ref_audit_event`
Immutable audit trail административных операций.
- `audit_event_id uuid PK`.
- `entity_type varchar(64)`, `entity_id uuid`.
- `actor_user_id uuid`.
- `action_code varchar(80)`.
- `old_value_json`, `new_value_json jsonb`.
- `reason_code varchar(80)`.
- `comment varchar(1000)`.
- `source_route varchar(240)`.
- `correlation_id varchar(120)`.
- `occurred_at timestamptz`.

Индексы: btree `entity_type + entity_id`, `actor_user_id`, `action_code`, `correlation_id`, `occurred_at`. События не редактируются через admin API.

## Связи
- `admin_ref_landing_variant 1:N admin_ref_landing_version`.
- `admin_ref_landing_version 1:N admin_ref_landing_block`.
- `admin_ref_registration_funnel 1:N admin_ref_funnel_version`.
- `admin_ref_referral_code 1:N admin_ref_conversion_event`.
- `admin_ref_landing_variant 1:N admin_ref_conversion_event`.
- `admin_ref_registration_funnel 1:N admin_ref_conversion_event`.
- `admin_ref_attribution_policy 1:N admin_ref_attribution_event`.
- `admin_ref_referral_code 1:N admin_ref_attribution_event`.

## Liquibase и ownership
Новые таблицы должны быть созданы отдельным XML changelog файлом в owning module `backend/monolith/monolith-app/src/main/java/com/bestorigin/monolith/adminreferral/db`. JPA entities и repository interfaces принадлежат `domain`; controller, service, validator, mapper, config и audit publisher принадлежат role-specific подпакетам `impl`.

## Версионная база
Модель рассчитана на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML и PostgreSQL-compatible типы `uuid`, `jsonb`, `timestamptz`. Если runtime database не поддерживает `jsonb`, требуется documented compatibility fallback перед implementation.


## Статус полноты модуля
На момент feature #28 модуль dmin-referral вводится как новый bounded backend module, поэтому полная module-level ER модель совпадает с feature-level моделью и будет расширяться последующими фичами только через отдельные dedicated Liquibase XML changelogs.
