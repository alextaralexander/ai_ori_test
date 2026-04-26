# Feature 002. ER описание изменений модуля public-content

## Ответственность изменения
Feature #2 расширяет модуль `public-content` новостями, динамическими контентными страницами и промо-офферами. Модуль остается владельцем публичного CMS-контента и должен соблюдать backend package policy:
- `api`: DTO и внешние REST-контракты.
- `domain`: JPA entities и repository interfaces.
- `db`: только Liquibase XML changelogs.
- `impl`: controllers, services, mappers, validators, config и orchestration в role-specific subpackages.

## Version baseline
На дату старта задачи 26.04.2026 целевой baseline: Java 25, Spring Boot 4.x stable, Maven 4.x stable, Hibernate 7.x stable, MapStruct 1.6.x stable, Lombok 1.18.x stable, Liquibase 5.x stable, PostgreSQL 18.x stable.

## Новые сущности

### public_news_item
Новостная карточка для маршрута `/news`.
- `id uuid` - первичный ключ.
- `news_key varchar(120)` - устойчивый ключ новости, уникальный.
- `content_id varchar(120)` - ссылка на детальный материал `public_content_page.content_id`.
- `title_key varchar(160)` - i18n-ключ заголовка.
- `summary_key varchar(160)` - i18n-ключ анонса, nullable.
- `image_url varchar(512)` - публичный URL изображения, nullable.
- `category_key varchar(120)` - i18n-ключ категории, nullable.
- `status varchar(24)` - `DRAFT`, `PUBLISHED`, `ARCHIVED`.
- `active_from timestamptz` - начало публикации.
- `active_to timestamptz` - окончание публикации, nullable.
- `sort_order integer` - порядок в ленте, неотрицательный.
- `created_at timestamptz`, `updated_at timestamptz` - аудит.

### public_content_page
Динамическая контентная страница для `/content/:contentId`.
- `id uuid` - первичный ключ.
- `content_id varchar(120)` - публичный идентификатор, уникальный.
- `template_code varchar(60)` - шаблон рендера: `ARTICLE`, `LANDING`, `GUIDE`.
- `title_key varchar(160)` - i18n-ключ заголовка.
- `description_key varchar(160)` - i18n-ключ описания, nullable.
- `seo_title_key varchar(160)` и `seo_description_key varchar(160)` - SEO i18n-ключи, nullable.
- `canonical_url varchar(512)` - canonical URL, nullable.
- `breadcrumb_key varchar(160)` - i18n-ключ breadcrumb, nullable.
- `status varchar(24)`, `active_from timestamptz`, `active_to timestamptz`.
- `created_at timestamptz`, `updated_at timestamptz`.

### public_offer
Промо-предложение для `/offer/:offerId`.
- `id uuid` - первичный ключ.
- `offer_id varchar(120)` - публичный идентификатор, уникальный.
- `title_key varchar(160)` - i18n-ключ заголовка.
- `summary_key varchar(160)` - i18n-ключ краткого описания.
- `hero_image_url varchar(512)` - hero-изображение.
- `primary_cta_label_key varchar(160)` - i18n-ключ основного CTA.
- `primary_cta_target varchar(512)` - маршрут платформы или разрешенная внешняя ссылка.
- `status varchar(24)`, `active_from timestamptz`, `active_to timestamptz`.
- `created_at timestamptz`, `updated_at timestamptz`.

### public_content_section
Переиспользуемый блок контента для страниц и офферов.
- `id uuid` - первичный ключ.
- `owner_type varchar(24)` - `CONTENT_PAGE` или `OFFER`.
- `owner_id uuid` - идентификатор владельца.
- `section_key varchar(120)` - ключ блока в рамках владельца.
- `section_type varchar(40)` - `HERO`, `RICH_TEXT`, `IMAGE`, `CTA`, `PDF`, `PRODUCT_LINKS`, `CONDITIONS`.
- `sort_order integer` - порядок блока.
- `payload_json jsonb` - структурные данные блока без hardcoded user-facing текстов.
- `enabled boolean` - признак отображения.

### public_content_attachment
PDF и другие разрешенные вложения.
- `id uuid` - первичный ключ.
- `owner_type varchar(24)`, `owner_id uuid` - владелец.
- `attachment_key varchar(120)` - ключ вложения.
- `file_type varchar(24)` - `PDF` на старте feature #2.
- `title_key varchar(160)` - i18n-ключ названия.
- `url varchar(512)` - публичный URL файла.
- `file_size_bytes bigint` - размер, nullable.
- `sort_order integer`, `enabled boolean`.

### public_content_product_link
Связь материала с товаром или каталогом.
- `id uuid` - первичный ключ.
- `owner_type varchar(24)`, `owner_id uuid` - владелец.
- `product_ref varchar(120)` - внешний идентификатор товара или категории каталога.
- `label_key varchar(160)` - i18n-ключ подписи, nullable.
- `sort_order integer`, `enabled boolean`.

## Индексы и ограничения
- `ux_public_news_item_news_key` по `news_key`.
- `ix_public_news_item_publication` по `status`, `active_from`, `active_to`, `sort_order`.
- `ux_public_content_page_content_id` по `content_id`.
- `ix_public_content_page_publication` по `status`, `active_from`, `active_to`.
- `ux_public_offer_offer_id` по `offer_id`.
- `ix_public_offer_publication` по `status`, `active_from`, `active_to`.
- `ux_public_content_section_owner_key` по `owner_type`, `owner_id`, `section_key`.
- `ix_public_content_section_owner_sort` по `owner_type`, `owner_id`, `enabled`, `sort_order`.
- `ux_public_content_attachment_owner_key` по `owner_type`, `owner_id`, `attachment_key`.
- `ix_public_content_attachment_owner_sort` по `owner_type`, `owner_id`, `enabled`, `sort_order`.
- `ix_public_content_product_link_owner_sort` по `owner_type`, `owner_id`, `enabled`, `sort_order`.

## Правила публикации
Материал публичен только если `status = PUBLISHED`, `active_from <= now()` и `active_to` пустой или больше текущего времени. Неопубликованный материал не должен возвращать body контента по прямой ссылке.

## Контракт локализации
Все пользовательские подписи представлены i18n-ключами. Backend не передает predefined user-facing тексты во frontend. Для ошибок и недоступности используются mnemonic-коды `STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND` и `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`.
