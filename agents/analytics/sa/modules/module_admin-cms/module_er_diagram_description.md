# Module ER description. admin-cms

## Назначение модели
Модуль `admin-cms` владеет административной моделью CMS: материалы, версии, structured content blocks, SEO metadata, связи с коммерческими сущностями, review decisions и immutable audit trail. Публичные модули feature #001-#003 получают только опубликованные render models и не читают draft/review данные напрямую.

## Таблица `admin_cms_material`
Основная карточка материала.

Поля:
- `material_id uuid PK` - стабильный идентификатор материала.
- `material_type varchar(32) not null` - `NEWS`, `ARTICLE`, `CONTENT_PAGE`, `FAQ_ITEM`, `DOCUMENT`, `OFFER_PAGE`, `HOME_BLOCK`.
- `language_code varchar(8) not null` - язык материала, например `ru` или `en`.
- `slug varchar(180) not null` - публичный slug в рамках типа и языка.
- `title varchar(240) not null` - редакционный заголовок.
- `summary varchar(1000)` - краткое описание для списков и preview.
- `audience varchar(64) not null` - `PUBLIC`, `CUSTOMER`, `PARTNER`, `EMPLOYEE`, `ADMIN` или специализированная аудитория.
- `status varchar(32) not null` - `DRAFT`, `IN_REVIEW`, `LEGAL_REVIEW`, `APPROVED`, `SCHEDULED`, `PUBLISHED`, `ARCHIVED`, `REJECTED`.
- `owner_user_id uuid not null` - владелец материала из admin RBAC/auth context.
- `reviewer_user_id uuid` - текущий reviewer, если материал в review.
- `active_version_id uuid` - опубликованная или текущая активная версия.
- `publish_at timestamptz`, `unpublish_at timestamptz`, `archived_at timestamptz` - окно публикации и архивирование.
- `created_at timestamptz not null`, `updated_at timestamptz not null`.
- `version_number bigint not null` - optimistic locking.

Ограничения и индексы:
- Unique index `ux_admin_cms_material_type_lang_slug(material_type, language_code, slug)`.
- Index `ix_admin_cms_material_status_publish(status, publish_at, unpublish_at)` для scheduler и публичного чтения.
- Index `ix_admin_cms_material_owner(owner_user_id)`.
- Check constraints для enum-полей и `unpublish_at > publish_at`, если обе даты указаны.

## Таблица `admin_cms_material_version`
Версия содержимого материала. Редактирование опубликованного материала создает новую draft version.

Поля:
- `version_id uuid PK`.
- `material_id uuid FK -> admin_cms_material.material_id`.
- `version_number int not null` - номер версии внутри материала.
- `status varchar(32) not null` - состояние версии в lifecycle.
- `title`, `summary` - snapshot заголовка и анонса.
- `document_type varchar(64)`, `document_version_label varchar(64)`, `document_effective_from date`, `document_required boolean`, `attachment_file_id varchar(120)`, `checksum varchar(128)` - обязательны для materialType `DOCUMENT`.
- `created_by_user_id uuid not null`, `created_at timestamptz not null`.
- `approved_by_user_id uuid`, `approved_at timestamptz`.

Ограничения:
- Unique index `ux_admin_cms_version_material_number(material_id, version_number)`.
- FK с cascade delete запрещается на уровне сервиса: версии не удаляются через UI.
- Check: published/legal document не может иметь пустой `attachment_file_id`, `document_version_label`, `document_effective_from`.

## Таблица `admin_cms_content_block`
Структурированные блоки версии.

Поля:
- `block_id uuid PK`.
- `version_id uuid FK -> admin_cms_material_version.version_id`.
- `block_type varchar(32) not null` - `RICH_TEXT`, `HERO`, `IMAGE`, `FAQ_GROUP`, `DOCUMENT_LINK`, `PRODUCT_LINK`, `CATALOG_LINK`, `CTA`, `LEGAL_NOTICE`.
- `sort_order int not null`.
- `payload_json jsonb not null` - sanitized payload блока без executable HTML.
- `created_at timestamptz not null`.

Индексы и ограничения:
- Unique index `ux_admin_cms_block_version_order(version_id, sort_order)`.
- GIN index по `payload_json` допускается для будущего поиска, если подтверждено нагрузкой.
- Backend validator проверяет schema каждого block type, обязательные поля и безопасный rich text.

## Таблица `admin_cms_seo_metadata`
SEO snapshot версии.

Поля:
- `seo_id uuid PK`.
- `version_id uuid FK unique`.
- `seo_slug`, `seo_title`, `seo_description`, `canonical_url`, `robots_policy`, `breadcrumb_title`, `social_image_file_id`.

Ограничения:
- Unique FK `version_id` гарантирует один SEO snapshot на версию.
- Validator проверяет canonical URL, длины title/description и допустимые robots policies.

## Таблица `admin_cms_relation`
Связи материала с товарами, каталогами, кампаниями, benefit programs и другими материалами.

Поля:
- `relation_id uuid PK`.
- `material_id uuid FK`.
- `relation_type varchar(32) not null` - `PRODUCT`, `PRODUCT_CATEGORY`, `CATALOG`, `CAMPAIGN`, `BENEFIT_PROGRAM`, `RELATED_MATERIAL`.
- `target_id varchar(120) not null`.
- `sort_order int not null`.

Индексы:
- Index `ix_admin_cms_relation_target(relation_type, target_id)`.
- Unique index `ux_admin_cms_relation(material_id, relation_type, target_id)`.

## Таблица `admin_cms_review_decision`
Решения editorial/legal review.

Поля:
- `decision_id uuid PK`.
- `version_id uuid FK`.
- `reviewer_user_id uuid not null`.
- `decision varchar(32) not null` - `APPROVED`, `REJECTED`, `RETURNED_TO_DRAFT`.
- `comment varchar(1000)` - обязателен при reject.
- `decided_at timestamptz not null`.

## Таблица `admin_cms_audit_event`
Immutable журнал CMS операций.

Поля:
- `event_id uuid PK`.
- `material_id uuid FK`, `version_id uuid FK`.
- `actor_user_id uuid not null`.
- `action_code varchar(80) not null` - например `ADMIN_CMS_MATERIAL_CREATED`, `ADMIN_CMS_REVIEW_APPROVED`, `ADMIN_CMS_MATERIAL_PUBLISHED`, `ADMIN_CMS_VERSION_ROLLED_BACK`.
- `old_value_json jsonb`, `new_value_json jsonb` - diff без секретов, token-ов и приватных storage paths.
- `comment varchar(1000)`, `source_route varchar(240)`, `correlation_id varchar(120)`, `occurred_at timestamptz not null`.

Индексы:
- `ix_admin_cms_audit_material(material_id, occurred_at desc)`.
- `ix_admin_cms_audit_actor(actor_user_id, occurred_at desc)`.
- `ix_admin_cms_audit_correlation(correlation_id)`.
- `ix_admin_cms_audit_action(action_code, occurred_at desc)`.

## Package ownership
- `api`: REST DTOs and response contracts.
- `domain`: JPA entities and repository interfaces only.
- `db`: dedicated Liquibase XML changelog `feature_027_admin_cms.xml`.
- `impl/controller`: Spring MVC controller.
- `impl/service`: CMS orchestration and in-memory/default service implementation for current monolith baseline.
- `impl/validator`: lifecycle, block, SEO and document validators.
- `impl/mapper`: DTO mapping if MapStruct is introduced for this module.
- `impl/exception`: access, validation and conflict exceptions.
- `impl/config`: module registration and Swagger group integration.

## Версионная база
На дату запуска 27.04.2026 модуль использует существующий monolith baseline Java/Spring Boot/Maven, Liquibase XML, Hibernate/JPA, Lombok/MapStruct where applicable, PostgreSQL jsonb-compatible schema and S3/MinIO abstraction for document attachments. Если runtime implementation временно использует in-memory storage, ER-модель остается целевой структурой для dedicated Liquibase XML и последующей persistence wiring.