# OpenAPI description. Module admin-pricing

## Назначение API
API module `admin-pricing` предоставляет административные endpoints для управления price lists, базовыми ценами, promo prices, segment rules, promotions, shopping offers, gift rules, import/export jobs, публикацией и audit trail. Runtime OpenAPI генерируется автоматически через springdoc-openapi:
- JSON: `/v3/api-docs/admin-pricing`
- Swagger UI: `/swagger-ui/admin-pricing`

`module-key` должен быть `admin-pricing`, а package prefix — `com.bestorigin.monolith.adminpricing`.

## Security и RBAC
Все endpoints требуют bearer JWT и проверку scopes feature #26:
- `ADMIN_PRICING_VIEW` — чтение price lists, promotions и readonly details.
- `ADMIN_PRICING_MANAGE` — создание и изменение price lists, prices, promo prices, segment rules.
- `ADMIN_PROMOTION_MANAGE` — создание promotions, shopping offers, gift rules и threshold benefits.
- `ADMIN_PRICING_PUBLISH` — публикация, preflight validation и emergency pause.
- `ADMIN_PRICING_IMPORT_EXPORT` — import/export jobs.
- `ADMIN_PRICING_AUDIT_VIEW` — audit search.

Forbidden responses возвращают `403` и mnemonic-код `STR_MNEMO_ADMIN_PRICING_FORBIDDEN`.

## Endpoints

### Price lists
- `GET /api/admin/pricing/price-lists` ищет прайс-листы по `campaignId`, `status`, поисковой строке и pagination.
- `POST /api/admin/pricing/price-lists` создает draft price list.
- `GET /api/admin/pricing/price-lists/{priceListId}` возвращает детали price list.
- `PATCH /api/admin/pricing/price-lists/{priceListId}` обновляет metadata с optimistic locking.

Ключевые ошибки:
- `STR_MNEMO_ADMIN_PRICING_PRICE_LIST_CODE_CONFLICT` для дубля `priceListCode`.
- `STR_MNEMO_ADMIN_PRICING_ACTIVE_WINDOW_INVALID` для некорректного периода.
- `STR_MNEMO_ADMIN_PRICING_VERSION_CONFLICT` для optimistic locking conflict.

### Prices и promo prices
- `POST /api/admin/pricing/price-lists/{priceListId}/prices` добавляет базовую цену SKU.
- `POST /api/admin/pricing/price-lists/{priceListId}/promo-prices` добавляет promo price.
- `POST /api/admin/pricing/segment-rules` создает segment rule.

Backend валидирует published PIM SKU, валюту price list, пересечение активных периодов и min margin threshold. Предопределенные ошибки возвращаются mnemonic-кодами:
- `STR_MNEMO_ADMIN_PRICING_PRICE_PERIOD_OVERLAP`
- `STR_MNEMO_ADMIN_PRICING_PIM_PRODUCT_NOT_PUBLISHED`
- `STR_MNEMO_ADMIN_PRICING_CURRENCY_MISMATCH`
- `STR_MNEMO_ADMIN_PRICING_MARGIN_APPROVAL_REQUIRED`
- `STR_MNEMO_ADMIN_PRICING_SEGMENT_RULE_PRIORITY_CONFLICT`

### Promotions, offers и gifts
- `GET /api/admin/pricing/promotions` ищет promotions.
- `POST /api/admin/pricing/promotions` создает promotion.
- `POST /api/admin/pricing/promotions/{promotionId}/offers` создает shopping offer.
- `POST /api/admin/pricing/promotions/{promotionId}/gift-rules` создает gift rule.
- `POST /api/admin/pricing/offers/{offerId}/pause` выполняет emergency pause.

Offer types: `CROSS_SELL`, `UPSELL`, `BUNDLE`, `GIFT`, `WELCOME_BENEFIT`, `RETENTION_BENEFIT`, `THRESHOLD_BENEFIT`.

Backend не передает hardcoded UI-текст для заголовков или описаний offers. `titleKey` и `descriptionKey` являются i18n keys, которые frontend resolve через `resources_ru.ts` и `resources_en.ts`.

Ключевые ошибки:
- `STR_MNEMO_ADMIN_PRICING_PROMOTION_CODE_CONFLICT`
- `STR_MNEMO_ADMIN_PRICING_OFFER_CODE_CONFLICT`
- `STR_MNEMO_ADMIN_PRICING_GIFT_SKU_NOT_AVAILABLE`
- `STR_MNEMO_ADMIN_PRICING_OFFER_COMPATIBILITY_CONFLICT`

### Publish
- `POST /api/admin/pricing/publish` выполняет preflight validation и публикует price list/promotions с `Idempotency-Key`.

Preflight validation проверяет:
- published PIM product/gift SKU;
- привязку к campaign window feature #030;
- пересечения ценовых периодов;
- min margin threshold;
- совместимость stackable/mutually exclusive offers;
- наличие базовых цен для gift SKU и bundle items.

Повтор publish с тем же `Idempotency-Key` возвращает существующий результат без повторной публикации и дублей audit events.

### Import/export
- `POST /api/admin/pricing/imports` создает import job с `Idempotency-Key`.

Dry-run import валидирует файл и возвращает row-level summary без изменения бизнес-данных. Apply import должен выполняться атомарно по валидному batch. Повтор с тем же idempotency key возвращает существующий import job.

### Audit
- `GET /api/admin/pricing/audit-events` ищет audit events по `entityType`, `entityId`, `correlationId` и pagination.

Audit response не содержит секреты, токены, stack traces, полные персональные данные или пользовательский UI-текст. Для расследований достаточно `actorUserId`, `actionCode`, `entityType`, `entityId`, `reasonCode`, `correlationId` и `occurredAt`.

## DTO и валидации
DTO лежат в `api` package. Валидации должны возвращать field-level errors в `ErrorResponse.fieldErrors` и стабильный `code`. Для frontend user-facing сообщений backend отправляет только `STR_MNEMO_ADMIN_PRICING_*`; frontend локализует эти коды из i18n dictionaries.

## Интеграционные контракты
`admin-pricing` читает ссылки на:
- PIM product/SKU из feature #29;
- campaignId и active window из feature #30;
- RBAC scopes из feature #26.

Опубликованные pricing/promotions потребляются:
- публичным каталогом feature #004/#005;
- цифровыми каталогами feature #006/#030;
- корзиной и shopping offers feature #009;
- checkout feature #010;
- партнерскими офлайн-заказами feature #017.

## Observability
Все state-changing операции должны иметь `correlationId`, `actorUserId`, `idempotencyKey` там, где операция повторяемая, и immutable audit event. Ошибки публикации, импорта и conflict states возвращают correlationId для поддержки.

## Версионная база
Спецификация рассчитана на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Spring MVC, Bean Validation, Spring Security, Springdoc OpenAPI, Liquibase XML и PostgreSQL. Новый runtime stack не вводится.

## Scope
Этот canonical module artifact описывает полный OpenAPI baseline module_admin-pricing после feature #31. Runtime Swagger должен оставаться совместимым с /v3/api-docs/admin-pricing и /swagger-ui/admin-pricing.
