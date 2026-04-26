# Feature 004. Module catalog. Описание OpenAPI-контракта

## Назначение API
Модуль `catalog` предоставляет frontend маршрута `/search` контракт поиска товаров и quick add в корзину. Контракт не передает hardcoded пользовательские тексты: названия товаров, описания, категорий и сообщений передаются как i18n keys или mnemonic-коды `STR_MNEMO_*`.

## GET /api/catalog/search
Возвращает поисковую выдачу товаров текущей кампании.

### Query parameters
- `audience`: `GUEST`, `CUSTOMER`, `PARTNER`; по умолчанию `GUEST`.
- `q`: поисковая строка по названию, артикулу, категории, тегам и ключевым словам.
- `category`: slug категории.
- `priceMin`, `priceMax`: безопасный диапазон цены; отрицательные значения нормализуются.
- `availability`: `all`, `inStock`, `outOfStock`.
- `tags`: список кодов тегов.
- `promo`: признак фильтрации по промо-меткам.
- `sort`: `relevance`, `newest`, `priceAsc`, `priceDesc`, `popular`.
- `page`: номер страницы с нуля.
- `size`: размер страницы от 1 до 60.

### Response 200
`CatalogSearchResponse` содержит:
- `items`: карточки товаров текущей страницы.
- `recommendations`: рекомендации для пустой выдачи или дополнительных блоков.
- `page`, `pageSize`, `totalItems`, `hasNextPage`: метаданные пагинации.
- `messageCode`: `null` при найденной выдаче или `STR_MNEMO_CATALOG_SEARCH_EMPTY` при пустом результате.

### CatalogProductCardResponse
- `id`, `sku`, `slug`: технические идентификаторы товара.
- `nameKey`, `descriptionKey`: frontend i18n keys.
- `categorySlug`: slug категории для фильтра и будущих breadcrumbs.
- `imageUrl`: публичный URL изображения.
- `price`, `currency`: цена и валюта для текущей аудитории.
- `availability`: `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`.
- `tags`, `promoBadges`: коды тегов и промо-меток, локализуются на frontend.
- `canAddToCart`: признак доступности quick add.
- `unavailableReasonCode`: `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE` или роль-специфичный mnemonic-код, если товар нельзя добавить.

## POST /api/catalog/cart/items
Добавляет товар из выдачи в корзину авторизованного клиента или партнера.

### Request
`AddToCartRequest`:
- `productId`: UUID товара.
- `quantity`: количество больше нуля.
- `audience`: `CUSTOMER` или `PARTNER`.
- `userContextId`: технический контекст пользователя или тестовой сессии.
- `searchUrl`: URL выдачи, откуда выполнено добавление.

### Response 200
`CartSummaryResponse`:
- `itemsCount`: количество разных позиций в корзине.
- `totalQuantity`: суммарное количество товаров.
- `messageCode`: `STR_MNEMO_CATALOG_CART_ITEM_ADDED`.

### Response 409
`CatalogErrorResponse`:
- `messageCode`: `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`, если товар не опубликован, снят с кампании или недоступен.

## Валидации и безопасность
- Гость не может выполнить quick add; frontend переводит его к входу или регистрации с сохранением URL поиска.
- Backend принимает только допустимые enum-значения и нормализует неопасные фильтры.
- Для предопределенных пользовательских сообщений backend возвращает только mnemonic-коды.
- Выдача не раскрывает неопубликованные товары, категории и закрытые партнерские данные гостю.

## Версионная база
OpenAPI должен соответствовать runtime Swagger модуля `catalog`: `/v3/api-docs/catalog` и `/swagger-ui/catalog`. Реализация использует текущий Spring Boot monolith baseline репозитория без добавления новых библиотек.
