# Acceptance criteria. Feature 029. Админ: PIM, каталог, товары, категории, медиа

## Общие критерии
1. Административный PIM доступен только пользователям с RBAC scopes `ADMIN_PIM_VIEW`, `ADMIN_PIM_MANAGE`, `ADMIN_PIM_MEDIA_MANAGE`, `ADMIN_PIM_IMPORT_EXPORT`, `ADMIN_PIM_AUDIT_VIEW` из feature #26.
2. Все пользовательские строки frontend вынесены в `resources_ru.ts` и `resources_en.ts`; hardcoded user-facing text в React-компонентах отсутствует.
3. Backend не возвращает во frontend предопределенный пользовательский текст: для ошибок, предупреждений и статусов используются mnemonic-коды `STR_MNEMO_ADMIN_PIM_*`.
4. Все артефакты, исходный код, тесты, XML changelog и конфигурации сохраняются в UTF-8; русский текст читается без mojibake.
5. Backend package ownership соблюден: DTO и контракты в `api`, JPA/domain snapshots и repository interfaces в `domain`, Liquibase XML changelog в `db`, controller/service/config/exception в role-specific подпакетах `impl`.
6. Monolith OpenAPI для модуля доступен через `/v3/api-docs/admin-pim`, а Swagger UI через `/swagger-ui/admin-pim`; endpoints попадают в группу автоматически по package prefix.

## Категории
1. Администратор может создать, обновить, переместить, скрыть и архивировать категорию с `categoryId`, `parentId`, `slug`, `name`, `description`, `status`, `sortOrder`, `locale`, `audience`, `activeFrom`, `activeTo`.
2. Нельзя активировать категорию без уникального slug в рамках parent/category locale и без валидного publication window.
3. Нельзя зациклить дерево категорий переносом категории внутрь собственного descendants.
4. Список категорий поддерживает фильтры по статусу, parent, locale, audience и строке поиска.
5. Публичный каталог получает только категории со статусом `ACTIVE` и текущим publication window.

## Товары
1. Администратор может создать и обновить товар с `productId`, `sku`, `articleCode`, `brandCode`, `categoryIds`, `name`, `shortDescription`, `description`, `composition`, `usageInstructions`, `restrictions`, `status`, `locale`, `tags`.
2. SKU и articleCode уникальны среди неархивных товаров; попытка дубля возвращает `409` и mnemonic-код `STR_MNEMO_ADMIN_PIM_PRODUCT_DUPLICATE_SKU`.
3. Товар нельзя опубликовать без активной категории, названия, описания, состава, главного изображения и хотя бы одного storefront visibility channel.
4. Публикация товара создает новую версию карточки и audit event `PRODUCT_PUBLISHED`.
5. Изменение опубликованного товара сохраняет предыдущую версию для аудита и rollback.
6. Список товаров поддерживает фильтры по статусу, категории, бренду, тегу, наличию медиа, готовности к публикации и поиску по SKU/name/articleCode.

## Атрибуты и теги
1. PIM-менеджер может создавать attribute definitions с типом `TEXT`, `NUMBER`, `BOOLEAN`, `SELECT`, `MULTI_SELECT`, единицей измерения, filterable/searchable flags и allowed values.
2. Значения атрибутов товара валидируются по типу и allowed values; ошибки возвращаются как structured fields и mnemonic-коды.
3. Теги имеют code, displayName, usage scope и status; архивный тег не может быть добавлен к новому товару.
4. Публичный каталог получает только атрибуты и теги, разрешенные для витрины.

## Медиа и вложения
1. Медиа-менеджер может загрузить главное изображение, gallery image, video preview, PDF-инструкцию, сертификат или attachment с `mediaId`, `productId`, `usageType`, `fileName`, `mimeType`, `sizeBytes`, `checksum`, `altText`, `locale`, `version`, `status`.
2. Backend отклоняет недопустимые mime type, размер выше лимита, пустой checksum и конфликт checksum для другого активного файла.
3. У товара может быть только одно активное главное изображение на locale; новая версия переводит предыдущую в неактивное состояние.
4. PDF и сертификаты доступны в карточке товара только после статуса `APPROVED`.
5. Для каждого изменения медиа создается audit trail с actorUserId, actionCode, oldValue, newValue, correlationId и timestamp.

## Рекомендации и merchandising
1. Коммерческий администратор может связать товар с cross-sell, alternative, bundle и recommendation позициями с sortOrder и active window.
2. Merchandising block содержит code, titleKey, placement, productIds, categoryIds, status и schedule.
3. Нельзя активировать merchandising block с архивным товаром или товаром без публикации.
4. API возвращает структурированные блоки без hardcoded пользовательского текста backend.

## Импорт и экспорт
1. PIM-менеджер может создать import job для CSV/XLSX данных товаров, категорий, атрибутов и медиа-ссылок.
2. Import job валидирует строки до применения, возвращает row-level ошибки и не создает частично примененные данные при critical validation errors.
3. Повтор import job с тем же idempotency key возвращает существующий job и не создает дубли.
4. Export endpoint возвращает отфильтрованный набор каталога с product/category/attribute/media status и checksum файла выгрузки.
5. Все import/export операции записываются в audit trail.

## UI criteria
1. В админке есть PIM workspace с вкладками: товары, категории, бренды/атрибуты, медиа, рекомендации, импорт/экспорт, аудит.
2. Формы используют Ant Design-compatible controls: tables, filters, segmented status controls, tree view для категорий, upload controls, drawers/modal, switches, selects, date range picker и icon buttons.
3. UI показывает loading, empty, validation, forbidden, conflict, import progress, media rejected и retry states.
4. Длинные SKU, slug, attribute codes, file names и category paths не ломают layout на desktop и mobile.
5. Все статусы и ошибки отображаются через i18n dictionaries по mnemonic-кодам.

## Testing criteria
1. Managed BDD scenarios начинаются с логина пользователя с соответствующей ролью.
2. Managed API test покрывает login, создание категории, создание товара, отказ публикации без обязательного медиа, загрузку медиа metadata, публикацию товара, импорт с idempotency, export summary, audit и forbidden access.
3. Managed UI test покрывает вход в PIM workspace, создание категории, создание draft товара, проверку validation checklist, добавление медиа, запуск import/export и локализованные сообщения.
4. Managed end-to-end API/UI tests агрегируют реальные per-feature tests для green path и не содержат placeholder assertions.
5. Runtime-копии tests синхронизируются из `agents/tests/` по `agents/tests/targets.yml` и содержат marker comment о generated source.

## Версионная база
Критерии приемки рассчитаны на runtime baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML, React, TypeScript, Vite и Ant Design через зависимости frontend-проекта. Новые technology-sensitive отклонения от latest-stable baseline не допускаются без отдельного compatibility decision.