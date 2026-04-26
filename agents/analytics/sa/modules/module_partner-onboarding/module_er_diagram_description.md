# Module `partner-onboarding`. Полное описание ER diagram

## Назначение модуля
`partner-onboarding` владеет данными регистрации и инвайтов партнеров Best Ori Gin. Модуль отвечает за invite/referral code lifecycle, registration application, activation token, создание partner profile, выдачу referral link, sponsor cabinet и audit/event stream onboarding. Модуль не рассчитывает MLM-бонусы и не управляет каталогом; он создает корректные входные данные для будущих MLM, CRM и order сценариев.

## Сущности

### `partner_onboarding_invite`
Хранит invite/referral-ссылки, создаваемые спонсором или маркетинговым контуром.

Поля:
- `id uuid PK`
- `code varchar(64) UK NOT NULL`
- `sponsor_partner_id uuid NOT NULL`
- `onboarding_type varchar(40) NOT NULL`
- `campaign_id varchar(80) NOT NULL`
- `source varchar(80) NOT NULL`
- `status varchar(40) NOT NULL`
- `expires_at timestamptz NOT NULL`
- `candidate_public_name varchar(160) NULL`
- `last_opened_at timestamptz NULL`
- `created_at timestamptz NOT NULL`
- `updated_at timestamptz NOT NULL`

Индексы:
- unique index на `code`.
- composite index `sponsor_partner_id, status, created_at`.
- composite index `expires_at, status`.

### `partner_registration_application`
Хранит заявку кандидата на регистрацию partner profile.

Поля:
- `id uuid PK`
- `application_number varchar(80) UK NOT NULL`
- `invite_id uuid FK NULL`
- `sponsor_partner_id uuid NULL`
- `onboarding_type varchar(40) NOT NULL`
- `status varchar(48) NOT NULL`
- `candidate_name varchar(180) NOT NULL`
- `contact_hash varchar(160) NOT NULL`
- `contact_channel varchar(40) NULL`
- `campaign_id varchar(80) NOT NULL`
- `landing_type varchar(40) NULL`
- `landing_variant varchar(80) NULL`
- `source_route varchar(220) NULL`
- `idempotency_key varchar(120) NULL`
- `consent_snapshot_json jsonb NOT NULL`
- `crm_lead_status varchar(40) NULL`
- `created_at timestamptz NOT NULL`
- `updated_at timestamptz NOT NULL`

Индексы:
- unique index на `application_number`.
- index `contact_hash, status` для deduplication.
- index `invite_id`.
- index `sponsor_partner_id, status`.
- unique partial или application-level uniqueness для `idempotency_key`, если ключ задан.

### `partner_activation_token`
Хранит hash activation token для завершения регистрации.

Поля:
- `id uuid PK`
- `application_id uuid FK NOT NULL`
- `token_hash varchar(160) UK NOT NULL`
- `status varchar(40) NOT NULL`
- `expires_at timestamptz NOT NULL`
- `used_at timestamptz NULL`
- `created_at timestamptz NOT NULL`

Ограничения:
- `application_id` ссылается на `partner_registration_application.id`.
- raw token не хранится.
- `token_hash` уникален.

### `partner_profile`
Хранит активированный профиль партнера в минимальном составе для feature #8.

Поля:
- `id uuid PK`
- `application_id uuid FK NOT NULL`
- `sponsor_partner_id uuid NULL`
- `partner_number varchar(80) UK NOT NULL`
- `status varchar(40) NOT NULL`
- `initial_level varchar(40) NOT NULL`
- `activated_at timestamptz NOT NULL`
- `created_at timestamptz NOT NULL`

Ограничения:
- `application_id` уникален, чтобы одна заявка не активировала несколько profile.
- `partner_number` уникален.

### `partner_referral_link`
Хранит публичный referral code/link активированного партнера.

Поля:
- `id uuid PK`
- `partner_profile_id uuid FK NOT NULL`
- `referral_code varchar(64) UK NOT NULL`
- `landing_type varchar(40) NOT NULL`
- `target_route varchar(220) NOT NULL`
- `status varchar(40) NOT NULL`
- `created_at timestamptz NOT NULL`

Ограничения:
- `partner_profile_id` ссылается на `partner_profile.id`.
- `referral_code` уникален и не раскрывает internal id.

### `partner_onboarding_event`
Хранит audit и integration events onboarding.

Поля:
- `id uuid PK`
- `invite_id uuid FK NULL`
- `application_id uuid FK NULL`
- `partner_profile_id uuid FK NULL`
- `event_type varchar(60) NOT NULL`
- `event_idempotency_key varchar(160) UK NOT NULL`
- `payload_json jsonb NOT NULL`
- `created_at timestamptz NOT NULL`

Ограничения:
- `payload_json` не должен содержать raw passwords, raw one-time codes, raw activation token, full private contacts или hardcoded user-facing text.
- `event_idempotency_key` уникален для безопасного retry.

## Связи
- Один invite может породить много registration applications, но активная бизнес-логика должна ограничивать дубли по contact/status.
- Одна registration application может иметь несколько activation token за жизненный цикл, но только один active token.
- Одна application может создать только один partner profile.
- Один partner profile может иметь несколько referral links, если в будущем появятся разные landing targets.
- Invite, application и profile могут иметь много onboarding events.

## Backend package ownership
- `api`: enums, DTO и external REST contracts.
- `domain`: JPA entities и repository interfaces.
- `db`: XML Liquibase changelog files.
- `impl/controller`: Spring MVC controllers.
- `impl/service`: orchestration services.
- `impl/validator`: invite, registration и activation validators.
- `impl/mapper`: DTO/entity mappers.
- `impl/event`: audit/CRM event publisher.
- `impl/client`: адаптер CRM/marketing integration.
- `impl/config`: module config, Swagger group metadata и scheduler settings.

## Версионная база
Новые технологии не вводятся. Модуль использует текущий Java/Spring Boot/Hibernate/PostgreSQL baseline репозитория, Liquibase XML и springdoc OpenAPI. `jsonb` используется как PostgreSQL capability для consent snapshot и event payload.
