# Feature 008. ER description для module `partner-onboarding`

## Назначение изменений
Feature #8 вводит отдельный backend module `partner-onboarding` для регистрации beauty/business партнеров, invite/referral attribution, активации приглашенного партнера, выдачи personal referral link, sponsor cabinet и передачи registration lead events в CRM. Модуль изолирует onboarding-данные от `public-content` и `catalog`, потому что эти данные включают статусы заявок, privacy-sensitive контакты, audit trail и будущую связь с MLM-структурой.

## Таблица `partner_onboarding_invite`
- `id uuid PK`: внутренний идентификатор invite.
- `code varchar(64) UK NOT NULL`: публичный invite/referral code для URL. Код не должен содержать internal user id.
- `sponsor_partner_id uuid NOT NULL`: идентификатор партнера-спонсора в будущем partner/master-data контуре.
- `onboarding_type varchar(40) NOT NULL`: `BEAUTY_PARTNER` или `BUSINESS_PARTNER`.
- `campaign_id varchar(80) NOT NULL`: кампания, в рамках которой создан invite.
- `source varchar(80) NOT NULL`: источник создания, например `SPONSOR_CABINET`, `BENEFIT_LANDING`, `CRM`.
- `status varchar(40) NOT NULL`: `CREATED`, `OPENED`, `REGISTRATION_STARTED`, `SUBMITTED`, `ACTIVE`, `EXPIRED`, `REJECTED`, `DISABLED`.
- `expires_at timestamptz NOT NULL`: срок действия invite.
- `candidate_public_name varchar(160) NULL`: безопасное отображаемое имя кандидата для sponsor cabinet, без полного контакта.
- `last_opened_at timestamptz NULL`: последнее открытие invite-ссылки.
- `created_at timestamptz NOT NULL`, `updated_at timestamptz NOT NULL`: audit timestamps.

## Таблица `partner_registration_application`
- `id uuid PK`: внутренний идентификатор заявки.
- `application_number varchar(80) UK NOT NULL`: бизнес-номер заявки для поддержки и audit.
- `invite_id uuid FK NULL`: ссылка на invite, если заявка пришла по валидному коду.
- `sponsor_partner_id uuid NULL`: фактически примененная sponsor attribution; может отсутствовать для базовой регистрации без спонсора, если это разрешено правилами кампании.
- `onboarding_type varchar(40) NOT NULL`: выбранный сценарий регистрации.
- `status varchar(48) NOT NULL`: `DRAFT`, `PENDING_CONTACT_CONFIRMATION`, `PENDING_CRM_REVIEW`, `READY_FOR_ACTIVATION`, `ACTIVE`, `REJECTED`, `EXPIRED`.
- `candidate_name varchar(180) NOT NULL`: имя кандидата для заявки и CRM.
- `contact_hash varchar(160) NOT NULL`: hash нормализованного email/телефона для deduplication без раскрытия контакта в публичных ответах.
- `contact_channel varchar(40) NULL`: `EMAIL`, `PHONE` или будущий поддерживаемый канал.
- `campaign_id varchar(80) NOT NULL`: кампания регистрации.
- `landing_type varchar(40) NULL`, `landing_variant varchar(80) NULL`, `source_route varchar(220) NULL`: marketing context из benefit landing.
- `idempotency_key varchar(120) NULL`: ключ идемпотентности submit формы.
- `consent_snapshot_json jsonb NOT NULL`: версии согласий, время принятия и required flags.
- `crm_lead_status varchar(40) NULL`: `PENDING`, `SENT`, `FAILED_RETRY`, `SKIPPED`.
- `created_at timestamptz NOT NULL`, `updated_at timestamptz NOT NULL`: audit timestamps.

## Таблица `partner_activation_token`
- `id uuid PK`: внутренний идентификатор token.
- `application_id uuid FK NOT NULL`: заявка, которую можно активировать.
- `token_hash varchar(160) UK NOT NULL`: hash activation token; raw token не хранится.
- `status varchar(40) NOT NULL`: `ACTIVE`, `USED`, `EXPIRED`, `REVOKED`.
- `expires_at timestamptz NOT NULL`: срок действия.
- `used_at timestamptz NULL`: время успешного использования.
- `created_at timestamptz NOT NULL`: время создания.

## Таблица `partner_profile`
- `id uuid PK`: внутренний идентификатор активированного partner profile.
- `application_id uuid FK NOT NULL`: исходная заявка.
- `sponsor_partner_id uuid NULL`: sponsor attribution, примененная при активации.
- `partner_number varchar(80) UK NOT NULL`: бизнес-номер партнера.
- `status varchar(40) NOT NULL`: `ACTIVE`, `SUSPENDED`, `CLOSED`.
- `initial_level varchar(40) NOT NULL`: начальный уровень для будущего MLM-контура, например `STARTER`.
- `activated_at timestamptz NOT NULL`: дата активации.
- `created_at timestamptz NOT NULL`: дата создания записи.

## Таблица `partner_referral_link`
- `id uuid PK`: внутренний идентификатор referral link.
- `partner_profile_id uuid FK NOT NULL`: партнер-владелец ссылки.
- `referral_code varchar(64) UK NOT NULL`: публичный код для последующих invite/benefit routes.
- `landing_type varchar(40) NOT NULL`: базовый landing target, например `BEAUTY` или `BUSINESS`.
- `target_route varchar(220) NOT NULL`: route, куда ведет ссылка.
- `status varchar(40) NOT NULL`: `ACTIVE`, `DISABLED`, `EXPIRED`.
- `created_at timestamptz NOT NULL`: дата генерации.

## Таблица `partner_onboarding_event`
- `id uuid PK`: внутренний идентификатор события.
- `invite_id uuid FK NULL`, `application_id uuid FK NULL`, `partner_profile_id uuid FK NULL`: optional связи с объектами onboarding.
- `event_type varchar(60) NOT NULL`: `INVITE_CREATED`, `INVITE_OPENED`, `REGISTRATION_STARTED`, `APPLICATION_SUBMITTED`, `CONTACT_CONFIRMED`, `PARTNER_ACTIVATED`, `REFERRAL_LINK_CREATED`, `CRM_LEAD_SENT`, `CRM_LEAD_RETRY`.
- `event_idempotency_key varchar(160) UK NOT NULL`: ключ дедупликации события.
- `payload_json jsonb NOT NULL`: структурированный payload без паролей, одноразовых кодов, raw token и лишних персональных данных.
- `created_at timestamptz NOT NULL`: время события.

## Индексы и ограничения
- `uk_partner_onboarding_invite_code` на `partner_onboarding_invite.code`.
- `idx_partner_invite_sponsor_status` на `sponsor_partner_id, status, created_at` для sponsor cabinet.
- `idx_partner_invite_expires_status` на `expires_at, status` для истечения invite.
- `uk_partner_registration_application_number` на `application_number`.
- `idx_partner_application_contact_status` на `contact_hash, status` для deduplication.
- `idx_partner_application_invite` на `invite_id`.
- `idx_partner_application_sponsor_status` на `sponsor_partner_id, status`.
- `uk_partner_activation_token_hash` на `token_hash`.
- `idx_partner_activation_application_status` на `application_id, status`.
- `uk_partner_profile_application` на `partner_profile.application_id`, чтобы одна заявка не создавала несколько профилей.
- `uk_partner_profile_number` на `partner_number`.
- `uk_partner_referral_code` на `referral_code`.
- `idx_partner_event_application_type` на `application_id, event_type, created_at`.

## Ownership и package policy
- `api`: request/response DTO, enums и external contracts модуля `partner-onboarding`.
- `domain`: JPA entities и repository interfaces для перечисленных таблиц.
- `db`: dedicated XML Liquibase changelog `feature_008_partner_onboarding.xml`.
- `impl`: controllers, services, validators, mappers, CRM publisher, activation orchestration, invite expiration scheduler и security wiring в role-specific subpackages.

## Версионная база
Новые технологии не вводятся. ER-модель рассчитана на текущий monolith baseline, Java/Spring Boot/Hibernate stack, PostgreSQL `jsonb` и XML Liquibase changesets. Если при реализации потребуется внешний CRM SDK или отдельный identity provider, это должно быть оформлено отдельным архитектурным решением и не заменяет описанную модель onboarding.
