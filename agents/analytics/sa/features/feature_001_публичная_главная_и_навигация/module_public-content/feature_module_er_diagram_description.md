# ER описание feature 001 для модуля public-content

## Назначение модуля
`public-content` хранит и отдает публичную CMS-конфигурацию для главной страницы, community-страницы, глобальной навигации, футера и быстрых entry points. Модуль не хранит пользовательские приватные данные; персонализация выполняется через аудиторию и признаки доступности entry points.

## Version baseline
На дату старта задачи 26.04.2026 целевой baseline: Java 25, Spring Boot 4.x stable, Maven 4.x stable, Hibernate 7.x stable, Liquibase 5.x stable, PostgreSQL 18.x stable. Если фактическая реализация вынужденно использует более ранние версии из-за совместимости, причина фиксируется в implementation artifacts.

## Таблица public_page_config
- `id uuid`: первичный ключ.
- `page_key varchar(64)`: стабильный ключ страницы, например `HOME` или `COMMUNITY`; уникальный.
- `route_path varchar(128)`: публичный маршрут, например `/`, `/home`, `/community`; индексируется.
- `title_i18n_key varchar(128)`: i18n-ключ заголовка страницы.
- `status varchar(32)`: `DRAFT`, `PUBLISHED`, `ARCHIVED`.
- `active_from timestamptz`: начало публикации.
- `active_to timestamptz null`: окончание публикации.
- `created_at timestamptz`, `updated_at timestamptz`: аудит изменения.

## Таблица public_content_block
- `id uuid`: первичный ключ.
- `page_config_id uuid`: внешний ключ на `public_page_config(id)` с каскадным удалением блоков при удалении страницы.
- `block_key varchar(64)`: ключ блока внутри страницы, например `HOME_HERO`.
- `block_type varchar(32)`: `HERO`, `PROMO`, `QUICK_LINKS`, `COMMUNITY_FEED`, `FALLBACK`.
- `sort_order integer`: порядок отображения.
- `enabled boolean`: признак активности блока.
- `payload_json jsonb`: структурированный payload блока, содержащий i18n-ключи, media references, CTA и настройки аудитории.
- `created_at timestamptz`, `updated_at timestamptz`: аудит изменения.
- Уникальность: `(page_config_id, block_key)`.
- Индекс: `(page_config_id, enabled, sort_order)`.

## Таблица public_navigation_item
- `id uuid`: первичный ключ.
- `parent_id uuid null`: ссылка на родительский пункт навигации для вложенного меню.
- `area varchar(32)`: зона отображения, например `HEADER`, `FOOTER`, `MOBILE_MENU`.
- `item_key varchar(64)`: стабильный ключ пункта.
- `label_i18n_key varchar(128)`: i18n-ключ подписи.
- `target_type varchar(32)`: `INTERNAL_ROUTE` или `EXTERNAL_URL`.
- `target_value varchar(256)`: путь или разрешенная внешняя ссылка.
- `sort_order integer`: порядок отображения.
- `audience varchar(32)`: `GUEST`, `AUTHENTICATED`, `PARTNER`, `CUSTOMER`, `ANY`.
- `enabled boolean`: признак активности.
- Индекс: `(area, audience, enabled, sort_order)`.

## Таблица public_entry_point
- `id uuid`: первичный ключ.
- `entry_key varchar(64)`: стабильный ключ entry point; уникальный.
- `label_i18n_key varchar(128)`: i18n-ключ названия.
- `description_i18n_key varchar(128) null`: i18n-ключ краткого описания.
- `target_route varchar(256)`: внутренний маршрут.
- `audience varchar(32)`: целевая аудитория.
- `sort_order integer`: порядок отображения.
- `enabled boolean`: признак активности.
- Индекс: `(audience, enabled, sort_order)`.

## Ограничения
- Все predefined user-facing тексты хранятся как i18n-ключи или mnemonic-коды, а не как hardcoded backend text.
- `payload_json` должен проходить валидацию схемы по `block_type`.
- Внешние ссылки допускаются только из разрешенного списка доменов.
- Удаление страницы не должно оставлять orphan-блоки.
