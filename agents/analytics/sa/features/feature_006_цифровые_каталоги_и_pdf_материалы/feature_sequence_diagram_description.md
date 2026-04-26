# Feature 006. Описание sequence-диаграммы

## Назначение
Sequence-диаграмма описывает взаимодействие web-shell frontend, module `catalog`, хранилища PDF-материалов S3/MinIO и frontend i18n при просмотре цифровых каталогов текущего и следующего периода.

## Основной поток загрузки выпуска
1. Пользователь открывает `/products/digital-catalogue-current` или `/products/digital-catalogue-next`.
2. Frontend `DigitalCatalogueView` вызывает `GET /api/catalog/digital-catalogues/current` или `GET /api/catalog/digital-catalogues/next` с параметром `audience`.
3. `DigitalCatalogueController` передает запрос в `DigitalCatalogueService`.
4. Service читает выпуск, страницы, материалы, правила видимости и hotspots из `catalog repository`.
5. Если выпуск доступен роли, backend возвращает `DigitalCatalogueIssueResponse`.
6. Frontend загружает i18n labels для периода, статуса публикации, viewer actions, пустых состояний и ошибок.
7. Пользователь видит выпуск, страницы, PDF viewer, список материалов и интерактивные зоны товаров.

## Ветка недоступного выпуска
Если выпуск отсутствует или закрыт для роли:
- service формирует контролируемую ошибку;
- controller возвращает `403` или `404` с `CatalogErrorResponse`;
- error response содержит `messageCode`, например `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN` или `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND`;
- frontend получает локализованный текст из dictionaries и не показывает raw JSON, stack trace или backend exception.

## Работа viewer
Открытие PDF preview и изменение масштаба выполняются на frontend стороне. Zoom in, zoom out и reset zoom не требуют дополнительных backend вызовов, если preview URL уже разрешен для пользователя.

## Download flow
1. Пользователь нажимает download.
2. Frontend вызывает `POST /api/catalog/digital-catalogues/materials/{materialId}/download`.
3. Backend проверяет видимость материала и `allow_download`.
4. Если действие разрешено, service запрашивает temporary download URL в S3/MinIO storage adapter.
5. Backend возвращает `DigitalCatalogueMaterialActionResponse` с `url`, `expiresAt` и `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY`.
6. Если действие запрещено, backend возвращает `STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED`.

## Share flow
Share flow аналогичен download, но проверяет `allow_share` и возвращает временную share-ссылку. Запрещенный шаринг возвращает `STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED`.

## Переход в карточку товара
Если пользователь выбирает hotspot:
- frontend берет `productCode` из `DigitalCatalogueHotspot`;
- выполняет навигацию на `/product/{productCode}` с campaign/issue context;
- `ProductCardView` использует существующий контракт feature #5 и показывает цену, доступность и промо-условия выбранного периода.

## Ошибки материалов
Если материал отсутствует, удален из storage или временно недоступен:
- backend возвращает `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`;
- frontend показывает локализованную ошибку и предлагает повторить загрузку или выбрать другой материал.

## Backend package ownership
- Controller: `com.bestorigin.monolith.catalog.impl.controller`.
- Service interfaces/implementations: `com.bestorigin.monolith.catalog.impl.service`.
- Repository interfaces and JPA entities: `com.bestorigin.monolith.catalog.domain`.
- DTO/API contracts: `com.bestorigin.monolith.catalog.api`.
- Mappers: `com.bestorigin.monolith.catalog.impl.mapper`.
- Validators: `com.bestorigin.monolith.catalog.impl.validator`.
- Exception handlers: `com.bestorigin.monolith.catalog.impl.exception`.
- Storage adapter/client: `com.bestorigin.monolith.catalog.impl.client` или существующий shared S3/MinIO client, если он уже есть в repository baseline.

## Версионная база
Новые runtime-технологии не вводятся. Sequence рассчитан на существующий Spring Boot monolith, Spring MVC controllers, Hibernate repositories, S3/MinIO integration baseline и web-shell React/TypeScript frontend. Все новые user-facing строки должны быть добавлены во frontend i18n dictionaries для всех поддерживаемых языков.
