# Feature 005. Module catalog. Описание OpenAPI-спецификации

## Назначение API
API feature #5 раскрывает детальную карточку товара и добавление товара в корзину из карточки. Контракт принадлежит monolith module `catalog`, должен быть отражен в runtime Swagger группы `/v3/api-docs/catalog` и Swagger UI `/swagger-ui/catalog`.

## Endpoint `GET /api/catalog/products/{productCode}`
Возвращает детальную карточку опубликованного товара.

### Входные параметры
- `productCode` path, обязательный, строка до 64 символов. Бизнес-код товара.
- `audience` query, необязательный: `GUEST`, `CUSTOMER`, `PARTNER`. Влияет на видимость цен, промо-меток и ограничений.
- `campaignCode` query, необязательный. Позволяет открыть карточку в контексте конкретной кампании; по умолчанию используется текущая кампания.

### Успешный ответ
`CatalogProductCardResponse` содержит:
- базовые поля: `productCode`, `name`, `categoryCode`, `categoryName`, `brand`, `volumeLabel`, `campaignCode`;
- `badges` - промо-метки как данные продукта;
- `price` - `basePrice`, `promoPrice`, `currency`;
- `availability` - статус, опциональный остаток и `messageCode` для локализации специальных состояний;
- `orderLimits` - минимальное и максимальное количество;
- `media` - галерея изображений с `url`, `altText`, `primary`, `sortOrder`;
- `information` - краткое и полное описание, способ применения, состав и характеристики;
- `attachments` - документы и материалы карточки;
- `recommendations` - связанные товары, cross-sell и альтернативы.

### Ошибки
- `404` с `STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND`, если товар неизвестен, снят с публикации или недоступен аудитории.
- `500` с mnemonic-кодом catalog-level ошибки без раскрытия stack trace.

## Endpoint `POST /api/catalog/cart/items`
Добавляет товар в корзину из результата поиска или карточки товара.

### Тело запроса
`AddToCartRequest`:
- `productCode` - обязательный код товара;
- `quantity` - обязательное количество, минимум 1;
- `partnerContextId` - nullable идентификатор партнерского контекста;
- `source` - `SEARCH_RESULT` или `PRODUCT_CARD`.

### Успешный ответ
`CartSummaryResponse` возвращает `itemsCount`, `totalQuantity` и `messageCode=STR_MNEMO_CATALOG_CART_ITEM_ADDED`.

### Ошибки
- `400 STR_MNEMO_CATALOG_ITEM_UNAVAILABLE` - товар недоступен.
- `400 STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED` - количество превышает лимит или остаток.
- `401 STR_MNEMO_AUTH_REQUIRED` - пользователь должен войти перед добавлением в корзину.

## DTO и валидации
- Все предопределенные пользовательские сообщения передаются mnemonic-кодами `STR_MNEMO_*`.
- Описания, состав, характеристики, названия вложений и alt text являются product data и могут передаваться как данные.
- `recommendations` не должен включать текущий товар.
- `media` сортируется по `sortOrder`, главное изображение помечается `primary=true`.
- `availability.status` принимает только значения `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`, `COMING_SOON`, `DISCONTINUED`.

## Связь с frontend
Frontend web-shell использует endpoint карточки при открытии `/product/:productCode`, отображает данные через i18n-ключи для статических элементов и локализует `STR_MNEMO_*` из словарей `resources_ru.ts` и `resources_en.ts`.

## Версионная база
Новые backend или frontend runtime-технологии не вводятся. Контракт должен быть реализован в текущем Spring Boot monolith с автоматической генерацией Swagger через springdoc-openapi.
