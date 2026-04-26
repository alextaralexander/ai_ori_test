# Feature 007. Public-content OpenAPI-описание

## Назначение API
API `benefit-landings` отдает frontend структурированный payload публичных маркетинговых лендингов и принимает conversion events. Контракт покрывает страницы `/beauty-benefits`, `/beauty-benefits/{code}`, `/business-benefits`, `/business-benefits/{code}`, `/member-benefits`, `/vip-customer-benefits` и `/the-new-oriflame-app`.

## GET /api/public-content/benefit-landings/{landingType}
Endpoint возвращает опубликованный benefit landing по типу:
- `BEAUTY` - бьюти-преимущества продукции, каталога, welcome benefits и cashback.
- `BUSINESS` - партнерская программа, community-модель, offline sales и referral benefits.
- `MEMBER` - выгоды участника.
- `VIP_CUSTOMER` - VIP customer benefits.
- `APP` - преимущества мобильного приложения.

Параметры:
- `landingType` path, обязательный enum.
- `code` query, optional referral/invite-код длиной до 64 символов.
- `campaignId` query, optional campaign context.
- `variant` query, optional A/B-вариант блоков.

Ответ `200` содержит:
- `landingType`, `routePath`, `campaignId`, `variant`.
- `seo` с i18n-ключами title/description и canonical path.
- `referral` со статусом кода, нормализованным кодом, публичным именем спонсора и optional `messageCode`.
- `blocks` с ordered list блоков, типами, i18n-ключами текстов, payload и CTA.

Ответ `404` используется, если лендинг не опубликован или неизвестен. Предопределенное сообщение возвращается через `messageCode`, например `STR_MNEMO_PUBLIC_BENEFIT_LANDING_NOT_FOUND`.

## POST /api/public-content/benefit-landings/conversions
Endpoint принимает событие просмотра или клика CTA. Он не должен блокировать пользовательский переход: frontend может fire-and-forget событие, а backend возвращает `202`.

Тело запроса:
- `landingType` - тип лендинга.
- `variant` - A/B-вариант.
- `referralCode` - optional referral-код.
- `campaignId` - optional campaign context.
- `ctaType` - `VIEW`, `REGISTER`, `REGISTER_PARTNER`, `OPEN_CATALOG`, `INSTALL_APP`, `CONTACT_SPONSOR`, `ACTIVATE_BENEFITS` или совместимый новый код.
- `routePath` - фактический frontend route.
- `occurredAt` - timestamp события.
- `anonymousSessionId` - optional обезличенный id сессии.

Ответ `202` возвращает `{ "accepted": true }`. Ответ `400` содержит mnemonic-код ошибки валидации и не должен включать hardcoded user-facing текст.

## DTO и валидации
- `BenefitLandingType` ограничен known landing values, чтобы Swagger и frontend routing оставались синхронизированы.
- `ReferralContext.status` принимает `ACTIVE`, `EXPIRED`, `DISABLED`, `NOT_FOUND`.
- Для активного referral-кода `sponsorPublicNameKey` может быть заполнен, но email, телефон и внутренние id не передаются.
- `BenefitLandingBlock.payload` остается структурированным JSON-object для медиа-ключей, bullet ids, secondary links и display conditions.
- `BenefitCta.preserveReferralContext=true` означает, что frontend обязан добавить `landingType`, `variant`, `code` и `campaignId` в целевой переход, если они доступны.

## Mnemonic-коды
Минимальный набор предопределенных кодов, которые могут попасть во frontend:
- `STR_MNEMO_PUBLIC_BENEFIT_LANDING_NOT_FOUND`.
- `STR_MNEMO_REFERRAL_CODE_INVALID`.
- `STR_MNEMO_REFERRAL_CODE_EXPIRED`.
- `STR_MNEMO_REFERRAL_CODE_DISABLED`.
- `STR_MNEMO_BENEFIT_CONVERSION_REJECTED`.

Все эти коды должны быть добавлены во все поддерживаемые frontend i18n dictionaries в задаче реализации.

## Безопасность
- `code`, `variant`, `campaignId`, `routePath` и `anonymousSessionId` считаются untrusted input.
- Backend нормализует referral-код и не раскрывает приватные данные спонсора.
- Conversion endpoint не принимает user-facing тексты и не пишет произвольный payload как отображаемый контент.
- Ошибка аналитики не должна ломать основной landing payload.

## Swagger/OpenAPI
Так как модуль находится в monolith, runtime Swagger должен формироваться автоматически через springdoc из контроллеров `public-content` внутри owning package prefix. Для модуля сохраняется группа `/v3/api-docs/public-content` и Swagger UI `/swagger-ui/public-content`; manual per-endpoint Swagger routing не требуется.

## Версионная база
Новые технологии не вводятся. Контракт рассчитан на существующий Spring Boot monolith, Java/Maven baseline, Jackson DTO, Bean Validation, React/TypeScript frontend и текущий i18n-подход проекта.
