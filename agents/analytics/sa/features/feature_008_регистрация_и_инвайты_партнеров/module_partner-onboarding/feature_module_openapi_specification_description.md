# Feature 008. OpenAPI description для module `partner-onboarding`

## Назначение API
OpenAPI feature #8 описывает внешний contract backend module `partner-onboarding` для публичной регистрации beauty/business партнеров, invite/referral validation, activation flow, sponsor cabinet и retry-safe передачи registration lead events. Runtime Swagger должен быть доступен в monolith group `/v3/api-docs/partner-onboarding` и `/swagger-ui/partner-onboarding` после добавления module config.

## Endpoint `GET /api/partner-onboarding/invites/validate`
Проверяет invite/referral-код до отправки registration application.

Параметры:
- `code`: обязательный public code длиной 3-64 символа.
- `onboardingType`: `BEAUTY_PARTNER` или `BUSINESS_PARTNER`.
- `campaignId`: optional campaign context.

Ответ `InviteValidationResponse`:
- `status`: `ACTIVE`, `NOT_FOUND`, `EXPIRED`, `DISABLED`, `ALREADY_USED`, `TYPE_MISMATCH`.
- `onboardingType`: применимый тип onboarding.
- `campaignId`: кампания, если определена.
- `sponsor`: только публично разрешенный `displayNameKey`, `publicCode`, optional `avatarUrl`.
- `messageCode`: mnemonic-код `STR_MNEMO_*`, например `STR_MNEMO_INVITE_CODE_INVALID`.

Контракт не возвращает телефон, email, internal user id, финансовые данные или hardcoded user-facing text.

## Endpoint `POST /api/partner-onboarding/registrations`
Создает registration application. Обязателен header `Idempotency-Key`, чтобы повторный submit не создавал дубль.

Request `RegistrationApplicationRequest`:
- `onboardingType`: beauty/business сценарий.
- `inviteCode`: optional code из URL, формы или landing context.
- `candidateName`: имя кандидата.
- `contact`: канал и значение контакта.
- `campaignId`, `landingType`, `landingVariant`, `sourceRoute`: marketing attribution.
- `consentVersions`: набор принятых согласий с кодом, версией и флагом принятия.

Responses:
- `201`: новая заявка создана.
- `200`: повторный idempotent submit вернул существующую заявку.
- `409`: duplicate contact или attribution conflict с `ErrorResponse`.

Response `RegistrationApplicationResponse` содержит `applicationId`, `applicationNumber`, `status`, `nextAction`, optional `activationRoute` и `messageCode`. Пользовательские тексты frontend обязан получить из i18n dictionaries по `messageCode`.

## Endpoint `GET /api/partner-onboarding/activations/{token}`
Возвращает состояние активации по activation token. Raw token приходит только в URL и в базе хранится как `token_hash`.

Response `ActivationStateResponse`:
- `applicationId`.
- `status`: состояние заявки.
- `contactConfirmed`, `termsAccepted`.
- optional public sponsor context.
- `messageCode`: например `STR_MNEMO_ACTIVATION_READY`, `STR_MNEMO_ACTIVATION_TOKEN_EXPIRED`.

Неверный, истекший или недоступный token возвращает `404` или контролируемый mnemonic error без раскрытия чужой заявки.

## Endpoint `POST /api/partner-onboarding/activations/{token}/confirm-contact`
Подтверждает email или телефон одноразовым кодом.

Request `ContactConfirmationRequest`:
- `code`: одноразовый код длиной 4-12 символов.

Правила:
- неверный код не меняет статус заявки;
- истекший код возвращает mnemonic `STR_MNEMO_CONTACT_CODE_EXPIRED`;
- rate limit должен применяться на уровне сервиса/security;
- raw code не пишется в audit payload и логи.

## Endpoint `POST /api/partner-onboarding/activations/{token}/complete`
Завершает активацию partner profile. Обязателен `Idempotency-Key`.

Request `ActivationCompleteRequest`:
- `acceptedTerms`: версии принятых условий для финальной активации.

Response `ActivationCompleteResponse`:
- `partnerProfileId`.
- `partnerNumber`.
- `status=ACTIVE`.
- `referralLink`: `referralCode` и `targetRoute`.
- `messageCode=STR_MNEMO_PARTNER_ACTIVATED`.

Повторный запрос по уже активированной заявке возвращает текущий активированный результат без создания второго profile или referral link.

## Endpoint `GET /api/partner-onboarding/sponsor-cabinet/invites`
Возвращает invite-записи только текущего авторизованного спонсора.

Security:
- `bearerAuth` обязателен.
- Доступ разрешен роли `Спонсор/партнер` или будущей внутренней роли поддержки, если она явно добавлена.

Filters:
- `status`.
- `onboardingType`.

Response `SponsorInviteListResponse` содержит список `SponsorInviteResponse`: `inviteId`, `code`, `onboardingType`, `status`, `targetRoute`, `candidatePublicName`, `expiresAt`, `lastOpenedAt`, `messageCode`. Полный контакт кандидата не возвращается.

## Endpoint `POST /api/partner-onboarding/sponsor-cabinet/invites`
Создает invite для текущего спонсора. Обязателен `Idempotency-Key`.

Request `CreateInviteRequest`:
- `onboardingType`.
- `campaignId`.
- optional `candidatePublicName`.

Business validation:
- статус текущего партнера должен позволять создание invite;
- кампания должна быть активна;
- onboarding type должен быть доступен текущей конфигурации;
- candidate public name очищается и не используется как доверенный HTML.

## Endpoint `POST /api/partner-onboarding/sponsor-cabinet/invites/{inviteId}/resend`
Повторно отправляет invite или возвращает существующую ссылку для копирования. Endpoint не создает дубль invite.

Request `ResendInviteRequest`:
- optional `channel`: `COPY_LINK`, `EMAIL`, `SMS`.

Если канал отправки недоступен, backend возвращает контролируемый `messageCode`, а frontend показывает локализованное состояние и оставляет доступной копию ссылки, если это разрешено.

## DTO и enums
- `OnboardingType`: `BEAUTY_PARTNER`, `BUSINESS_PARTNER`.
- `InviteStatus`: `CREATED`, `OPENED`, `REGISTRATION_STARTED`, `SUBMITTED`, `ACTIVE`, `EXPIRED`, `REJECTED`, `DISABLED`.
- `ApplicationStatus`: `PENDING_CONTACT_CONFIRMATION`, `PENDING_CRM_REVIEW`, `READY_FOR_ACTIVATION`, `ACTIVE`, `REJECTED`, `EXPIRED`.
- `InviteValidationStatus`: `ACTIVE`, `NOT_FOUND`, `EXPIRED`, `DISABLED`, `ALREADY_USED`, `TYPE_MISMATCH`.
- `ErrorResponse`: `code`, `messageCode`, structured `details`.

## STR_MNEMO codes
Feature #8 вводит минимум следующие mnemonic-коды, которые должны быть добавлены во все поддерживаемые frontend i18n dictionaries:
- `STR_MNEMO_INVITE_CODE_INVALID`
- `STR_MNEMO_INVITE_CODE_EXPIRED`
- `STR_MNEMO_INVITE_CODE_DISABLED`
- `STR_MNEMO_INVITE_TYPE_MISMATCH`
- `STR_MNEMO_REGISTRATION_APPLICATION_CREATED`
- `STR_MNEMO_REGISTRATION_DUPLICATE_CONTACT`
- `STR_MNEMO_ATTRIBUTION_CONFLICT`
- `STR_MNEMO_ACTIVATION_READY`
- `STR_MNEMO_ACTIVATION_TOKEN_EXPIRED`
- `STR_MNEMO_CONTACT_CODE_INVALID`
- `STR_MNEMO_CONTACT_CODE_EXPIRED`
- `STR_MNEMO_PARTNER_ACTIVATED`
- `STR_MNEMO_INVITE_CREATED`
- `STR_MNEMO_INVITE_RESEND_UNAVAILABLE`
- `STR_MNEMO_SPONSOR_CABINET_FORBIDDEN`

## Validation и security
- Все `code`, `token`, `sourceRoute`, `candidatePublicName` считаются untrusted input.
- Public endpoints не раскрывают existence чужого аккаунта сверх контролируемых states.
- Sponsor cabinet не принимает sponsor id из клиента; текущий sponsor определяется из security context.
- `Idempotency-Key` обязателен для операций создания заявки, создания invite и завершения активации.
- Backend не возвращает hardcoded user-facing text во frontend.

## Версионная база
Новые технологии не вводятся. Контракты рассчитаны на Spring MVC controllers внутри `com.bestorigin.monolith.partneronboarding.impl.controller`, автоматическую генерацию OpenAPI через springdoc и текущий frontend i18n contract. Если implementation добавит внешнюю CRM/notification integration, транспорт должен быть скрыт за `impl/client` или `impl/event` и не менять публичный API без обновления этих артефактов.
