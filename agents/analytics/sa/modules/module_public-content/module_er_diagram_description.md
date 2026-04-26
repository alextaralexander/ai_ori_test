# Module public-content. Полное ER описание

## Ответственность
Модуль `public-content` отвечает за публичные CMS-страницы, структуру контентных блоков, глобальную навигацию и быстрые entry points. Код модуля должен соблюдать backend package policy:
- `api`: DTO и внешние REST-контракты.
- `domain`: JPA entities и repository interfaces.
- `db`: только Liquibase XML changelogs.
- `impl`: controllers, services, validators, mappers, config и runtime orchestration в role-specific subpackages.

## Version baseline
На дату старта задачи 26.04.2026 целевой baseline: Java 25, Spring Boot 4.x stable, Maven 4.x stable, Hibernate 7.x stable, MapStruct 1.6.x stable, Lombok 1.18.x stable, Liquibase 5.x stable, PostgreSQL 18.x stable.

## Сущности и связи
Полная модель модуля включает:
- `public_page_config`: опубликованные публичные страницы.
- `public_content_block`: блоки страниц, связанные с `public_page_config`.
- `public_navigation_item`: пункты header/footer/mobile menu с поддержкой иерархии.
- `public_entry_point`: быстрые переходы для главной и персонализируемых зон.
- `public_news_item`: карточки ленты новостей для `/news`.
- `public_content_page`: динамические опубликованные страницы для `/content/:contentId`.
- `public_offer`: промо-предложения для `/offer/:offerId`.
- `public_content_section`: переиспользуемые секции контентных страниц и офферов.
- `public_content_attachment`: PDF и разрешенные вложения контентных страниц и офферов.
- `public_content_product_link`: связи контента и офферов с товарами или разделами каталога.

`public_page_config` имеет связь один-ко-многим с `public_content_block`.
`public_navigation_item` имеет self-reference связь для вложенных меню.
`public_news_item` ссылается на `public_content_page` через `content_id`, чтобы карточка новости открывала детальный материал.
`public_content_page` и `public_offer` владеют секциями, вложениями и product links через пару `owner_type` + `owner_id`.

## Поля feature #2
- `public_news_item`: `id`, `news_key`, `content_id`, `title_key`, `summary_key`, `image_url`, `category_key`, `status`, `active_from`, `active_to`, `sort_order`, `created_at`, `updated_at`.
- `public_content_page`: `id`, `content_id`, `template_code`, `title_key`, `description_key`, `seo_title_key`, `seo_description_key`, `canonical_url`, `breadcrumb_key`, `status`, `active_from`, `active_to`, `created_at`, `updated_at`.
- `public_offer`: `id`, `offer_id`, `title_key`, `summary_key`, `hero_image_url`, `primary_cta_label_key`, `primary_cta_target`, `status`, `active_from`, `active_to`, `created_at`, `updated_at`.
- `public_content_section`: `id`, `owner_type`, `owner_id`, `section_key`, `section_type`, `sort_order`, `payload_json`, `enabled`.
- `public_content_attachment`: `id`, `owner_type`, `owner_id`, `attachment_key`, `file_type`, `title_key`, `url`, `file_size_bytes`, `sort_order`, `enabled`.
- `public_content_product_link`: `id`, `owner_type`, `owner_id`, `product_ref`, `label_key`, `sort_order`, `enabled`.

## Индексы
- `ux_public_page_config_page_key` по `page_key`.
- `ix_public_page_config_route_status` по `route_path`, `status`, `active_from`, `active_to`.
- `ux_public_content_block_page_key` по `page_config_id`, `block_key`.
- `ix_public_content_block_page_enabled_sort` по `page_config_id`, `enabled`, `sort_order`.
- `ix_public_navigation_area_audience_sort` по `area`, `audience`, `enabled`, `sort_order`.
- `ux_public_entry_point_key` по `entry_key`.
- `ix_public_entry_point_audience_sort` по `audience`, `enabled`, `sort_order`.
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

## Constraints
- `status` ограничен значениями `DRAFT`, `PUBLISHED`, `ARCHIVED`.
- `block_type` ограничен значениями `HERO`, `PROMO`, `QUICK_LINKS`, `COMMUNITY_FEED`, `FALLBACK`.
- `area` ограничен значениями `HEADER`, `FOOTER`, `MOBILE_MENU`.
- `target_type` ограничен значениями `INTERNAL_ROUTE`, `EXTERNAL_URL`.
- `audience` ограничен значениями `GUEST`, `AUTHENTICATED`, `CUSTOMER`, `PARTNER`, `ANY`.
- `template_code` ограничен значениями `ARTICLE`, `LANDING`, `GUIDE`.
- `owner_type` ограничен значениями `CONTENT_PAGE`, `OFFER`.
- `section_type` ограничен значениями `HERO`, `RICH_TEXT`, `IMAGE`, `CTA`, `PDF`, `PRODUCT_LINKS`, `CONDITIONS`.
- `file_type` на старте ограничен значением `PDF`.
- `sort_order` не должен быть отрицательным.
- `payload_json` не должен быть пустым для включенных content blocks.
- `payload_json` не должен содержать hardcoded predefined user-facing текст; такие значения передаются i18n-ключами.
- Публичный контент возвращается только при `status = PUBLISHED`, `active_from <= now()` и пустом или будущем `active_to`.
