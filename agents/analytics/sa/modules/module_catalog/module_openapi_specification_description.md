# Module catalog. Полное описание OpenAPI на feature 006

## Назначение
`module_catalog` предоставляет REST API для поиска товаров, карточки товара, добавления в корзину из catalog context и цифровых каталогов с PDF-материалами. Спецификация должна соответствовать runtime Swagger group `catalog`.

## Swagger/OpenAPI
- OpenAPI JSON: `/v3/api-docs/catalog`.
- Swagger UI: `/swagger-ui/catalog`.
- Controllers должны находиться в package prefix module `catalog` и в `impl/controller`.
- Ручная регистрация отдельных endpoint lists запрещена; Springdoc должен собрать endpoints автоматически.

## Existing endpoints through feature #5
- `GET /api/catalog/search` - поиск и фильтрация опубликованных товаров текущей кампании.
- `GET /api/catalog/products/{productCode}` - карточка товара с медиа, ценой, доступностью, информационными секциями, вложениями и рекомендациями.
- `POST /api/catalog/cart/items` - добавление доступного товара в корзину из поиска или карточки.

## New endpoints in feature #6
- `GET /api/catalog/digital-catalogues/current` - текущий цифровой выпуск.
- `GET /api/catalog/digital-catalogues/next` - следующий цифровой выпуск с учетом роли и preview window.
- `GET /api/catalog/digital-catalogues/{issueCode}` - выпуск по коду.
- `POST /api/catalog/digital-catalogues/materials/{materialId}/download` - временная download-ссылка.
- `POST /api/catalog/digital-catalogues/materials/{materialId}/share` - временная share-ссылка.

## DTO groups

### Catalog search/product/cart
Сохраняются DTO, введенные предыдущими features:
- search request params: `audience`, `q`, `category`, `availability`, `sort`, `page`, `size`;
- product card response: `productCode`, `name`, `price`, `availability`, `orderLimits`, `media`, `information`, `attachments`, `recommendations`;
- cart add response: cart summary and mnemonic message code.

### Digital catalogue
Новые DTO:
- `DigitalCatalogueIssueResponse` - выпуск, период, статус, viewer capabilities, pages, materials.
- `DigitalCataloguePage` - номер страницы, preview image, thumbnail, hotspots.
- `DigitalCatalogueMaterial` - материал, тип, title, preview URL and allowed actions.
- `DigitalCatalogueMaterialActionResponse` - temporary URL, expiration and mnemonic success code.

## Валидации
- `productCode`, `issueCode`, `materialId` max 64 and nonblank.
- `audience` enum: `GUEST`, `CUSTOMER`, `PARTNER`, `CONTENT_MANAGER`, `CATALOG_MANAGER`.
- Pagination: page >= 0, size 1..60.
- Hotspot coordinates stay in 0..100 percent.
- Download/share actions require allowed visibility and action flag.

## Error and mnemonic contract
Backend must not send hardcoded user-facing messages to frontend. Controlled user-facing states use `messageCode`.

Catalog codes:
- `STR_MNEMO_CATALOG_SEARCH_EMPTY`.
- `STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND`.
- `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`.
- `STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED`.
- `STR_MNEMO_CATALOG_CART_ITEM_ADDED`.

Digital catalogue codes:
- `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND`.
- `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN`.
- `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`.
- `STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED`.
- `STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED`.
- `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY`.

## Frontend i18n
All labels, buttons, statuses, empty states, viewer actions and aria-label values must be resolved through frontend dictionaries. Every new mnemonic code must be present in all supported frontend languages in the same implementation task.

## Версионная база
Новые runtime-технологии OpenAPI не вводятся. Используется текущий Spring Boot, Spring MVC and springdoc-openapi baseline module `catalog`. Если implementation добавит отдельную PDF viewer dependency во frontend, ее версия должна быть зафиксирована в implementation artifacts.
