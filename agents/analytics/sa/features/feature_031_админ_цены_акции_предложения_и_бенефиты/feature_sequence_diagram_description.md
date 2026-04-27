# Sequence description. Feature 031. Админ: цены, акции, предложения и бенефиты

## Участники
- `Admin frontend /admin/pricing` — административный UI для pricing/promotions.
- `admin-pricing` — новый монолитный backend module с module-key `admin-pricing`.
- `admin-pim` — источник published SKU/product data для валидации price, gift и offer items.
- `admin-catalog` — источник campaignId, 21-дневного active window и freeze/rollover контекста.
- `catalog/cart/checkout pricing consumers` — публичный каталог, цифровой каталог, корзина, checkout и партнерские офлайн-заказы.
- `Audit trail` — immutable журнал изменений.

## Основной поток
Pricing manager открывает `/admin/pricing`, получает список price lists и создает новый price list для кампании. `admin-pricing` валидирует campaignId и active window через `admin-catalog`, затем сохраняет `admin_pricing_price_list` и audit event `PRICE_LIST_CREATED`.

При добавлении базовой цены `admin-pricing` валидирует SKU через `admin-pim`, проверяет currency context и отсутствие пересечения active interval для той же комбинации price list, SKU и segment context. Если период пересекается, backend возвращает `409` и mnemonic-код `STR_MNEMO_ADMIN_PRICING_PRICE_PERIOD_OVERLAP`; frontend локализует сообщение из i18n dictionaries. Если цена валидна, сохраняется `admin_pricing_price` и audit event `PRICE_CREATED`.

Promo price и segment rule создаются отдельными commands. Для promo price проверяются min margin threshold, валюта и active window. Для segment rule проверяются role/segment/partner level и уникальность priority внутри пересекающегося контекста.

Promotions manager создает promotion, shopping offer, bundle items и gift rule. Backend не хранит пользовательские заголовки и описания, а принимает только `titleKey` и `descriptionKey`. SKU в offer items и gift SKU валидируются через published PIM state.

Бизнес-администратор запускает публикацию через `POST /publish` с `Idempotency-Key`. `admin-pricing` загружает price list, prices, promotions, offers и gifts, затем выполняет preflight validation:
- published PIM products and gift SKUs;
- campaign window из feature #30;
- absence of price period overlaps;
- min margin threshold;
- offer compatibility: priority, stackability, mutual exclusion;
- наличие базовых цен для bundle/gift SKU.

Если preflight validation проходит, price list и promotions активируются атомарно, создается `PRICING_PUBLISHED`, а consumer modules начинают получать опубликованные условия по campaign, role, segment и channel. Если validation не проходит, business data остается в прежнем состоянии, audit получает `PRICING_PUBLISH_REJECTED`, а frontend показывает локализуемые mnemonic reasons.

## Emergency pause
Если обнаружена ошибочная скидка или некорректный offer, бизнес-администратор вызывает `POST /offers/{offerId}/pause` с `reasonCode`. `admin-pricing` переводит offer в `PAUSED`, сохраняет `SHOPPING_OFFER_PAUSED`, а корзина и checkout исключают offer при следующем recalculation. История offer и причины отключения остаются доступны в audit.

## Идемпотентность
Публикация и import jobs требуют `Idempotency-Key`. Повтор запроса с тем же ключом возвращает существующий результат и не создает дубли price lists, promotions или audit events.

## Контракт сообщений
Backend не передает hardcoded user-facing text. Все предопределенные сообщения возвращаются как `STR_MNEMO_ADMIN_PRICING_*`, например:
- `STR_MNEMO_ADMIN_PRICING_PRICE_LIST_CODE_CONFLICT`
- `STR_MNEMO_ADMIN_PRICING_PRICE_PERIOD_OVERLAP`
- `STR_MNEMO_ADMIN_PRICING_PIM_PRODUCT_NOT_PUBLISHED`
- `STR_MNEMO_ADMIN_PRICING_GIFT_SKU_NOT_AVAILABLE`
- `STR_MNEMO_ADMIN_PRICING_PREFLIGHT_FAILED`
- `STR_MNEMO_ADMIN_PRICING_FORBIDDEN`

Frontend обязан resolve эти коды через `resources_ru.ts` и `resources_en.ts`.

## Package ownership
Runtime code должен размещаться так:
- `api` — DTO и REST contracts.
- `domain` — JPA entities и repository interfaces.
- `db` — Liquibase XML changelog.
- `impl/controller`, `impl/service`, `impl/validator`, `impl/mapper`, `impl/exception`, `impl/config`, `impl/security` — runtime orchestration.

## Версионная база
Описание рассчитано на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Spring MVC, Spring Security, Hibernate/JPA, MapStruct, Lombok, Liquibase XML, PostgreSQL и Springdoc OpenAPI.
