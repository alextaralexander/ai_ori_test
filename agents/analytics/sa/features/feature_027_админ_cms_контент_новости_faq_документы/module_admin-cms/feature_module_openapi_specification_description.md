# Feature module OpenAPI description. admin-cms. Feature 027

## Назначение API
`admin-cms` предоставляет административный REST API под префиксом `/api/admin-cms` для управления публичным контентом Best Ori Gin: новости, статьи, страницы, FAQ, документы, offer pages и home blocks. Runtime Swagger должен быть доступен в группе монолитного модуля `admin-cms`: `/v3/api-docs/admin-cms` и `/swagger-ui/admin-cms`.

## Основные endpoints
- `GET /api/admin-cms/materials` - поиск материалов по типу, статусу, языку, аудитории, reviewer, publication window и search строке.
- `POST /api/admin-cms/materials` - создание draft материала и первой версии.
- `GET /api/admin-cms/materials/{materialId}` - чтение карточки материала, active/draft version, blocks, SEO, document metadata и audit flags.
- `PUT /api/admin-cms/materials/{materialId}` - обновление материала; для опубликованного материала создает или обновляет draft version, не меняя публичную версию.
- `POST /api/admin-cms/materials/{materialId}/submit-review` - перевод draft в review queue.
- `POST /api/admin-cms/materials/{materialId}/review` - approve/reject/return decision editorial или legal reviewer-а.
- `POST /api/admin-cms/materials/{materialId}/publish` - immediate publish или schedule approved version.
- `POST /api/admin-cms/materials/{materialId}/archive` - архивирование материала без удаления истории.
- `POST /api/admin-cms/materials/{materialId}/preview` - построение render model для draft версии без публикации.
- `GET /api/admin-cms/materials/{materialId}/versions` - список версий.
- `POST /api/admin-cms/materials/{materialId}/versions/{versionId}/rollback` - создание новой draft version из предыдущей версии.
- `GET /api/admin-cms/audit` - поиск audit events по materialId, actionCode, correlationId и периоду.

## DTO и статусы
Ключевые DTO:
- `AdminCmsMaterialSummary`: materialId, materialType, language, slug, title, status, publishAt, unpublishAt, reviewerUserId, updatedAt.
- `AdminCmsMaterialDetailResponse`: material summary, activeVersion, auditRecorded, messageCode.
- `AdminCmsMaterialVersion`: versionId, versionNumber, content blocks, SEO metadata, document metadata.
- `AdminCmsContentBlock`: blockType, sortOrder, payload.
- `AdminCmsSeoMetadata`: slug, title, description, canonicalUrl, robotsPolicy, breadcrumbTitle.
- `AdminCmsDocumentMetadata`: documentType, versionLabel, effectiveFrom, required, attachmentFileId, checksum.
- `AdminCmsReviewRequest`: decision и обязательный comment при reject.
- `AdminCmsPublishRequest`: publishAt и unpublishAt.

Материалы используют статусы `DRAFT`, `IN_REVIEW`, `LEGAL_REVIEW`, `APPROVED`, `SCHEDULED`, `PUBLISHED`, `ARCHIVED`, `REJECTED`. Backend валидирует переходы и возвращает `STR_MNEMO_ADMIN_CMS_PUBLICATION_INVALID` или `STR_MNEMO_ADMIN_CMS_VERSION_CONFLICT` при нарушении lifecycle.

## Валидации
- Slug уникален в рамках materialType + language; конфликт возвращает HTTP 409 и `STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT`.
- Blocks валидируются по разрешенному enum, sort order, payload schema, sanitized rich text, ссылкам на documents/products/catalogs и отсутствию executable HTML.
- Document publication требует documentType, versionLabel, effectiveFrom, attachmentFileId и checksum; нарушение возвращает `STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID`.
- SEO validation проверяет title, description, canonicalUrl, robotsPolicy и breadcrumbTitle; нарушение возвращает `STR_MNEMO_ADMIN_CMS_SEO_INVALID`.
- Параллельное редактирование, rollback устаревшей версии или publish неактуального draft возвращают HTTP 409 и `STR_MNEMO_ADMIN_CMS_VERSION_CONFLICT`.
- Access denied по permission matrix feature #26 возвращает HTTP 403 и `STR_MNEMO_ADMIN_CMS_ACCESS_DENIED`.

## Backend-to-frontend message contract
Backend возвращает только mnemonic codes:
- `STR_MNEMO_ADMIN_CMS_ACCESS_DENIED`
- `STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT`
- `STR_MNEMO_ADMIN_CMS_CONTENT_INVALID`
- `STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID`
- `STR_MNEMO_ADMIN_CMS_SEO_INVALID`
- `STR_MNEMO_ADMIN_CMS_VERSION_CONFLICT`
- `STR_MNEMO_ADMIN_CMS_PUBLICATION_INVALID`
- `STR_MNEMO_ADMIN_CMS_MATERIAL_SAVED`
- `STR_MNEMO_ADMIN_CMS_REVIEW_APPROVED`
- `STR_MNEMO_ADMIN_CMS_REVIEW_REJECTED`
- `STR_MNEMO_ADMIN_CMS_MATERIAL_PUBLISHED`
- `STR_MNEMO_ADMIN_CMS_MATERIAL_ARCHIVED`

Frontend обязан локализовать эти коды через `resources_ru.ts` и `resources_en.ts`. Backend не возвращает hardcoded пользовательские тексты в API response.

## Безопасность и аудит
Все endpoints требуют authenticated admin session. Controller делегирует проверку scopes в service/security слой; frontend availability не является источником истины. Audit event создается для material create/update, review, publish, archive, rollback, SEO change, attachment change и forbidden attempt. Payload audit diff не содержит private S3 paths, temporary tokens, service credentials, session tokens, passwords или MFA secrets.

## Связь с публичным контуром
Admin CMS API управляет draft/review/published состоянием. Публичные endpoints feature #001-#003 должны получать только published render models через service boundary или публичный projection. Draft, rejected, archived и future scheduled versions не доступны публичному frontend.

## Тестируемость
Feature API test должен начинаться с логина контент-администратора, затем проверять CRUD, validation, slug conflict, review approve/reject, schedule publish, document validation, preview, rollback, forbidden и audit search. UI test должен начинаться с логина контент-администратора и проверять основные test ids admin CMS экрана. End-to-end tests должны импортировать реальные managed tests feature #027, а не проверять строковые placeholders.