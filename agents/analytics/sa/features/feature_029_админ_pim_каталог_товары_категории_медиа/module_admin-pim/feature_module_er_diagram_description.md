# Feature module ER description. Feature 029 / module admin-pim

## Назначение модели
Модуль `admin-pim` хранит административную модель PIM-каталога Best Ori Gin: категории, бренды, товары, типизированные атрибуты, теги, медиа, рекомендации, merchandising blocks, import/export jobs и audit trail. Runtime package ownership: `api` содержит DTO, `domain` содержит JPA/domain entities и repository interfaces, `db` содержит только Liquibase XML, `impl` содержит controller/service/config/exception.

## Основные таблицы
- `admin_pim_category`: дерево категорий с `category_id`, `parent_id`, уникальным контекстом `parent_id + slug + locale`, статусом, audience, sortOrder, active window и SEO-полями. Для защиты дерева сервис запрещает перенос категории внутрь собственных descendants.
- `admin_pim_brand`: справочник брендов с уникальным `brand_code`, display name и status.
- `admin_pim_product`: карточка товара с уникальными `sku` и `article_code`, ссылкой на brand, locale, текстовыми полями, статусом, storefront visibility, `published_version_id`, optimistic `version`, timestamps.
- `admin_pim_product_category`: many-to-many связь товаров и категорий с признаком primary category.
- `admin_pim_attribute_definition`: типизированные attribute definitions (`TEXT`, `NUMBER`, `BOOLEAN`, `SELECT`, `MULTI_SELECT`) с unit code, filterable/searchable flags, allowed values JSON и status.
- `admin_pim_product_attribute`: значения атрибутов товара в `value_json`, валидируемые по definition type.
- `admin_pim_tag` и `admin_pim_product_tag`: теги товара, usage scope и связь many-to-many.
- `admin_pim_media`: metadata медиа и вложений товара с usage type, file name, mime type, size, checksum, alt text, locale, version number, status и `file_reference_id` для S3/MinIO.
- `admin_pim_recommendation`: связи source/target товаров для cross-sell, alternative, bundle, recommendation с sortOrder, active window и status.
- `admin_pim_merchandising_block`: код блока, i18n title key, placement, status и schedule; состав блока хранится в runtime DTO и может быть нормализован отдельной таблицей при расширении.
- `admin_pim_import_job`: import job с `idempotency_key`, data type, status, source file, row counters, validation summary JSON, actor и timestamp.
- `admin_pim_export_job`: export job с filter JSON, counters, checksum, file reference и actor.
- `admin_pim_audit_event`: immutable audit trail с actorUserId, actionCode, entityType, entityId, old/new JSON, reasonCode, correlationId, occurredAt.

## Индексы и ограничения
- Unique indexes: `admin_pim_product.sku`, `admin_pim_product.article_code`, `admin_pim_brand.brand_code`, `admin_pim_attribute_definition.attribute_code`, `admin_pim_tag.tag_code`, `admin_pim_merchandising_block.block_code`, `admin_pim_import_job.idempotency_key`.
- Category unique constraint: `(parent_id, slug, locale)` для неархивных категорий.
- Media uniqueness: активное главное изображение ограничивается сервисной проверкой по `product_id + usage_type + locale + status`, checksum conflict блокируется для активных файлов.
- Foreign keys связывают product-category, product-attribute, product-tag, product-media и recommendation source/target.
- Optimistic locking: `version` в category/product используется для конфликтов параллельного редактирования.

## Интеграции
- Публичный `catalog` и `product card` читают только published/active render data, сформированные из PIM.
- `admin-cms` использует published product/category references для product links и catalog links.
- `admin-campaign` и pricing-фичи #030/#031 используют только опубликованные и валидные товары.
- S3/MinIO хранит binary files, а `admin_pim_media.file_reference_id` содержит только ссылку на объект.

## Сообщения и безопасность
Backend возвращает frontend только mnemonic-коды `STR_MNEMO_ADMIN_PIM_*`: forbidden, duplicate SKU, category cycle, invalid publication window, main image required, media rejected, import validation failed, idempotency conflict. Пользовательские тексты локализуются во frontend dictionaries. Audit не хранит пароли, tokens, payment data или лишние персональные данные.

## Версионная база
Дата baseline: 27.04.2026. Стек: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML, PostgreSQL, React/TypeScript/Vite/Ant Design. Отклонения от latest-stable baseline не требуются.