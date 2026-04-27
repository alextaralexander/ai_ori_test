# Acceptance criteria. Feature 030. Админ: каталоги кампаний и PDF

## Общие критерии
1. Административный модуль каталогов доступен только пользователям с RBAC scopes `ADMIN_CATALOG_VIEW`, `ADMIN_CATALOG_MANAGE`, `ADMIN_CATALOG_MEDIA_MANAGE`, `ADMIN_CATALOG_ROLLOVER`, `ADMIN_CATALOG_AUDIT_VIEW`.
2. Все пользовательские строки frontend вынесены в `resources_ru.ts` и `resources_en.ts`; hardcoded user-facing text в React-компонентах не допускается.
3. Backend не возвращает во frontend предопределенный пользовательский текст: ошибки, предупреждения и статусы передаются mnemonic-кодами `STR_MNEMO_ADMIN_CATALOG_*`.
4. Все артефакты, исходный код, тесты, XML changelog и конфигурации сохраняются в UTF-8; русский текст читается без mojibake.
5. Backend package ownership соблюден: DTO и контракты в `api`, JPA/domain snapshots и repository interfaces в `domain`, Liquibase XML changelog в `db`, controller/service/config/exception в role-specific подпакетах `impl`.
6. Monolith OpenAPI для модуля доступен через `/v3/api-docs/admin-catalog`, а Swagger UI через `/swagger-ui/admin-catalog`; endpoints попадают в группу автоматически по package prefix.

## Кампании и выпуски
1. Администратор может создать кампанию с `campaignId`, `campaignCode`, `name`, `locale`, `audience`, `startsAt`, `endsAt`, `status`, `createdBy`, `updatedAt`.
2. Длительность кампании по умолчанию равна 21 календарному дню; отклонение допускается только при явном override reason и audit event.
3. `campaignCode` уникален среди неархивных кампаний; дубль возвращает `409` и `STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_CODE_CONFLICT`.
4. Выпуск каталога содержит `issueId`, `campaignId`, `issueCode`, `status`, `currentFlag`, `nextFlag`, `archiveFlag`, `publicationAt`, `archiveAt`, `freezeStartsAt`, `rolloverWindowStartsAt`, `rolloverWindowEndsAt`.
5. Нельзя иметь больше одного текущего опубликованного выпуска для одной locale/audience пары.

## PDF, страницы и промо-вкладки
1. Контент-администратор может прикрепить PDF с `materialId`, `issueId`, `fileName`, `mimeType`, `sizeBytes`, `checksum`, `storageKey`, `version`, `status`.
2. Backend отклоняет PDF без checksum, с неподдерживаемым mime type, размером выше лимита или конфликтом checksum для другого активного материала.
3. Для выпуска можно загрузить cover image и page images с `pageNumber`, `width`, `height`, `imageUrl`, `status`; номера страниц уникальны в рамках выпуска.
4. Промо-вкладка содержит `tabId`, `issueId`, `titleKey`, `placement`, `sortOrder`, `activeFrom`, `activeTo`, `linkedOfferCode`, `status`.
5. Опубликованный выпуск не может ссылаться на PDF/page image со статусом ниже `APPROVED`.

## Hotspots и ссылки
1. Marketing manager может создать hotspot с `hotspotId`, `issueId`, `pageNumber`, `x`, `y`, `width`, `height`, `productId`, `sku`, `promoCode`, `sortOrder`, `status`.
2. Координаты hotspot валидируются как доли страницы от `0` до `1`; некорректные координаты возвращают `400` и `STR_MNEMO_ADMIN_CATALOG_HOTSPOT_INVALID`.
3. Hotspot нельзя активировать, если связанный товар отсутствует, не опубликован в PIM feature #29 или исключен из аудитории выпуска.
4. Проверка ссылок возвращает отчет с количеством валидных, предупрежденных и заблокированных hotspots.

## Freeze window и rollover
1. При наступлении freeze window выпуск переводится в режим ограниченных изменений: допускаются только комментарии, audit view и emergency rollback.
2. Попытка изменить PDF, страницы, hotspots или статус публикации внутри freeze window без emergency override возвращает `409` и `STR_MNEMO_ADMIN_CATALOG_FREEZE_WINDOW_ACTIVE`.
3. Rollover архивирует текущий выпуск, публикует следующий и обновляет current/next/archive flags атомарно в рамках бизнес-операции.
4. Повторный rollover с тем же idempotency key возвращает существующий результат и не создает повторные audit events.
5. После успешного rollover публичный цифровой каталог feature #006 получает новый текущий выпуск, а прошлый выпуск попадает в архив.

## Audit и наблюдаемость
1. Все изменения кампаний, выпусков, PDF, страниц, hotspots, freeze и rollover пишутся в audit trail с `actorUserId`, `actionCode`, `entityType`, `entityId`, `oldValue`, `newValue`, `correlationId`, `occurredAt`.
2. Audit search поддерживает фильтры `campaignId`, `issueId`, `entityType`, `actionCode`, `actorUserId`, `dateFrom`, `dateTo`, `correlationId`.
3. Ошибки публикации и rollover возвращают stable mnemonic-коды и correlationId.

## UI criteria
1. В админке есть workspace "Каталоги кампаний" с вкладками: кампании, выпуски, PDF и страницы, hotspots, freeze/rollover, аудит.
2. Формы используют Ant Design-compatible controls: tables, filters, segmented status controls, upload controls, date range picker, drawers/modal, switches, selects, page preview area и icon buttons.
3. UI показывает loading, empty, validation, forbidden, conflict, freeze active, rollover progress, broken hotspot links и retry states.
4. Длинные campaignCode, issueCode, fileName, storageKey, product SKU и correlationId не ломают layout на desktop и mobile.
5. Все статусы и ошибки отображаются через i18n dictionaries по mnemonic-кодам.

## Testing criteria
1. Managed BDD scenarios начинаются с логина пользователя с соответствующей ролью.
2. Managed API test покрывает login, создание кампании, создание выпуска, загрузку PDF metadata, создание page image, создание hotspot, отказ публикации внутри freeze window, успешный rollover, archive view, audit и forbidden access.
3. Managed UI test покрывает вход в workspace, создание кампании, подготовку выпуска, добавление PDF/page/hotspot, запуск rollover, проверку freeze warning и локализованные сообщения.
4. Managed end-to-end API/UI tests агрегируют реальные per-feature tests для green path и не содержат placeholder assertions.
5. Runtime-копии tests синхронизируются из `agents/tests/` по `agents/tests/targets.yml` и содержат marker comment о generated source.

## Версионная база
Критерии приемки рассчитаны на runtime baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML, React, TypeScript, Vite и Ant Design через зависимости frontend-проекта. Новые technology-sensitive отклонения от latest-stable baseline не допускаются без отдельного compatibility decision.
