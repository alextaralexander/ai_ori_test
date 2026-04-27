# Feature sequence description. Feature 029. Админ: PIM, каталог, товары, категории, медиа

## Участники
- Frontend route `/admin/pim` в web-shell: показывает workspace, таблицы, дерево категорий, drawers/forms, media upload metadata, import/export и audit.
- Auth/RBAC context: возвращает session permissions feature #26 и ограничивает действия по scopes.
- `AdminPimController`: REST layer модуля `admin-pim` под `/api/admin-pim`.
- `AdminPimService`: бизнес-валидация категорий, товаров, медиа, публикации, import/export и audit.
- Repository layer module `admin-pim`: хранит PIM snapshots, idempotency records и audit events.
- S3/MinIO metadata: внешний storage для binary files; module сохраняет только reference metadata.
- Public catalog read model: потребитель published product/category render data для feature #004/#005/#006.

## Основной поток
1. Администратор открывает `/admin/pim`; frontend получает session permissions и вызывает `GET /api/admin-pim/workspace`.
2. Категорийный администратор создает категорию через `POST /categories`; сервис проверяет slug, parent tree и active window, сохраняет draft и audit.
3. При активации `POST /categories/{categoryId}/activate` сервис повторно проверяет отсутствие циклов и конфликтов, затем делает категорию `ACTIVE`.
4. PIM-менеджер создает товар через `POST /products`; сервис валидирует уникальность SKU/article, активную категорию, бренд и сохраняет draft.
5. Попытка публикации без approved main image возвращает `400` с `STR_MNEMO_ADMIN_PIM_PRODUCT_MAIN_IMAGE_REQUIRED` и field detail `mainImage`.
6. Медиа-менеджер добавляет metadata через `POST /products/{productId}/media`; сервис проверяет mime type, checksum, size и `fileReferenceId`, затем сохраняет media draft.
7. `POST /media/{mediaId}/approve` утверждает media, деактивирует предыдущее активное главное изображение для locale и пишет audit.
8. `POST /products/{productId}/publish` валидирует обязательные поля, категории и approved media, создает product version, меняет статус на `PUBLISHED` и публикует render data для public catalog.
9. Import/export операции используют `Idempotency-Key`, row-level validation и audit trail. Повторный import с тем же key возвращает существующий job.

## Ошибки
- `STR_MNEMO_ADMIN_PIM_FORBIDDEN`: нет scope.
- `STR_MNEMO_ADMIN_PIM_CATEGORY_CYCLE_FORBIDDEN`: перенос создает цикл.
- `STR_MNEMO_ADMIN_PIM_CATEGORY_SLUG_CONFLICT`: конфликт slug.
- `STR_MNEMO_ADMIN_PIM_PRODUCT_DUPLICATE_SKU`: дубль SKU или articleCode.
- `STR_MNEMO_ADMIN_PIM_PRODUCT_MAIN_IMAGE_REQUIRED`: публикация без approved main image.
- `STR_MNEMO_ADMIN_PIM_MEDIA_REJECTED`: media metadata не прошла validation.
- `STR_MNEMO_ADMIN_PIM_IMPORT_VALIDATION_FAILED`: import имеет critical row errors.
- `STR_MNEMO_ADMIN_PIM_IDEMPOTENCY_CONFLICT`: key повторно использован с другим payload.

## Нефункциональные требования
Все frontend user-facing тексты берутся из i18n dictionaries. Backend возвращает только structured data и mnemonic-коды. Audit не содержит secrets, binary content или лишние персональные данные. Все файлы feature workflow и runtime artifacts сохраняются в UTF-8.

## Версионная база
Дата baseline: 27.04.2026. Стек: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML, PostgreSQL, React/TypeScript/Vite/Ant Design.