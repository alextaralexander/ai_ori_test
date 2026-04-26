# Feature 006. Module catalog. Описание OpenAPI-контракта

## Назначение
OpenAPI-контракт feature #6 описывает REST API модуля `catalog` для текущего и следующего цифрового каталога Best Ori Gin, получения выпуска по коду, отображения PDF viewer, скачивания и шаринга разрешенных PDF-материалов.

## Базовый URL и Swagger
- Базовый API URL: `/api/catalog`.
- Module key для monolith Swagger/OpenAPI: `catalog`.
- Canonical OpenAPI JSON: `/v3/api-docs/catalog`.
- Canonical Swagger UI: `/swagger-ui/catalog`.
- Controllers должны находиться внутри package prefix module `catalog` и внутри `impl/controller`, чтобы Springdoc автоматически включил endpoints в group `catalog`.

## Endpoints

### GET /api/catalog/digital-catalogues/current
Возвращает текущий опубликованный цифровой каталог для роли пользователя.

Параметры:
- `audience` query, optional: `GUEST`, `CUSTOMER`, `PARTNER`, `CONTENT_MANAGER`, `CATALOG_MANAGER`.

Ответы:
- `200 DigitalCatalogueIssueResponse` - текущий каталог доступен.
- `404 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND` - опубликованный текущий выпуск отсутствует.

### GET /api/catalog/digital-catalogues/next
Возвращает следующий каталог, если он разрешен для роли или открыт для публичного предпросмотра.

Ответы:
- `200 DigitalCatalogueIssueResponse` - следующий каталог доступен.
- `403 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN` - выпуск существует, но закрыт для роли.
- `404 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND` - следующий выпуск отсутствует.

### GET /api/catalog/digital-catalogues/{issueCode}
Возвращает выпуск по бизнес-коду. Используется для прямых ссылок и менеджерского preview.

Параметры:
- `issueCode` path, required, длина 1..64.
- `audience` query, optional.

Ответы:
- `200 DigitalCatalogueIssueResponse`.
- `403 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN`.
- `404 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND`.

### POST /api/catalog/digital-catalogues/materials/{materialId}/download
Создает download URL для PDF-материала, если скачивание разрешено.

Request body:
- `audience` required.
- `returnUrl` optional, max 1024.

Ответы:
- `200 DigitalCatalogueMaterialActionResponse` с URL и `expiresAt`.
- `403 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED`.
- `404 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`.

### POST /api/catalog/digital-catalogues/materials/{materialId}/share
Создает share URL для PDF-материала, если шаринг разрешен.

Ответы:
- `200 DigitalCatalogueMaterialActionResponse`.
- `403 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED`.
- `404 CatalogErrorResponse` с `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`.

## DTO

### DigitalCatalogueIssueResponse
Основной DTO выпуска:
- `issueCode` - код выпуска.
- `title` - название выпуска как данные каталога.
- `periodType` - `CURRENT`, `NEXT`, `ARCHIVE`.
- `period.startDate`, `period.endDate` - период действия.
- `publicationStatus` - `DRAFT`, `SCHEDULED`, `PUBLISHED`, `ARCHIVED`.
- `viewer` - capability flags `zoom`, `download`, `share`.
- `pages[]` - страницы выпуска.
- `materials[]` - PDF и брошюры выпуска.

### DigitalCataloguePage
Описывает страницу viewer:
- `pageNumber` - номер страницы, минимум 1.
- `imageUrl` - URL preview страницы.
- `thumbnailUrl` - optional URL миниатюры.
- `widthPx`, `heightPx` - размеры страницы.
- `hotspots[]` - интерактивные зоны.

### DigitalCatalogueHotspot
Описывает интерактивную зону перехода в карточку:
- `productCode` - target `/product/:productCode`.
- `xPercent`, `yPercent`, `widthPercent`, `heightPercent` - координаты в процентах от страницы, диапазон 0..100.

### DigitalCatalogueMaterial
Описывает PDF-материал:
- `materialId` - идентификатор.
- `materialType` - `MAIN_CATALOG`, `BROCHURE`, `PROMO_LEAFLET`, `DOCUMENT`.
- `title` - название материала.
- `fileSizeBytes` - nullable размер.
- `publicationStatus` - статус публикации.
- `previewUrl` - URL для viewer, nullable если preview закрыт.
- `actions.canOpen`, `actions.canDownload`, `actions.canShare` - действия, разрешенные текущему пользователю.

### DigitalCatalogueMaterialActionResponse
Ответ на download/share:
- `url` - временный URL.
- `expiresAt` - дата истечения.
- `messageCode` - mnemonic-код успешного действия, например `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY`.

### CatalogErrorResponse
Единый error DTO:
- `code` - технический код ошибки.
- `messageCode` - mnemonic-код для frontend i18n.

## Валидации
- `issueCode`, `materialId`, `productCode` не пустые, max 64.
- `audience` должен входить в enum.
- Координаты hotspot должны быть в диапазоне 0..100.
- `widthPercent` и `heightPercent` должны быть больше 0.
- `returnUrl` не длиннее 1024 и не должен использоваться backend как user-facing текст.

## Доступ и безопасность
- Backend фильтрует выпуски, материалы и hotspots по роли до возврата DTO.
- Приватные PDF не должны попадать в `previewUrl`, download URL или share URL для неразрешенной роли.
- `CONTENT_MANAGER` и `CATALOG_MANAGER` могут использовать preview только через разрешенный контур; публичные маршруты не раскрывают preview-ссылки гостям.
- Backend не возвращает hardcoded user-facing сообщения; frontend получает только данные и `STR_MNEMO_*`.

## Mnemonic-коды
Новые коды, обязательные для frontend dictionaries:
- `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND`.
- `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN`.
- `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`.
- `STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED`.
- `STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED`.
- `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY`.

## Frontend i18n contract
Frontend должен локализовать:
- labels для текущего и следующего каталога;
- статусы публикации;
- действия viewer: zoom in, zoom out, reset zoom, download, share;
- пустые и ошибочные состояния;
- aria-label для навигации по страницам, hotspots и кнопок viewer.

## Версионная база
Контракт не требует новых runtime-технологий. Используется текущий Spring Boot monolith, Spring MVC, springdoc-openapi, Java/Maven baseline и существующий web-shell frontend. Если при реализации будет добавлен сторонний PDF viewer package, его версия должна быть зафиксирована отдельно в implementation artifacts.
