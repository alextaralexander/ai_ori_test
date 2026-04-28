# ER-описание feature #41 для модуля admin-benefit-program

## Назначение модуля
`admin-benefit-program` является owning backend-модулем административного контура программ преимуществ и удержания. Модуль хранит настройки, версии, бюджеты, награды, ручные корректировки, audit trail и исходящие интеграционные события для downstream-контуров: `partnerbenefits`, `bonuswallet`, `cart`, `order/checkout`, `platformexperience/notification` и `adminplatform`.

Java package prefix: `com.bestorigin.monolith.adminbenefitprogram`.

Backend package ownership:
- `api`: REST DTOs и внешние контракты модуля.
- `domain`: JPA entities и repository interfaces.
- `db`: только Liquibase XML changelog files feature #41.
- `impl/controller`: Spring MVC controllers.
- `impl/service`: сервисные интерфейсы и реализации.
- `impl/validator`: validation и status transition checks.
- `impl/mapper`: MapStruct mappers.
- `impl/event`: публикация integration events.
- `impl/config`: `MonolithModule` и OpenAPI group configuration.
- `impl/exception`: typed exceptions и mnemonic mapping.

## Таблица `benefit_program`
Главная таблица программ преимуществ.

Поля:
- `id uuid PK`: технический идентификатор.
- `code varchar(80) UK not null`: стабильный бизнес-код программы, например `CAT-2026-08-CASHBACK`.
- `type varchar(40) not null`: `CASHBACK`, `REFERRAL_DISCOUNT`, `WELCOME`, `REWARD_SHOP`, `FREE_SHIPPING`, `RESERVATION`, `SUBSCRIPTION`, `RETENTION_OFFER`.
- `status varchar(32) not null`: `DRAFT`, `READY_FOR_REVIEW`, `SCHEDULED`, `ACTIVE`, `PAUSED`, `ARCHIVED`.
- `owner_role varchar(80) not null`: бизнес-владелец правила: `CRM`, `FINANCE`, `BENEFIT_ADMIN`.
- `catalog_id varchar(80) not null`: 21-дневный каталог, к которому привязана программа.
- `active_from timestamptz not null`, `active_to timestamptz not null`: окно действия.
- `current_version integer not null`: текущая опубликованная или редактируемая версия.
- `created_at timestamptz not null`, `updated_at timestamptz not null`: технические timestamps.

Ограничения и индексы:
- `uk_benefit_program_code` на `code`.
- `idx_benefit_program_status_catalog` на `status, catalog_id`.
- `idx_benefit_program_type_period` на `type, active_from, active_to`.
- Check constraint на допустимые `type`, `status`, `active_to > active_from`, `current_version > 0`.

## Таблица `benefit_program_version`
Версионированное тело правил. Активная программа изменяется через новую версию, чтобы не переписывать историю уже созданных выгод.

Поля:
- `id uuid PK`.
- `program_id uuid FK -> benefit_program.id`.
- `version integer not null`: номер версии в рамках программы.
- `rules_json jsonb not null`: типоспецифичные настройки cashback, referral discount, welcome, reward shop, free shipping, reservation, subscription или retention.
- `eligibility_json jsonb not null`: роли, уровни, регионы, ББ, суммы заказов, referral source, channels, risk score.
- `compatibility_json jsonb not null`: priority, stackability, mutual exclusion, max benefit rules.
- `lifecycle_json jsonb not null`: expiration, grace period, carry-over, revoke и scheduled activation/deactivation.
- `created_by_user_id uuid not null`.
- `created_at timestamptz not null`.

Ограничения и индексы:
- `uk_benefit_program_version` на `program_id, version`.
- `idx_benefit_program_version_created_at` на `created_at`.
- JSONB GIN indexes на `eligibility_json` и `rules_json` допускаются для фильтрации и dry-run, если runtime profiling подтвердит необходимость.

## Таблица `benefit_program_budget`
Финансовые лимиты и контроль стоимости программы.

Поля:
- `id uuid PK`.
- `program_id uuid FK -> benefit_program.id`.
- `currency varchar(3) not null`.
- `total_budget numeric(19,4) not null`.
- `used_budget numeric(19,4) not null default 0`.
- `cashback_limit numeric(19,4)`.
- `discount_limit numeric(19,4)`.
- `redemption_limit integer`.
- `stop_on_exhausted boolean not null default true`.

Ограничения и индексы:
- `idx_benefit_program_budget_program` на `program_id`.
- Check constraints: суммы не отрицательные, `used_budget <= total_budget`, ISO currency length = 3.

## Таблица `benefit_program_reward`
Награды и параметры reward shop, если программа имеет тип `REWARD_SHOP`.

Поля:
- `id uuid PK`.
- `program_id uuid FK -> benefit_program.id`.
- `reward_code varchar(80) not null`.
- `cost_amount numeric(19,4) not null`.
- `currency varchar(3) not null`.
- `region_code varchar(40)`.
- `warehouse_code varchar(40)`.
- `active_from timestamptz not null`.
- `active_to timestamptz not null`.
- `redemption_limit integer not null`.

Ограничения и индексы:
- `uk_benefit_program_reward_code` на `program_id, reward_code`.
- `idx_benefit_program_reward_region_warehouse` на `region_code, warehouse_code`.
- Check constraints: `cost_amount >= 0`, `redemption_limit > 0`, `active_to > active_from`.

## Таблица `benefit_program_manual_adjustment`
Разрешенные ручные корректировки cashback, reward eligibility, reservation или subscription status.

Поля:
- `id uuid PK`.
- `program_id uuid FK -> benefit_program.id`.
- `target_user_id uuid`.
- `target_partner_number varchar(80)`.
- `adjustment_type varchar(40) not null`: `CASHBACK`, `REWARD_ELIGIBILITY`, `RESERVATION`, `SUBSCRIPTION`.
- `amount numeric(19,4)`.
- `currency varchar(3)`.
- `reason_code varchar(80) not null`.
- `evidence_ref varchar(255)`.
- `approval_status varchar(32) not null`: `DRAFT`, `APPROVED`, `REJECTED`, `APPLIED`.
- `created_at timestamptz not null`.

Ограничения и индексы:
- `idx_benefit_program_adjustment_target` на `target_user_id, target_partner_number`.
- `idx_benefit_program_adjustment_program` на `program_id`.
- Check constraint: указан хотя бы один target (`target_user_id` или `target_partner_number`).

## Таблица `benefit_program_audit_event`
Immutable audit trail административных действий.

Поля:
- `id uuid PK`.
- `program_id uuid FK -> benefit_program.id`.
- `actor_user_id uuid not null`.
- `actor_role varchar(80) not null`.
- `action_code varchar(80) not null`: `CREATE`, `UPDATE`, `DRY_RUN`, `SUBMIT_REVIEW`, `SCHEDULE`, `ACTIVATE`, `PAUSE`, `ARCHIVE`, `ADJUST`, `EXPORT`, `INTEGRATION_SEND`.
- `entity_type varchar(80) not null`.
- `entity_id uuid`.
- `before_summary_json jsonb`.
- `after_summary_json jsonb`.
- `reason_code varchar(80)`.
- `correlation_id varchar(120) not null`.
- `created_at timestamptz not null`.

Ограничения и индексы:
- `idx_benefit_program_audit_program_created` на `program_id, created_at`.
- `idx_benefit_program_audit_actor` на `actor_user_id, created_at`.
- `idx_benefit_program_audit_correlation` на `correlation_id`.

## Таблица `benefit_program_integration_event`
Outbox-like журнал исходящих событий в связанные контуры.

Поля:
- `id uuid PK`.
- `program_id uuid FK -> benefit_program.id`.
- `target_context varchar(80) not null`: `PARTNER_BENEFITS`, `BONUS_WALLET`, `CART`, `CHECKOUT`, `NOTIFICATION`, `ADMIN_PLATFORM`.
- `event_type varchar(80) not null`.
- `idempotency_key varchar(120) UK not null`.
- `payload_checksum varchar(120) not null`.
- `status varchar(32) not null`: `PENDING`, `SENT`, `FAILED`, `SKIPPED`.
- `retry_count integer not null default 0`.
- `last_error_mnemonic varchar(160)`.
- `correlation_id varchar(120) not null`.
- `created_at timestamptz not null`.
- `updated_at timestamptz not null`.

Ограничения и индексы:
- `uk_benefit_program_integration_idempotency` на `idempotency_key`.
- `idx_benefit_program_integration_status` на `status, updated_at`.
- `idx_benefit_program_integration_program` на `program_id`.
- Check constraint: `retry_count >= 0`.

## Liquibase и версия
Feature #41 должна получить отдельный Liquibase XML changelog в db package owning module, например `admin-benefit-program/feature-041-admin-benefit-program.xml`. SQL/YAML/JSON changesets не используются.

Версионная база задачи фиксируется на 28.04.2026: новые части должны следовать latest-stable baseline, применимому к монолиту Best Ori Gin. Если фактический монолит требует fallback по Java, Spring Boot, Hibernate, Liquibase, PostgreSQL или frontend stack, причина совместимости фиксируется в архитектурных артефактах и статусе follow-up.
