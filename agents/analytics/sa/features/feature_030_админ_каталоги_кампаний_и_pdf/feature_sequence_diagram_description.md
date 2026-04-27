# Feature 030. Sequence description

## Основной поток
1. Каталожный менеджер в `/admin/catalogs` создает кампанию `CAM-2026-05`. Frontend отправляет `POST /api/admin-catalog/campaigns`, backend проверяет RBAC scope `ADMIN_CATALOG_MANAGE`, уникальность `campaignCode`, 21-дневное окно и сохраняет кампанию с audit event `CAMPAIGN_CREATED`.
2. Менеджер создает выпуск `ISSUE-2026-05` через `POST /campaigns/{campaignId}/issues`. Backend фиксирует publication/archive/freeze/rollover окна и возвращает `STR_MNEMO_ADMIN_CATALOG_ISSUE_SCHEDULED`.
3. Контент-администратор загружает PDF и изображения страниц в S3/MinIO, затем передает metadata в admin-catalog. Backend сохраняет fileName, mimeType, sizeBytes, checksum, storageKey, version и status. Утверждение PDF создает audit event `PDF_APPROVED`.
4. Маркетинг-менеджер создает product hotspots на page image. Admin-catalog валидирует координаты `0..1`, затем синхронно проверяет SKU/productId против published PIM-товара feature #29. Невалидная ссылка возвращает `STR_MNEMO_ADMIN_CATALOG_HOTSPOT_INVALID` или validation report.
5. Перед публикацией frontend вызывает `/validate-links`. Backend проверяет approved PDF, готовые page images, активные hotspots и связи с PIM.
6. Во время rollover backend проверяет idempotency key, freeze/readiness rules, архивирует текущий выпуск, публикует следующий и передает projection в публичный цифровой каталог feature #006.

## Ошибки и состояния
- Отсутствие прав возвращает `403` и `STR_MNEMO_ADMIN_CATALOG_FORBIDDEN`.
- Дубль кода кампании возвращает `409` и `STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_CODE_CONFLICT`.
- Невалидный PDF/material metadata возвращает `400` и `STR_MNEMO_ADMIN_CATALOG_MATERIAL_REJECTED`.
- Попытка изменения в freeze window возвращает `409` и `STR_MNEMO_ADMIN_CATALOG_FREEZE_WINDOW_ACTIVE`.
- Повтор rollover с тем же `Idempotency-Key` возвращает прежний `RolloverResponse` без повторного изменения выпуска.

## Интеграции
- `admin-pim` поставляет published product contract для проверки hotspots.
- S3/MinIO хранит binary PDF, cover и page images; admin-catalog хранит metadata и checksum.
- Public digital catalog получает текущий выпуск, PDF, pages, hotspots и архивную проекцию.
- Frontend локализует все predefined состояния через `resources_ru.ts` и `resources_en.ts`.

## Версионная база
Sequence рассчитан на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML, PostgreSQL, S3/MinIO, React/TypeScript/Vite/Ant Design.
