# Acceptance criteria. Feature 027. Админ: CMS, контент, новости, FAQ, документы

## Обязательные критерии доступа и безопасности
1. Backend предоставляет admin CMS API для управления материалами типов `NEWS`, `ARTICLE`, `CONTENT_PAGE`, `FAQ_ITEM`, `DOCUMENT`, `OFFER_PAGE`, `HOME_BLOCK` и связанными версиями, блоками, вложениями, review decisions, publication windows, SEO metadata и audit events.
2. Каждый admin CMS endpoint проверяет authenticated session, admin permission scopes из feature #26, область ответственности по типу материала, elevated session для рискованных действий и возвращает HTTP 403 с `STR_MNEMO_ADMIN_CMS_ACCESS_DENIED` при запрете.
3. Backend не передает во frontend predefined user-facing text; ошибки, предупреждения, статусы и validation results передаются mnemonic-кодами `STR_MNEMO_ADMIN_CMS_*`.
4. Storage paths, temporary upload tokens, private S3/MinIO keys, полные URL приватных файлов и служебные credentials не возвращаются во frontend и не пишутся в audit payload в открытом виде.
5. Все создание, редактирование, review, approval, rejection, scheduling, publication, archive, rollback, attachment change и SEO change фиксируются в audit trail с actorUserId, materialId, versionId, actionCode, oldValue, newValue, comment, sourceRoute, correlationId и occurredAt.
6. Публикация юридически значимых документов, правил кампаний и offer pages невозможна без approval юридического reviewer-а.

## Критерии управления материалами
1. CMS list поддерживает фильтры materialType, status, language, audience, ownerUserId, reviewerUserId, tag, linkedProductId, linkedCatalogId, publishAt range, updatedAt range и полнотекстовый поиск по title, slug, summary и tags.
2. Создание материала требует materialType, title, language, audience, ownerUserId и хотя бы один содержательный content block, кроме `DOCUMENT`, где обязательны document metadata и attachment reference.
3. Slug уникален в рамках materialType и language; дубликат возвращает HTTP 409 и `STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT`.
4. Материал поддерживает статусы `DRAFT`, `IN_REVIEW`, `LEGAL_REVIEW`, `APPROVED`, `SCHEDULED`, `PUBLISHED`, `ARCHIVED`, `REJECTED`; backend валидирует допустимые переходы.
5. Редактирование опубликованного материала создает новую draft version и не меняет публичную версию до approval/publication.
6. Архивирование скрывает материал из публичных списков, сохраняет историю версий и audit trail, а прямой публичный URL получает controlled not-found или archived state без раскрытия черновика.
7. Rollback создает новую draft version на основе выбранной предыдущей версии и фиксирует `ADMIN_CMS_VERSION_ROLLED_BACK`.

## Критерии block-based editor и rich content
1. Поддерживаются blocks `RICH_TEXT`, `HERO`, `IMAGE`, `FAQ_GROUP`, `DOCUMENT_LINK`, `PRODUCT_LINK`, `CATALOG_LINK`, `CTA`, `LEGAL_NOTICE` с явной схемой payload для каждого типа.
2. Backend валидирует порядок, обязательность и совместимость blocks: пустой body, неизвестный block type, небезопасный HTML, битые ссылки и превышение лимитов возвращают HTTP 400 и `STR_MNEMO_ADMIN_CMS_CONTENT_INVALID`.
3. Rich text хранится как структурированный sanitized payload, а не как произвольный executable HTML; frontend рендерит только разрешенные блоки.
4. Links на products, catalogs, documents и related materials сохраняются как stable identifiers, чтобы frontend мог строить переходы без hardcoded URL.
5. Preview endpoint возвращает render model текущей draft version без изменения статуса материала и без публикации.

## Критерии публикации, review и moderation workflow
1. Editor может отправить draft в `IN_REVIEW`; материал получает reviewer queue item и сохраняет номер версии.
2. Юридически значимый materialType или материал с legal flag переходит в `LEGAL_REVIEW`; публикация без legal approval запрещена.
3. Reviewer approve переводит версию в `APPROVED` или `SCHEDULED` при наличии будущего publishAt; reject переводит в `REJECTED` с обязательным comment.
4. Publication scheduler публикует `SCHEDULED` версии при наступлении publishAt и снимает публикацию при unpublishAt.
5. Конфликт параллельного редактирования по versionNumber или updatedAt возвращает HTTP 409 и `STR_MNEMO_ADMIN_CMS_VERSION_CONFLICT`.
6. Повторный approve, publish, archive или rollback должен быть идемпотентным там, где бизнес-состояние уже соответствует запросу, и возвращать актуальное состояние.

## Критерии документов и вложений
1. Документ содержит documentType, versionLabel, effectiveFrom, required flag, legalOwner, attachmentFileId, checksum, fileName, mimeType и fileSize.
2. Загрузка или замена файла выполняется только через существующий S3/MinIO abstraction; CMS хранит file reference и metadata, но не приватный storage path.
3. Документ без attachmentFileId, версии или effectiveFrom не может быть опубликован; backend возвращает `STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID`.
4. Поддерживается архив версий документа; публичный контур показывает актуальную опубликованную версию и дату вступления в силу.
5. Attachment validation запрещает неподдерживаемые MIME types, слишком большие файлы и файлы без checksum.

## Критерии SEO и связей
1. SEO metadata содержит slug, title, description, canonicalUrl, robotsPolicy, breadcrumbTitle и optional social preview image.
2. Некорректный canonicalUrl, пустой title, слишком длинный description или запрещенный robotsPolicy возвращают HTTP 400 и `STR_MNEMO_ADMIN_CMS_SEO_INVALID`.
3. Материал может быть связан с productId, productCategoryId, catalogId, campaignId, benefitProgramId или relatedMaterialId; backend проверяет формат identifiers и не раскрывает недоступные сущности.
4. Публичные feature #001-#003 получают только published render models и не читают draft/review данные.

## Критерии frontend и i18n
1. Admin CMS UI содержит разделы: список материалов, редактор материала, review queue, preview, versions, publication schedule, attachments/documents, SEO panel и audit trail.
2. Все новые user-facing строки для заголовков, таблиц, фильтров, форм, validation states, кнопок, drawer/modal, preview, audit, empty states и ошибок вынесены в `resources_ru.ts` и `resources_en.ts`.
3. UI разрешает backend mnemonic-коды `STR_MNEMO_ADMIN_CMS_ACCESS_DENIED`, `STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT`, `STR_MNEMO_ADMIN_CMS_CONTENT_INVALID`, `STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID`, `STR_MNEMO_ADMIN_CMS_SEO_INVALID`, `STR_MNEMO_ADMIN_CMS_VERSION_CONFLICT`, `STR_MNEMO_ADMIN_CMS_PUBLICATION_INVALID` через i18n dictionaries.
4. React-компоненты не типизируют возвращаемое значение как `JSX.Element`; используется inference или `ReactElement` из `react`.
5. UI содержит test ids: `admin-cms-page`, `admin-cms-material-table`, `admin-cms-material-form`, `admin-cms-editor-blocks`, `admin-cms-review-queue`, `admin-cms-preview`, `admin-cms-version-list`, `admin-cms-publication-schedule`, `admin-cms-seo-panel`, `admin-cms-audit-table`.
6. UI отображает loading, forbidden, validation, conflict, saved, draft, in review, legal review, approved, scheduled, published, archived, rejected, preview, empty и audit states без hardcoded user-facing строк.

## Критерии backend contract и хранения данных
1. DTO находятся в `api`, JPA entities и repository interfaces находятся в `domain`, Liquibase XML changelog находится в `db`, runtime controller/service/mapper/validator/audit classes находятся в role-specific subpackages внутри `impl`.
2. Swagger/OpenAPI endpoints admin CMS module появляются в runtime группе monolith module автоматически через Spring MVC controllers, без ручной регистрации списков endpoint-ов.
3. Для фичи создается отдельный Liquibase XML changelog в owning module `admin-cms`; изменения не добавляются в shared changelog другой фичи.
4. Persisted модель должна поддерживать cms material, cms material version, content block, attachment reference, SEO metadata, review decision, publication schedule, material relation и audit event.
5. API возвращает предсказуемые HTTP-коды: 200 для чтения, preview и update, 201 для создания material или version, 204 для archive без тела, 400 для validation errors, 401 для отсутствующей session, 403 для запрета доступа, 404 для разрешенного пользователя при отсутствии target, 409 для slug/version/status conflicts.
6. Module key для монолитного backend модуля: `admin-cms`; canonical URLs Swagger: `/v3/api-docs/admin-cms` и `/swagger-ui/admin-cms`.

## Критерии тестирования
1. Managed API test в `agents/tests/api/feature_027_админ_cms_контент_новости_faq_документы/FeatureApiTest.java` начинается с логина контент-администратора и проверяет create/update material, block validation, slug conflict, review approve/reject, schedule publish, document validation, preview, rollback, forbidden и audit search.
2. Managed UI test в `agents/tests/ui/feature_027_админ_cms_контент_новости_faq_документы/feature_ui_test.spec.ts` начинается с логина контент-администратора и проверяет material table, editor blocks, review queue, preview, versions, schedule, SEO panel, document attachment metadata, audit trail и validation states.
3. End-to-end managed tests агрегируют реальные managed feature tests публичного и admin-контентного потока, включая feature #001, #002, #003, #026 и #027, а не используют placeholder assertions по id или имени фичи.
4. Runtime-копии тестов синхронизированы из `agents/tests/` по `agents/tests/targets.yml`, содержат marker comment и совпадают с canonical source после учета marker-обертки.
5. Перед завершением workflow backend и frontend запускаются, feature API/UI проверки выполняются или фиксируется технический blocker без создания пустых файлов.