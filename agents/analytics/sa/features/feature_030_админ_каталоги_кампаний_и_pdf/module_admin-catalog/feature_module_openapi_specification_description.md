# Feature 030. OpenAPI description for module admin-catalog

## API group
Модуль `admin-catalog` публикуется в monolith Swagger group `admin-catalog`: OpenAPI JSON `/v3/api-docs/admin-catalog`, Swagger UI `/swagger-ui/admin-catalog`. Controllers находятся под package prefix `com.bestorigin.monolith.admincatalog.impl`.

## Endpoints
- `GET /api/admin-catalog/workspace` - summary workspace: количество кампаний, выпусков, approved PDF, активных hotspots, freeze warnings и mnemonic `STR_MNEMO_ADMIN_CATALOG_WORKSPACE_LOADED`.
- `GET /api/admin-catalog/campaigns` - поиск кампаний по статусу, локали, аудитории и строке.
- `POST /api/admin-catalog/campaigns` - создание кампании. Конфликт `campaignCode` возвращает `409` и `STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_CODE_CONFLICT`.
- `POST /api/admin-catalog/campaigns/{campaignId}/issues` - создание следующего выпуска с publication/archive/freeze/rollover окнами.
- `POST /api/admin-catalog/issues/{issueId}/materials` - добавление PDF, cover или page material metadata. Валидация mime type, checksum и размера возвращает `STR_MNEMO_ADMIN_CATALOG_MATERIAL_REJECTED`.
- `POST /api/admin-catalog/materials/{materialId}/approve` - утверждение материала и audit event `PDF_APPROVED` или `MATERIAL_APPROVED`.
- `POST /api/admin-catalog/issues/{issueId}/pages` - добавление page image metadata с уникальным `pageNumber`.
- `POST /api/admin-catalog/issues/{issueId}/hotspots` - создание product hotspot с координатами `0..1`. Ошибка координат возвращает `STR_MNEMO_ADMIN_CATALOG_HOTSPOT_INVALID`.
- `POST /api/admin-catalog/issues/{issueId}/validate-links` - проверка PDF/pages/hotspots и связей с published PIM-товарами.
- `POST /api/admin-catalog/issues/{issueId}/rollover` - публикация следующего выпуска и архивирование текущего с `Idempotency-Key`.
- `GET /api/admin-catalog/archive` - архив прошлых выпусков с датами действия и ссылками на PDF.
- `GET /api/admin-catalog/audit` - поиск audit events по entity/action/correlation/date.

## DTOs
- `CampaignCreateRequest`, `CampaignResponse`, `CampaignListResponse`.
- `IssueCreateRequest`, `IssueResponse`.
- `MaterialCreateRequest`, `MaterialResponse`.
- `PageCreateRequest`, `PageResponse`.
- `HotspotCreateRequest`, `HotspotResponse`.
- `LinkValidationResponse`, `RolloverResponse`, `ArchiveResponse`, `AuditResponse`.
- `AdminCatalogErrorResponse` всегда содержит `messageCode`, `correlationId`, `details`.

## Validation and message contract
Backend не отправляет hardcoded пользовательский текст во frontend. Для предопределенных состояний используются mnemonic-коды:
- `STR_MNEMO_ADMIN_CATALOG_FORBIDDEN`
- `STR_MNEMO_ADMIN_CATALOG_WORKSPACE_LOADED`
- `STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_SAVED`
- `STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_CODE_CONFLICT`
- `STR_MNEMO_ADMIN_CATALOG_ISSUE_SCHEDULED`
- `STR_MNEMO_ADMIN_CATALOG_MATERIAL_SAVED`
- `STR_MNEMO_ADMIN_CATALOG_MATERIAL_REJECTED`
- `STR_MNEMO_ADMIN_CATALOG_PDF_APPROVED`
- `STR_MNEMO_ADMIN_CATALOG_PAGE_SAVED`
- `STR_MNEMO_ADMIN_CATALOG_HOTSPOT_SAVED`
- `STR_MNEMO_ADMIN_CATALOG_HOTSPOT_INVALID`
- `STR_MNEMO_ADMIN_CATALOG_LINKS_VALID`
- `STR_MNEMO_ADMIN_CATALOG_FREEZE_WINDOW_ACTIVE`
- `STR_MNEMO_ADMIN_CATALOG_ROLLOVER_COMPLETED`

## RBAC
Endpoints require admin roles/scopes from feature #26:
- read workspace/archive/audit: `ADMIN_CATALOG_VIEW` или `ADMIN_CATALOG_AUDIT_VIEW`;
- create/update campaigns/issues/hotspots: `ADMIN_CATALOG_MANAGE`;
- upload/approve materials/pages: `ADMIN_CATALOG_MEDIA_MANAGE`;
- rollover: `ADMIN_CATALOG_ROLLOVER`.

## Версионная база
API рассчитан на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, springdoc-openapi, Maven, React/TypeScript/Vite/Ant Design. Новые frontend сообщения должны быть добавлены в `resources_ru.ts` и `resources_en.ts`.
