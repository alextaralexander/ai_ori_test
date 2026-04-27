# Feature 030. ER description for module admin-catalog

## Назначение модуля
`admin-catalog` отвечает за административное управление 21-дневными кампаниями каталога Best Ori Gin, PDF-материалами, изображениями страниц, hotspots, freeze window, rollover и audit trail. Модуль использует package ownership policy: DTO в `api`, целевая модель persistence в `domain`, Liquibase XML в `db`, runtime controller/service/config/exception в `impl/*`.

## Таблицы

### admin_catalog_campaign
- `campaign_id uuid PK` - идентификатор кампании.
- `campaign_code varchar(64)` - уникальный код активной/неархивной кампании, например `CAM-2026-05`.
- `name varchar(255)` - человекочитаемое имя кампании.
- `locale varchar(16)` - локаль выпуска.
- `audience varchar(64)` - аудитория: customer, partner или смешанная.
- `starts_at timestamptz`, `ends_at timestamptz` - 21-дневное окно кампании.
- `status varchar(32)` - `DRAFT`, `SCHEDULED`, `PUBLISHED`, `FROZEN`, `ARCHIVED`.
- `created_by uuid`, `updated_at timestamptz` - операционный контекст.

### admin_catalog_issue
- `issue_id uuid PK` - идентификатор выпуска.
- `campaign_id uuid FK -> admin_catalog_campaign.campaign_id`.
- `issue_code varchar(64)` - код выпуска, например `ISSUE-2026-05`.
- `status varchar(32)` - lifecycle выпуска.
- `current_flag boolean`, `next_flag boolean`, `archive_flag boolean` - роль выпуска в публичном каталоге.
- `publication_at timestamptz`, `archive_at timestamptz` - даты публикации и архивирования.
- `freeze_starts_at timestamptz` - начало freeze window.
- `rollover_window_starts_at timestamptz`, `rollover_window_ends_at timestamptz` - техническое окно переключения.

### admin_catalog_material
- `material_id uuid PK` - идентификатор файла.
- `issue_id uuid FK -> admin_catalog_issue.issue_id`.
- `material_type varchar(32)` - `PDF`, `COVER`, `PAGE_IMAGE`, `PROMO_ATTACHMENT`.
- `file_name varchar(255)`, `mime_type varchar(128)`, `size_bytes bigint`, `checksum varchar(128)`, `storage_key varchar(512)`.
- `version int` - версия файла в рамках выпуска и типа материала.
- `status varchar(32)` - `DRAFT`, `UPLOADED`, `APPROVED`, `REJECTED`, `ARCHIVED`.

### admin_catalog_page
- `page_id uuid PK` - идентификатор страницы.
- `issue_id uuid FK -> admin_catalog_issue.issue_id`.
- `page_number int` - номер страницы, уникальный в рамках выпуска.
- `image_url varchar(512)` - публичная или подписанная ссылка на изображение страницы.
- `width_px int`, `height_px int` - размер исходного изображения для валидации hotspots.
- `status varchar(32)` - lifecycle страницы.

### admin_catalog_hotspot
- `hotspot_id uuid PK` - идентификатор интерактивной зоны.
- `issue_id uuid FK -> admin_catalog_issue.issue_id`.
- `page_number int` - номер страницы выпуска.
- `product_id uuid`, `sku varchar(128)` - ссылка на published PIM-товар feature #29.
- `promo_code varchar(128)` - связь с промо-предложением.
- `x_ratio`, `y_ratio`, `width_ratio`, `height_ratio numeric(6,5)` - координаты как доли страницы от `0` до `1`.
- `sort_order int`, `status varchar(32)` - порядок и статус.

### admin_catalog_audit_event
- `audit_event_id uuid PK`.
- `entity_type varchar(64)`, `entity_id uuid`, `action_code varchar(96)`.
- `actor_user_id uuid`, `old_value jsonb`, `new_value jsonb`.
- `correlation_id varchar(128)`, `occurred_at timestamptz`.

## Ограничения и индексы
- Unique active index по `admin_catalog_campaign(campaign_code)` для неархивных кампаний.
- Unique index `admin_catalog_issue(issue_code)` и ограничение одного `current_flag=true` на locale/audience через сервисную проверку.
- Unique index `admin_catalog_page(issue_id, page_number)`.
- Unique active checksum index для `admin_catalog_material(checksum, status)` на approved/uploaded материалах.
- Check constraints для `x_ratio`, `y_ratio`, `width_ratio`, `height_ratio` в диапазоне `0..1`.
- Индексы поиска audit по `entity_type`, `entity_id`, `action_code`, `actor_user_id`, `correlation_id`, `occurred_at`.

## Версионная база
Проектная модель рассчитана на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML, PostgreSQL JSONB, React/TypeScript/Vite/Ant Design. Фича не вводит новый runtime stack.
