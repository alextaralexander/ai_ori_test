# Feature sequence description. Feature 027. Админ CMS

## Основной поток
Администратор открывает `/admin/cms`. Frontend получает session context и permission matrix из auth/RBAC слоя feature #24/#26. UI показывает только разрешенные CMS actions, но backend все равно проверяет actor scope на каждом endpoint-е.

Frontend загружает список материалов через `GET /api/admin-cms/materials`. `admin-cms controller` передает фильтры и actor context в service. Service применяет scope, status и search filters, читает материалы из storage и возвращает summary list.

Редактор создает материал через `POST /api/admin-cms/materials`. Service валидирует materialType, slug, language, audience, content blocks, SEO metadata, document metadata и relations. Для `DOCUMENT` проверяются `attachmentFileId`, `checksum`, `versionLabel` и `effectiveFrom`; физические файлы остаются за S3/MinIO abstraction, а CMS хранит только file reference. Успешное создание сохраняет material, version, blocks, SEO, relations и audit event, затем возвращает detail response и mnemonic `STR_MNEMO_ADMIN_CMS_MATERIAL_SAVED`.

Preview выполняется через `POST /materials/{materialId}/preview`. Service читает draft version и blocks, строит render model для desktop/mobile preview и не меняет lifecycle status. Публичная версия материала остается прежней.

Submit to review переводит draft в `IN_REVIEW` или `LEGAL_REVIEW`. Legal review обязателен для documents, campaign rules, legal FAQ и offer pages с legal flag. Reviewer вызывает `POST /materials/{materialId}/review`, а service проверяет reviewer scope, lifecycle, document validity и comment при rejection. Решение сохраняется в `admin_cms_review_decision` и audit trail.

Publish выполняется через `POST /materials/{materialId}/publish`. Если `publishAt` в будущем, материал получает `SCHEDULED`; иначе approved version становится `PUBLISHED`. Publication scheduler активирует scheduled version при наступлении времени и снимает публикацию при `unpublishAt`. Public-content projection получает только published render model и не видит draft/review entities.

Rollback через `POST /materials/{materialId}/versions/{versionId}/rollback` не изменяет историю. Service копирует выбранную версию в новую draft version, фиксирует `ADMIN_CMS_VERSION_ROLLED_BACK`, после чего редактор может повторно пройти review и publish.

Audit search через `GET /api/admin-cms/audit` возвращает immutable events без паролей, tokens, private storage paths и service secrets. Audit связывает actorUserId, materialId, versionId, actionCode, old/new diff, sourceRoute, correlationId и occurredAt.

## Ошибки и mnemonic-коды
- Нет доступа: HTTP 403 и `STR_MNEMO_ADMIN_CMS_ACCESS_DENIED`.
- Дубликат slug: HTTP 409 и `STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT`.
- Ошибка blocks или rich content: HTTP 400 и `STR_MNEMO_ADMIN_CMS_CONTENT_INVALID`.
- Ошибка документа или вложения: HTTP 400 и `STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID`.
- Ошибка SEO: HTTP 400 и `STR_MNEMO_ADMIN_CMS_SEO_INVALID`.
- Конфликт версии или lifecycle: HTTP 409 и `STR_MNEMO_ADMIN_CMS_VERSION_CONFLICT`.
- Недопустимая публикация: HTTP 400 и `STR_MNEMO_ADMIN_CMS_PUBLICATION_INVALID`.

Frontend локализует все mnemonic-коды через `resources_ru.ts` и `resources_en.ts`. Backend не передает hardcoded user-facing text.

## Интеграции
- Auth/RBAC: источник actor identity, roles, permission sets, responsibility scopes и elevated session requirement.
- S3/MinIO abstraction: хранение файлов документов и изображений; CMS хранит только безопасные file references и metadata.
- Public-content projection: получение только published render models для главной, новостей, FAQ, документов и offer pages.
- Audit trail: immutable фиксация всех изменений и отказов доступа.

## Версионная база
Фича использует существующий monolith baseline на 27.04.2026: Java/Spring Boot/Maven, REST controllers with springdoc-openapi, Liquibase XML, PostgreSQL-compatible ER model, frontend TypeScript/React/Ant Design and i18n dictionaries. Package ownership соблюдает `api/domain/db/impl` policy.