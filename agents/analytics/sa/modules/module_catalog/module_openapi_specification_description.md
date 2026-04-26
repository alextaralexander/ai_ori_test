# Module catalog. Полное описание OpenAPI на feature 004

## API surface
На feature #4 модуль `catalog` публикует два runtime endpoint-а:
- `GET /api/catalog/search` - поисковая выдача каталога.
- `POST /api/catalog/cart/items` - quick add доступного товара в корзину.

## Runtime Swagger
Модуль должен иметь dedicated OpenAPI group:
- OpenAPI JSON: `/v3/api-docs/catalog`.
- Swagger UI: `/swagger-ui/catalog`.

В текущем монолите Swagger/OpenAPI должен генерироваться автоматически из Spring MVC controller-ов внутри package prefix `com.bestorigin.monolith.catalog`.

## DTO
- `CatalogSearchResponse`: `items`, `recommendations`, `page`, `pageSize`, `totalItems`, `hasNextPage`, `messageCode`.
- `CatalogProductCardResponse`: `id`, `sku`, `slug`, `nameKey`, `descriptionKey`, `categorySlug`, `imageUrl`, `price`, `currency`, `availability`, `tags`, `promoBadges`, `canAddToCart`, `unavailableReasonCode`.
- `AddToCartRequest`: `productId`, `quantity`, `audience`, `userContextId`, `searchUrl`.
- `CartSummaryResponse`: `itemsCount`, `totalQuantity`, `messageCode`.
- `CatalogErrorResponse`: `messageCode`.

## STR_MNEMO codes
- `STR_MNEMO_CATALOG_SEARCH_EMPTY` - поисковая выдача пуста.
- `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE` - товар нельзя добавить в корзину.
- `STR_MNEMO_CATALOG_CART_ITEM_ADDED` - товар добавлен в корзину.

Frontend обязан локализовать эти коды через `resources_ru.ts` и `resources_en.ts`.

## Валидации
- `size` ограничен диапазоном 1..60.
- `page` не может быть меньше 0.
- `quantity` должен быть больше 0.
- `audience=GUEST` допустим для поиска, но не допустим для quick add.
- Невалидные фильтры поиска не должны приводить к раскрытию stacktrace или technical text во frontend.

## Версионная база
Новые OpenAPI-библиотеки не добавляются. Если в репозитории отсутствует springdoc runtime, metadata фиксируется в модульной конфигурации, а подключение полноценного Swagger grouping должно быть синхронизировано с общей задачей монолитного OpenAPI.
