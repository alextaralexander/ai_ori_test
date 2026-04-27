# Module OpenAPI description. module admin-pim

## API surface
Module key: `admin-pim`. Runtime base path: `/api/admin-pim`. Monolith OpenAPI group: `/v3/api-docs/admin-pim`, Swagger UI: `/swagger-ui/admin-pim`. Controllers must stay under `com.bestorigin.monolith.adminpim.impl.controller` so springdoc group picks them up automatically.

## Endpoints
- `GET /workspace`: summary counts for PIM admin workspace and messageCode `STR_MNEMO_ADMIN_PIM_WORKSPACE_LOADED`.
- `GET /categories`, `POST /categories`, `PUT /categories/{categoryId}`, `POST /categories/{categoryId}/activate`: category tree CRUD, activation and validation for slug uniqueness, cycle prevention and publication window.
- `GET /products`, `POST /products`, `GET /products/{productId}`, `PUT /products/{productId}`, `POST /products/{productId}/publish`: product search, draft editing, validation checklist and publication.
- `POST /products/{productId}/media`, `POST /media/{mediaId}/approve`: media metadata lifecycle; binary files remain in S3/MinIO and API stores `fileReferenceId`.
- `POST /attributes`: create attribute definitions with value type, filterable/searchable flags and allowed values.
- `POST /recommendations`: create cross-sell, alternative, bundle and recommendation relations.
- `POST /imports`: start idempotent import job using `Idempotency-Key` header.
- `POST /exports`: create export job with counters and checksum.
- `GET /audit`: search immutable PIM audit events by entity type/id, action code and correlation id.

## DTO and validation contract
Requests use structured fields and no backend-generated user-facing labels. Validation errors return `AdminPimErrorResponse.messageCode` and optional machine-readable `details`. Required validations include duplicate SKU/article, category cycle, missing main image before publication, invalid mime type/checksum, import row validation and idempotency conflicts.

## Mnemonic codes
Backend-to-frontend predefined messages use only `STR_MNEMO_ADMIN_PIM_*` codes:
- `STR_MNEMO_ADMIN_PIM_FORBIDDEN`
- `STR_MNEMO_ADMIN_PIM_WORKSPACE_LOADED`
- `STR_MNEMO_ADMIN_PIM_CATEGORY_SAVED`
- `STR_MNEMO_ADMIN_PIM_CATEGORY_CYCLE_FORBIDDEN`
- `STR_MNEMO_ADMIN_PIM_CATEGORY_SLUG_CONFLICT`
- `STR_MNEMO_ADMIN_PIM_PRODUCT_SAVED`
- `STR_MNEMO_ADMIN_PIM_PRODUCT_DUPLICATE_SKU`
- `STR_MNEMO_ADMIN_PIM_PRODUCT_MAIN_IMAGE_REQUIRED`
- `STR_MNEMO_ADMIN_PIM_MEDIA_SAVED`
- `STR_MNEMO_ADMIN_PIM_MEDIA_REJECTED`
- `STR_MNEMO_ADMIN_PIM_ATTRIBUTE_SAVED`
- `STR_MNEMO_ADMIN_PIM_RECOMMENDATION_SAVED`
- `STR_MNEMO_ADMIN_PIM_IMPORT_APPLIED`
- `STR_MNEMO_ADMIN_PIM_IMPORT_VALIDATION_FAILED`
- `STR_MNEMO_ADMIN_PIM_EXPORT_CREATED`
- `STR_MNEMO_ADMIN_PIM_IDEMPOTENCY_CONFLICT`

## Security
All write endpoints require `ADMIN_PIM_MANAGE`; media endpoints require `ADMIN_PIM_MEDIA_MANAGE`; import/export endpoints require `ADMIN_PIM_IMPORT_EXPORT`; audit requires `ADMIN_PIM_AUDIT_VIEW`; read workspace/search requires `ADMIN_PIM_VIEW`. Frontend may hide actions from session permissions, but backend remains authoritative.

## Audit
State-changing endpoints create audit events: `CATEGORY_CREATED`, `CATEGORY_ACTIVATED`, `PRODUCT_CREATED`, `PRODUCT_UPDATED`, `PRODUCT_PUBLISHED`, `PRODUCT_MEDIA_CREATED`, `PRODUCT_MEDIA_APPROVED`, `ATTRIBUTE_CREATED`, `RECOMMENDATION_CREATED`, `IMPORT_JOB_APPLIED`, `CATALOG_EXPORTED`. Events include actorUserId, entityType, entityId, old/new JSON, correlationId and occurredAt.

## Version baseline
Дата baseline: 27.04.2026. Стек: Java 25, Spring Boot 4.0.6, Maven, springdoc-openapi, Liquibase XML, PostgreSQL, React/TypeScript/Vite/Ant Design. Отклонения не требуются.