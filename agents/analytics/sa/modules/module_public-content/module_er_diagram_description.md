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

`public_page_config` имеет связь один-ко-многим с `public_content_block`.
`public_navigation_item` имеет self-reference связь для вложенных меню.

## Индексы
- `ux_public_page_config_page_key` по `page_key`.
- `ix_public_page_config_route_status` по `route_path`, `status`, `active_from`, `active_to`.
- `ux_public_content_block_page_key` по `page_config_id`, `block_key`.
- `ix_public_content_block_page_enabled_sort` по `page_config_id`, `enabled`, `sort_order`.
- `ix_public_navigation_area_audience_sort` по `area`, `audience`, `enabled`, `sort_order`.
- `ux_public_entry_point_key` по `entry_key`.
- `ix_public_entry_point_audience_sort` по `audience`, `enabled`, `sort_order`.

## Constraints
- `status` ограничен значениями `DRAFT`, `PUBLISHED`, `ARCHIVED`.
- `block_type` ограничен значениями `HERO`, `PROMO`, `QUICK_LINKS`, `COMMUNITY_FEED`, `FALLBACK`.
- `area` ограничен значениями `HEADER`, `FOOTER`, `MOBILE_MENU`.
- `target_type` ограничен значениями `INTERNAL_ROUTE`, `EXTERNAL_URL`.
- `audience` ограничен значениями `GUEST`, `AUTHENTICATED`, `CUSTOMER`, `PARTNER`, `ANY`.
- `sort_order` не должен быть отрицательным.
- `payload_json` не должен быть пустым для включенных content blocks.
