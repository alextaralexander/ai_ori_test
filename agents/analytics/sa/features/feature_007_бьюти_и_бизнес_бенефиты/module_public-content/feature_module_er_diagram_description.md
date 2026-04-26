# Feature 007. Public-content ER-описание

## Назначение изменений
Feature #7 расширяет `public-content` контур сущностями для публичных benefit-лендингов Best Ori Gin: beauty, business, member, VIP customer и app. Модель поддерживает управляемые наборы блоков, CTA, A/B-варианты, referral-коды и запись conversion events. Данные используются для публичных маршрутов `/beauty-benefits`, `/beauty-benefits/{code}`, `/business-benefits`, `/business-benefits/{code}`, `/member-benefits`, `/vip-customer-benefits` и `/the-new-oriflame-app`.

## Таблица public_benefit_landing
- `id uuid` - первичный ключ.
- `landing_type varchar` - тип лендинга: `BEAUTY`, `BUSINESS`, `MEMBER`, `VIP_CUSTOMER`, `APP`.
- `route_path varchar` - публичный route без referral-кода.
- `default_variant varchar` - вариант блоков по умолчанию, например `DEFAULT`.
- `campaign_id varchar` - идентификатор активной кампании, передаваемый в CTA и аналитику.
- `seo_title_key varchar` - i18n-ключ SEO title.
- `seo_description_key varchar` - i18n-ключ SEO description.
- `published boolean` - признак доступности лендинга на публичном route.

Ограничения: `landing_type` и `route_path` должны быть уникальны среди опубликованных активных лендингов. `landing_type`, `route_path`, `default_variant`, `seo_title_key`, `seo_description_key` и `published` обязательны.

## Таблица public_benefit_landing_block
- `id uuid` - первичный ключ.
- `landing_id uuid` - внешний ключ на `public_benefit_landing.id`.
- `block_key varchar` - стабильный ключ блока.
- `block_type varchar` - тип блока: `HERO`, `BENEFIT_CARD`, `SCENARIO`, `DISCLAIMER`, `APP_PROMO`, `SEO_LINKS`.
- `variant varchar` - A/B-вариант, для которого доступен блок.
- `title_key varchar` - i18n-ключ заголовка.
- `body_key varchar` - i18n-ключ основного текста.
- `payload jsonb` - структурированные параметры блока: media key, bullets, link ids, условия отображения.
- `sort_order int` - порядок отображения.
- `display_condition varchar` - условие показа, например `ALWAYS`, `HAS_REFERRAL`, `NO_REFERRAL`, `INVALID_REFERRAL`.

Ограничения: `landing_id`, `block_key`, `block_type`, `variant` и `sort_order` обязательны. Для одного `landing_id` комбинация `variant + block_key` уникальна. Индекс по `landing_id, variant, sort_order` обеспечивает детерминированную выдачу.

## Таблица public_benefit_cta
- `id uuid` - первичный ключ.
- `block_id uuid` - внешний ключ на `public_benefit_landing_block.id`.
- `cta_type varchar` - тип действия: `REGISTER`, `REGISTER_PARTNER`, `OPEN_CATALOG`, `INSTALL_APP`, `CONTACT_SPONSOR`, `ACTIVATE_BENEFITS`.
- `label_key varchar` - i18n-ключ подписи CTA.
- `target_route varchar` - route или route-template целевого перехода.
- `preserve_referral_context boolean` - признак передачи `landingType`, `variant`, `code` и `campaignId`.
- `sort_order int` - порядок CTA внутри блока.

Ограничения: `block_id`, `cta_type`, `label_key`, `target_route` и `preserve_referral_context` обязательны. Индекс по `block_id, sort_order` сохраняет порядок CTA.

## Таблица public_referral_code
- `id uuid` - первичный ключ.
- `code varchar` - публичный referral/invite-код.
- `sponsor_public_name_key varchar` - i18n-ключ публичного имени или display-name спонсора без приватных данных.
- `status varchar` - статус: `ACTIVE`, `EXPIRED`, `DISABLED`.
- `campaign_id varchar` - кампания, в которой код применим.
- `valid_from timestamptz` - начало действия кода.
- `valid_to timestamptz` - окончание действия кода, может быть `null` для бессрочного кода.

Ограничения: `code` уникален, хранится в нормализованном безопасном виде и используется как untrusted input при входе из URL. Публичный payload не содержит email, телефона и внутренних идентификаторов спонсора.

## Таблица public_landing_conversion_event
- `id uuid` - первичный ключ.
- `landing_type varchar` - тип лендинга, на котором произошло событие.
- `variant varchar` - A/B-вариант.
- `referral_code varchar` - referral-код из контекста, nullable для базового лендинга.
- `campaign_id varchar` - кампания события.
- `cta_type varchar` - тип CTA или `VIEW` для просмотра.
- `route_path varchar` - route, на котором произошло событие.
- `occurred_at timestamptz` - время события.
- `anonymous_session_id varchar` - обезличенный идентификатор пользовательской сессии.

Ограничения: событие не хранит user-facing тексты и не принимает произвольные сообщения от клиента. Индексы по `landing_type, campaign_id, occurred_at` и `referral_code, occurred_at` нужны для отчетности маркетинга.

## Связи
- `public_benefit_landing 1:N public_benefit_landing_block`.
- `public_benefit_landing_block 1:N public_benefit_cta`.
- `public_referral_code 1:N public_landing_conversion_event` по нормализованному `referral_code`.
- `public_benefit_landing 1:N public_landing_conversion_event` по `landing_type`.

## Версионная база
Новые технологии не вводятся. Модель проектируется для существующего Spring Boot monolith, Java/Maven baseline, Hibernate-compatible JPA подхода, Liquibase XML changesets и PostgreSQL. Все предопределенные пользовательские сообщения должны доходить до frontend только как mnemonic-коды `STR_MNEMO_*`; пользовательские строки лендингов и CTA разрешаются через frontend i18n-словари или управляемый контент.
