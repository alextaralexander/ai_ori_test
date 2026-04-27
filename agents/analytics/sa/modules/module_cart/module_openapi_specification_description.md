# Module cart. Полное описание OpenAPI

## Назначение API
OpenAPI module `cart` описывает runtime-контракт корзины Best Ori Gin для frontend web-shell. Контракт покрывает основную корзину, промо-предложения, supplementary order партнера, support view и server-side validation перед checkout. Swagger/OpenAPI в monolith должен генерироваться автоматически из Spring MVC controllers в пакете `com.bestorigin.monolith.cart.impl.controller`; canonical URLs модуля: `/v3/api-docs/cart` и `/swagger-ui/cart`.

## Группы endpoint

### Основная корзина
- `GET /api/cart/current` возвращает текущую основную корзину авторизованного пользователя.
- `POST /api/cart/items` добавляет товар в основную корзину по `productCode`, `quantity`, `source`, `campaignId` и `Idempotency-Key`.
- `PATCH /api/cart/items/{lineId}` меняет количество строки с учетом `expectedVersion`.
- `DELETE /api/cart/items/{lineId}` удаляет строку корзины.
- `POST /api/cart/validate` выполняет server-side validation перед checkout и возвращает `valid`, `blockingReasons`, `checkoutRoute`.

### Промо-предложения основной корзины
- `GET /api/cart/shopping-offers` возвращает применимые к основной корзине предложения текущей кампании.
- `POST /api/cart/shopping-offers/{offerId}/apply` применяет предложение к основной корзине, добавляет связанные строки или applied offer и возвращает пересчитанную корзину.

### Supplementary order
- `GET /api/cart/supplementary/current` возвращает отдельную корзину дозаказа партнера.
- `POST /api/cart/supplementary/items` добавляет товар в supplementary cart.
- `GET /api/cart/supplementary/shopping-offers` возвращает только офферы, применимые к supplementary order.

### Support view
- `GET /api/cart/support/users/{userId}/current` возвращает разрешенное представление корзины пользователя для сотрудника поддержки заказов. Endpoint требует support permission и фиксирует audit event просмотра.

## DTO и статусы
- `CartResponse` - основной DTO корзины: `cartId`, `cartType`, `campaignId`, `roleSegment`, `partnerContextId`, `status`, `currency`, `version`, `lines`, `appliedOffers`, `totals`, `validation`, `messageCode`.
- `CartLineResponse` - строка корзины: `lineId`, `productCode`, `name`, `imageUrl`, `quantity`, `price`, `availability`, `source`.
- `CartAvailability` - доступность строки: `AVAILABLE`, `LOW_STOCK`, `RESERVED`, `PARTIALLY_AVAILABLE`, `UNAVAILABLE`, `REMOVED_FROM_CAMPAIGN`, optional `reservedQuantity`, `maxAllowedQuantity`, `messageCode`.
- `ShoppingOfferResponse` - предложение: `offerId`, `titleKey`, `offerType`, `status`, `requiredCondition`, `remainingCondition`, `relatedProductCodes`, `benefitAmount`, `messageCode`.
- `CartValidationResponse` - результат validation: `valid`, `blockingReasons`, optional `checkoutRoute`.

## Валидации
- Все операции изменения требуют `Idempotency-Key`.
- `quantity` должен быть больше нуля и не превышать `maxAllowedQuantity`, полученный после проверки каталога, склада и campaign rules.
- `lineId`, `offerId`, `cartType`, `campaignId`, `productCode` проверяются как недоверенный ввод.
- Основная корзина и supplementary cart проверяются разными service paths; offer основной корзины не может примениться к supplementary order и наоборот.
- Support endpoint проверяет permission сотрудника и принадлежность запрашиваемого пользователя к разрешенному support scope.

## STR_MNEMO контракты
Backend не передает hardcoded user-facing text. Для предопределенных пользовательских сообщений используются mnemonic-коды:
- `STR_MNEMO_CART_RECALCULATED`
- `STR_MNEMO_CART_ITEM_ADDED`
- `STR_MNEMO_CART_ITEM_REMOVED`
- `STR_MNEMO_CART_ITEM_UNAVAILABLE`
- `STR_MNEMO_CART_QUANTITY_LIMIT_EXCEEDED`
- `STR_MNEMO_CART_OFFER_AVAILABLE`
- `STR_MNEMO_CART_OFFER_APPLIED`
- `STR_MNEMO_CART_OFFER_UNAVAILABLE`
- `STR_MNEMO_SUPPLEMENTARY_ORDER_FORBIDDEN`
- `STR_MNEMO_CART_CHECKOUT_VALIDATION_FAILED`

Все новые mnemonic-коды должны быть добавлены во все поддерживаемые frontend i18n dictionaries в рамках реализации.

## Ошибки
- `401` - пользователь не авторизован.
- `403` - действие запрещено ролью, типом корзины или отсутствием support permission.
- `404` - строка или корзина не найдена в разрешенном scope.
- `400` - business validation failure, недоступный товар, конфликт версии, недоступный offer или недопустимое количество.

Все ошибки возвращают `ErrorResponse` с `code`, `messageCode`, optional `details`; raw exception, stack trace и пользовательский текст backend не возвращаются.

## Интеграции
- Module `cart` читает товарные данные, цены, доступность и ограничения через локальный contract module `catalog`.
- Module `cart` не создает order и не рассчитывает compensation plan; checkout/order и bonus modules будут использовать валидированное состояние корзины в последующих фичах.
- Для WMS/остатков feature #9 допускает in-memory/mock adapter до реализации полноценной интеграции в административных фичах, но контракт availability должен быть совместим с будущим WMS.

## Версионная база
Новые runtime-технологии не вводятся. Контракт должен быть реализован в текущем Spring Boot monolith через Spring MVC controllers, DTO в `api`, сервисы в `impl/service`, JPA entities/repositories в `domain`, Liquibase XML changelog в `db`, с автоматической генерацией Swagger/OpenAPI для module `cart`.
