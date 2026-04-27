# Module OpenAPI specification description. Module admin-referral

## Назначение API
`admin-referral` предоставляет административный API для настройки landing variants, registration funnels, referral codes, attribution policy, conversion analytics и audit trail. Runtime Swagger должен быть доступен через monolith OpenAPI group `/v3/api-docs/admin-referral` и Swagger UI `/swagger-ui/admin-referral`.

## Базовый путь
- Внутренний REST base path: `/api/admin-referral`.
- Module key для Swagger grouping: `admin-referral`.
- Package prefix backend: `com.bestorigin.monolith.adminreferral`.

## Endpoint groups

### Landing variants
- `GET /landing-variants` возвращает список лендингов с фильтрами `landingType`, `status`, `campaignCode`, `locale`, `search`.
- `POST /landing-variants` создает draft landing variant.
- `GET /landing-variants/{landingId}` возвращает детальную карточку.
- `PUT /landing-variants/{landingId}` обновляет draft и создает новую версию при содержательном изменении.
- `POST /landing-variants/{landingId}/activate` активирует версию после проверки обязательных blocks, slug, locale, campaign code и active window.
- `POST /landing-variants/{landingId}/preview` возвращает draft render model без публикации.

DTO:
- `AdminReferralLandingUpsertRequest` содержит `landingType`, `locale`, `slug`, `name`, `campaignCode`, `activeFrom`, `activeTo`, `hero`, `seo`, `campaignContext`, `blocks`.
- `AdminReferralLandingDetailResponse` содержит landing summary, version, `auditRecorded` и optional `messageCode`.
- `AdminReferralLandingBlock` допускает block types `HERO`, `BENEFIT`, `TESTIMONIAL`, `CTA`, `FAQ`, `LEGAL_NOTICE`.

Валидации:
- `slug` обязателен, нормализован и уникален в активном окне.
- `activeFrom < activeTo`.
- Для активации обязательны hero, хотя бы один benefit, CTA и legal notice.
- `campaignContext` не содержит секреты, токены и персональные данные кандидатов.

### Registration funnels
- `GET /funnels` ищет funnel definitions по scenario и status.
- `POST /funnels` создает draft funnel.
- `POST /funnels/{funnelId}/activate` активирует версию funnel.

DTO:
- `AdminReferralFunnelUpsertRequest` содержит `funnelCode`, `scenario`, `steps`, `consentCodes`, `validationRules`, `defaultContext`.
- `AdminReferralFunnelDetailResponse` содержит funnel id, code, scenario, status, active version и `messageCode`.

Валидации:
- Активируемый funnel должен содержать обязательные шаги регистрации и consent codes.
- Изменение active funnel создает новую версию; уже начатые регистрации используют ранее выбранную версию.

### Referral codes
- `GET /referral-codes` ищет коды по campaign, owner partner, code type и status.
- `POST /referral-codes` генерирует referral code; поддерживает header `Idempotency-Key`.
- `POST /referral-codes/{referralCodeId}/revoke` отзывает код.

DTO:
- `AdminReferralCodeGenerateRequest` содержит `codeType`, `campaignCode`, `ownerPartnerId`, `landingType`, `activeFrom`, `activeTo`, `maxUsageCount`, `constraints`.
- `AdminReferralCodeResponse` возвращает `referralCodeId`, `publicCode`, type, status, campaign, owner, usage counts и `messageCode`.

Валидации:
- `publicCode` уникален.
- `active window` не может быть пустым или истекшим при активации.
- `usageCount` не может превышать `maxUsageCount`.
- Повторная генерация с тем же `Idempotency-Key` возвращает созданный код без дубля.

### Attribution policy
- `GET /attribution-policy` возвращает активную policy.
- `PUT /attribution-policy` обновляет priority sources и conflict strategy.
- `POST /attribution/override` сохраняет ручной sponsor attribution override.

DTO:
- `AdminReferralAttributionPolicyUpdateRequest` содержит упорядоченный `prioritySources` и `conflictStrategy`.
- `AdminReferralAttributionOverrideRequest` содержит `registrationId`, `sponsorPartnerId`, `reasonCode`, `comment`.
- `AdminReferralAttributionEventResponse` возвращает сохраненное решение и `messageCode`.

Валидации:
- `prioritySources` должен содержать только допустимые источники и не иметь дублей.
- Override требует permission scope `ADMIN_REFERRAL_ATTRIBUTION_OVERRIDE`, reason code и комментарий.
- Исходная attribution history не удаляется.

### Conversion analytics
- `GET /analytics/conversions` возвращает aggregate report по campaign, landing, source channel, sponsor и периоду.

DTO:
- `AdminReferralConversionReportResponse` содержит `totals` и `rows`.
- `metrics` являются machine-readable числовыми значениями, frontend не парсит текст.

Ограничения:
- Отчет не содержит секретов, одноразовых токенов и лишних персональных данных кандидатов.
- Пустой результат возвращается как `200` с пустым массивом, а не как ошибка.

### Audit
- `GET /audit` ищет audit events по `entityType`, `entityId`, `actionCode`, `correlationId`.

DTO:
- `AdminReferralAuditResponse` содержит event id, entity, action, actor, correlation id и timestamp.
- Audit API read-only; изменение audit events через API не поддерживается.

## RBAC scopes
- `ADMIN_REFERRAL_VIEW` - просмотр списков и карточек.
- `ADMIN_REFERRAL_MANAGE` - создание и обновление landing variants.
- `ADMIN_REFERRAL_FUNNEL_MANAGE` - управление registration funnels.
- `ADMIN_REFERRAL_CODE_MANAGE` - генерация и отзыв referral codes.
- `ADMIN_REFERRAL_ATTRIBUTION_MANAGE` - управление policy.
- `ADMIN_REFERRAL_ATTRIBUTION_OVERRIDE` - ручная корректировка sponsor attribution.
- `ADMIN_REFERRAL_ANALYTICS_VIEW` - просмотр conversion report.
- `ADMIN_REFERRAL_AUDIT_VIEW` - просмотр audit trail.

## Message contract
Backend не возвращает hardcoded user-facing text. Все predefined сообщения передаются mnemonic-кодами:
- `STR_MNEMO_ADMIN_REFERRAL_FORBIDDEN`
- `STR_MNEMO_ADMIN_REFERRAL_LANDING_SAVED`
- `STR_MNEMO_ADMIN_REFERRAL_LANDING_LEGAL_NOTICE_REQUIRED`
- `STR_MNEMO_ADMIN_REFERRAL_LANDING_SLUG_CONFLICT`
- `STR_MNEMO_ADMIN_REFERRAL_ACTIVE_WINDOW_INVALID`
- `STR_MNEMO_ADMIN_REFERRAL_FUNNEL_SAVED`
- `STR_MNEMO_ADMIN_REFERRAL_FUNNEL_CONSENT_REQUIRED`
- `STR_MNEMO_ADMIN_REFERRAL_CODE_GENERATED`
- `STR_MNEMO_ADMIN_REFERRAL_CODE_ACTIVE_WINDOW_INVALID`
- `STR_MNEMO_ADMIN_REFERRAL_CODE_DUPLICATE`
- `STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_POLICY_SAVED`
- `STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_POLICY_INVALID`
- `STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_REASON_REQUIRED`
- `STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_OVERRIDDEN`

Frontend обязан локализовать эти коды через `resources_ru.ts` и `resources_en.ts`.

## Backend package ownership
- `api`: DTO, request/response records, enum contracts.
- `domain`: JPA entities и repository interfaces для `admin_ref_*`.
- `db`: Liquibase XML changelog feature #28.
- `impl/controller`: Spring MVC controllers.
- `impl/service`: orchestration и business operations.
- `impl/validator`: landing/funnel/referral/attribution validators.
- `impl/mapper`: API/domain mapping.
- `impl/config`: `MonolithModule` и OpenAPI grouping.
- `impl/exception`: business exceptions с mnemonic codes.

## Версионная база
OpenAPI artifact рассчитан на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, springdoc runtime grouping, React/TypeScript/Vite frontend и Ant Design-compatible UI. Спецификация должна соответствовать runtime Swagger после реализации.


## Статус полноты модуля
На момент feature #28 модуль dmin-referral вводится как новый backend module, поэтому полная module-level OpenAPI спецификация совпадает с feature-level контрактом. Последующие расширения обязаны обновлять этот файл и runtime Swagger group /v3/api-docs/admin-referral.
