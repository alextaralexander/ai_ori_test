# Public-content module. ER-описание

Модуль `public-content` владеет публичной витриной, навигацией, новостями, контентными страницами, офферами, FAQ, информационными разделами, документами и benefit-лендингами. Текущая реализация использует in-memory репозиторий, а ER-модель фиксирует целевую структуру для Postgres.

## Состав модели
- `public_page`, `public_content_block`, `public_navigation_item`: главная, community, header, footer и entry points.
- `public_content_page`: новости, статьи, промо-материалы, rich text, вложения и CTA.
- `public_faq_item`: опубликованные вопросы с категорией, аудиторией, тегами и связями с инфо или документами.
- `public_info_section`: справочные страницы `/info/:section`.
- `public_document`: документы `/documents/:documentType`, актуальные версии и архив.
- `public_benefit_landing`: публичные benefit-лендинги beauty, business, member, VIP customer и app с route, SEO-ключами, default variant и campaignId.
- `public_benefit_landing_block`: упорядоченные блоки лендинга с A/B-вариантом, типом блока, i18n-ключами и структурированным `payload`.
- `public_benefit_cta`: CTA внутри блоков, target route, i18n label и признак сохранения referral/campaign context.
- `public_referral_code`: публичные referral/invite-коды со статусом, campaignId, периодом действия и безопасным публичным именем спонсора.
- `public_landing_conversion_event`: обезличенные события просмотров и CTA-кликов с landingType, variant, referralCode, campaignId, route и timestamp.

## Ограничения и индексы benefit-лендингов
- `public_benefit_landing.landing_type` и `route_path` уникальны среди активных опубликованных лендингов.
- `public_benefit_landing_block` имеет уникальность `landing_id + variant + block_key` и индекс `landing_id + variant + sort_order`.
- `public_benefit_cta` индексируется по `block_id + sort_order`.
- `public_referral_code.code` уникален и хранится в нормализованном виде; публичный payload не содержит email, телефон и internal id спонсора.
- `public_landing_conversion_event` индексируется по `landing_type + campaign_id + occurred_at` и `referral_code + occurred_at` для маркетинговой отчетности.

## Package ownership
- `api`: внешние DTO и REST response records.
- `domain`: доменные контракты репозиториев без сервисной логики.
- `impl/controller`: Spring MVC controllers модуля.
- `impl/service`: сервисы, in-memory репозиторий, фильтрация, fallback-логика и исключения.
- `db`: XML changelog-файлы feature #1, #2, #3 и feature #7.

## Версионный baseline
Для текущего потока используется baseline монолита: Java 25, Spring Boot 4.0.6, Maven 3.9.13, React 19.2.0, TypeScript 5.9.3, Ant Design 6.0.0. При внедрении физической БД модель должна быть оформлена отдельными Liquibase XML changelog-файлами по feature; feature #7 использует отдельный changelog в контуре `public-content`.
