# Feature 006. Module catalog. Описание ER-изменений

## Назначение
Feature #6 расширяет module `catalog` в monolith и добавляет модель цифровых выпусков каталога Best Ori Gin. Модель покрывает текущий и следующий трехнедельный выпуск, страницы каталога, связанные PDF-материалы, интерактивные зоны перехода в карточки товаров и ролевую видимость.

## Backend package ownership
- JPA entities и repository interfaces должны находиться в `backend/monolith/monolith-app/src/main/java/com/bestorigin/monolith/catalog/domain`.
- Liquibase XML changelog должен находиться в owning db package/resources модуля catalog: `backend/monolith/monolith-app/src/main/resources/db/changelog/catalog`.
- Controllers, services, mappers, validators и exception handlers должны находиться в `backend/monolith/monolith-app/src/main/java/com/bestorigin/monolith/catalog/impl/<role>`.
- External REST DTO и module-facing API types должны находиться в `backend/monolith/monolith-app/src/main/java/com/bestorigin/monolith/catalog/api`.

## Таблица catalog_digital_issue
Хранит выпуск цифрового каталога.

Поля:
- `issue_id varchar(64)` - primary key, стабильный идентификатор выпуска.
- `issue_code varchar(64)` - бизнес-код выпуска, уникален.
- `title varchar(255)` - название выпуска для отображения через данные каталога.
- `period_type varchar(32)` - тип периода: `CURRENT`, `NEXT`, `ARCHIVE`.
- `period_start date` - дата начала действия выпуска.
- `period_end date` - дата окончания действия выпуска.
- `publication_status varchar(32)` - статус: `DRAFT`, `SCHEDULED`, `PUBLISHED`, `ARCHIVED`.
- `preview_allowed_from timestamp` - момент, с которого следующий выпуск доступен ролям предпросмотра.
- `sort_order integer` - порядок отображения при списках.
- `created_at timestamp`, `updated_at timestamp` - технические даты.

Ключи и ограничения:
- Primary key: `pk_catalog_digital_issue(issue_id)`.
- Unique key: `uk_catalog_digital_issue_code(issue_code)`.
- Check: `period_end >= period_start`.
- Check: `period_type in ('CURRENT','NEXT','ARCHIVE')`.
- Check: `publication_status in ('DRAFT','SCHEDULED','PUBLISHED','ARCHIVED')`.

Индексы:
- `ix_catalog_digital_issue_period_status(period_type, publication_status, period_start, period_end)`.
- `ix_catalog_digital_issue_preview(preview_allowed_from)`.

## Таблица catalog_digital_page
Хранит страницы выпуска для viewer и переходов по страницам.

Поля:
- `page_id varchar(64)` - primary key.
- `issue_id varchar(64)` - foreign key на `catalog_digital_issue.issue_id`.
- `page_number integer` - номер страницы внутри выпуска.
- `image_url varchar(1024)` - URL растрового preview страницы.
- `thumbnail_url varchar(1024)` - URL миниатюры.
- `width_px integer`, `height_px integer` - размер страницы для корректного расчета hotspots.
- `created_at timestamp` - дата загрузки страницы.

Ключи и ограничения:
- Primary key: `pk_catalog_digital_page(page_id)`.
- Foreign key: `fk_catalog_digital_page_issue(issue_id)`.
- Unique key: `uk_catalog_digital_page_number(issue_id, page_number)`.
- Check: `page_number > 0`, `width_px > 0`, `height_px > 0`.

Индексы:
- `ix_catalog_digital_page_issue(issue_id, page_number)`.

## Таблица catalog_digital_material
Хранит PDF и связанные материалы выпуска.

Поля:
- `material_id varchar(64)` - primary key.
- `issue_id varchar(64)` - foreign key на выпуск.
- `material_type varchar(32)` - тип: `MAIN_CATALOG`, `BROCHURE`, `PROMO_LEAFLET`, `DOCUMENT`.
- `title varchar(255)` - название материала.
- `file_url varchar(1024)` - URL PDF/файла в S3/MinIO или публичном CDN-контуре.
- `file_size_bytes bigint` - размер файла, nullable.
- `publication_status varchar(32)` - статус материала.
- `allow_download boolean` - разрешено ли скачивание.
- `allow_share boolean` - разрешено ли создание share-ссылки.
- `sort_order integer` - порядок в списке материалов.
- `created_at timestamp`, `updated_at timestamp` - технические даты.

Ключи и ограничения:
- Primary key: `pk_catalog_digital_material(material_id)`.
- Foreign key: `fk_catalog_digital_material_issue(issue_id)`.
- Check: `material_type in ('MAIN_CATALOG','BROCHURE','PROMO_LEAFLET','DOCUMENT')`.
- Check: `publication_status in ('DRAFT','SCHEDULED','PUBLISHED','ARCHIVED')`.
- Check: `file_size_bytes is null or file_size_bytes >= 0`.

Индексы:
- `ix_catalog_digital_material_issue_status(issue_id, publication_status, sort_order)`.

## Таблица catalog_digital_page_hotspot
Хранит интерактивные зоны страниц каталога.

Поля:
- `hotspot_id varchar(64)` - primary key.
- `page_id varchar(64)` - foreign key на страницу.
- `product_code varchar(64)` - foreign key на `catalog_product.product_code`.
- `x_percent numeric(6,3)`, `y_percent numeric(6,3)` - позиция левого верхнего угла в процентах.
- `width_percent numeric(6,3)`, `height_percent numeric(6,3)` - размер зоны в процентах.
- `sort_order integer` - порядок обхода keyboard/focus.

Ключи и ограничения:
- Primary key: `pk_catalog_digital_page_hotspot(hotspot_id)`.
- Foreign keys: `fk_catalog_digital_hotspot_page(page_id)`, `fk_catalog_digital_hotspot_product(product_code)`.
- Check для всех координат: значение от `0` до `100`.
- Check: `width_percent > 0`, `height_percent > 0`.

Индексы:
- `ix_catalog_digital_hotspot_page(page_id, sort_order)`.
- `ix_catalog_digital_hotspot_product(product_code)`.

## Таблица catalog_digital_visibility
Описывает ролевую видимость выпуска или конкретного материала.

Поля:
- `visibility_id varchar(64)` - primary key.
- `issue_id varchar(64)` - foreign key на выпуск.
- `material_id varchar(64)` - optional foreign key на материал; null означает правило на выпуск.
- `role_code varchar(64)` - роль: `GUEST`, `CUSTOMER`, `PARTNER`, `CONTENT_MANAGER`, `CATALOG_MANAGER`.
- `can_preview boolean` - может видеть выпуск или материал.
- `can_download boolean` - может скачивать материал.
- `can_share boolean` - может создавать share-ссылку.

Ключи и ограничения:
- Primary key: `pk_catalog_digital_visibility(visibility_id)`.
- Foreign keys: `fk_catalog_digital_visibility_issue(issue_id)`, `fk_catalog_digital_visibility_material(material_id)`.
- Unique key: `uk_catalog_digital_visibility_scope(issue_id, material_id, role_code)`.
- Check: `role_code` входит в поддерживаемый набор ролей.

Индексы:
- `ix_catalog_digital_visibility_role(role_code, can_preview)`.
- `ix_catalog_digital_visibility_material(material_id, role_code)`.

## Связи
- `catalog_digital_issue 1:N catalog_digital_page`.
- `catalog_digital_issue 1:N catalog_digital_material`.
- `catalog_digital_issue 1:N catalog_digital_visibility`.
- `catalog_digital_material 1:N catalog_digital_visibility`.
- `catalog_digital_page 1:N catalog_digital_page_hotspot`.
- `catalog_product 1:N catalog_digital_page_hotspot` через `product_code`.

## Mnemonic-коды
Backend не передает hardcoded user-facing text. Для контролируемых состояний используются:
- `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND`.
- `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN`.
- `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`.
- `STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED`.
- `STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED`.

## Версионная база
Новые runtime-технологии не вводятся. ER-модель рассчитана на текущий Spring Boot monolith, Hibernate/JPA, Liquibase XML и PostgreSQL baseline репозитория. Для S3/MinIO используются URL/keys материалов без внедрения отдельного storage service в рамках feature #6.
