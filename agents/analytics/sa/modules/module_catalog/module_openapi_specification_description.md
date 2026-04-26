# Module catalog. Полное описание OpenAPI на feature 005

## API surface
На feature #5 модуль `catalog` публикует runtime endpoint-ы:
- `GET /api/catalog/search` - поисковая выдача каталога.
- `GET /api/catalog/products/{productCode}` - детальная карточка товара.
- `POST /api/catalog/cart/items` - добавление доступного товара в корзину из выдачи или карточки.

## Runtime Swagger
Модуль должен иметь dedicated OpenAPI group:
- OpenAPI JSON: `/v3/api-docs/catalog`.
- Swagger UI: `/swagger-ui/catalog`.

Swagger/OpenAPI генерируется автоматически из Spring MVC controller-ов внутри package prefix `com.bestorigin.monolith.catalog`; ручные per-endpoint swagger registries не создаются.

## DTO
- `CatalogSearchResponse`: `items`, `recommendations`, `page`, `pageSize`, `totalItems`, `hasNextPage`, `messageCode`.
- `CatalogProductCardResponse`: `productCode`, `name`, `categoryCode`, `categoryName`, `brand`, `volumeLabel`, `campaignCode`, `badges`, `price`, `availability`, `orderLimits`, `media`, `information`, `attachments`, `recommendations`.
- `CatalogProductMedia`: `url`, `altText`, `primary`, `sortOrder`.
- `CatalogProductInformation`: `shortDescription`, `fullDescription`, `usageInstructions`, `ingredients`, `characteristics`.
- `CatalogProductRecommendation`: `productCode`, `name`, `imageUrl`, `price`, `availability`, `recommendationType`.
- `AddToCartRequest`: `productCode`, `quantity`, `audience`, `userContextId`, `partnerContextId`, `source`.
- `CartSummaryResponse`: `itemsCount`, `totalQuantity`, `messageCode`.
- `CatalogErrorResponse`: `code`, `messageCode`.

## STR_MNEMO codes
- `STR_MNEMO_CATALOG_SEARCH_EMPTY` - поисковая выдача пуста.
- `STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND` - карточка товара не найдена или недоступна аудитории.
- `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE` - товар нельзя добавить в корзину.
- `STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED` - количество превышает лимиты заказа или остаток.
- `STR_MNEMO_CATALOG_CART_ITEM_ADDED` - товар добавлен в корзину.
- `STR_MNEMO_AUTH_REQUIRED` - для покупки нужна авторизация.

Frontend обязан локализовать эти коды через `resources_ru.ts` и `resources_en.ts`.

## Валидации
- `size` ограничен диапазоном 1..60.
- `page` не может быть меньше 0.
- `productCode` обязателен и не длиннее 64 символов.
- `quantity` должен быть больше 0.
- `audience=GUEST` допустим для поиска и просмотра карточки, но не допустим для add-to-cart.
- `source=PRODUCT_CARD` используется для добавления из карточки товара.
- Невалидные фильтры, неизвестная карточка и ошибки покупки не должны приводить к раскрытию stacktrace или technical text во frontend.

## Версионная база
Новые OpenAPI-библиотеки не добавляются. Если в репозитории отсутствует springdoc runtime, metadata фиксируется в модульной конфигурации, а подключение полноценного Swagger grouping должно быть синхронизировано с общей задачей монолитного OpenAPI.
