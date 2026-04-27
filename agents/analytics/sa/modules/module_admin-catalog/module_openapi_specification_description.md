# Module admin-catalog. Full OpenAPI description

## Swagger group
Monolith module key: `admin-catalog`. Canonical URLs: `/v3/api-docs/admin-catalog` and `/swagger-ui/admin-catalog`. Controllers must stay under `com.bestorigin.monolith.admincatalog.impl.controller`.

## Functional surface
The module exposes endpoints for workspace summary, campaign search/create, issue create, material metadata, material approval, page image metadata, hotspot creation, link validation, rollover, archive and audit.

## Message contract
Backend responses use `messageCode` only for predefined user-facing states. Supported codes include `STR_MNEMO_ADMIN_CATALOG_FORBIDDEN`, `STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_SAVED`, `STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_CODE_CONFLICT`, `STR_MNEMO_ADMIN_CATALOG_ISSUE_SCHEDULED`, `STR_MNEMO_ADMIN_CATALOG_MATERIAL_SAVED`, `STR_MNEMO_ADMIN_CATALOG_PDF_APPROVED`, `STR_MNEMO_ADMIN_CATALOG_HOTSPOT_INVALID`, `STR_MNEMO_ADMIN_CATALOG_LINKS_VALID`, `STR_MNEMO_ADMIN_CATALOG_FREEZE_WINDOW_ACTIVE`, `STR_MNEMO_ADMIN_CATALOG_ROLLOVER_COMPLETED`.

## Security
Endpoints require `ADMIN_CATALOG_VIEW`, `ADMIN_CATALOG_MANAGE`, `ADMIN_CATALOG_MEDIA_MANAGE`, `ADMIN_CATALOG_ROLLOVER` or `ADMIN_CATALOG_AUDIT_VIEW` according to action type. Frontend may hide unavailable actions, but backend remains authoritative.

## Version baseline
Baseline: 27.04.2026, Java 25, Spring Boot 4.0.6, springdoc-openapi, Maven, React/TypeScript/Vite/Ant Design.
