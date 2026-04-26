# Best Ori Gin. Описание архитектуры на feature 006

## Version baseline
На дату старта задачи 26.04.2026 используется совместимый baseline текущего монолита: Java 25, Spring Boot 4.0.6, Maven 3.9.13, React 19.2.0, TypeScript 5.9.3, Ant Design 6.0.0, Liquibase XML changelog policy, PostgreSQL-совместимая модель данных и S3/MinIO-compatible storage для файлов. В рамках feature #6 зависимости не обновляются, чтобы сохранить совместимость с уже реализованными feature #1-#5; обновление major/minor baseline требует отдельной миграционной задачи.

## Модули
- `frontend/public-web`: публичное React-приложение для маршрутов `/`, `/home`, `/community`, `/news`, `/content/:contentId`, `/offer/:offerId`, `/FAQ`, `/faq`, `/info/:section?`, `/documents/:documentType`, `/products/digital-catalogue-current`, `/products/digital-catalogue-next`, `/search`, `/product/:productCode`.
- `backend/monolith/public-content`: Spring Boot модуль read-only API для публичной CMS-конфигурации, новостей, контентных страниц, офферов, FAQ, информационных разделов и документов.
- `backend/monolith/catalog`: Spring Boot модуль API для поиска товаров, фильтров, сортировки, карточек выдачи, детальной карточки товара, рекомендаций, quick add в корзину, цифровых выпусков каталога и PDF material actions.
- `PostgreSQL public-content schema`: целевое хранение страниц, блоков, навигации, новостей, content pages, offers, FAQ, info sections, documents и archive versions.
- `PostgreSQL catalog schema`: целевое хранение категорий, товаров, детализации карточек, медиа, вложений, рекомендаций, тегов, промо-меток, строк корзины quick add, цифровых выпусков, страниц, PDF-материалов, hotspots и visibility rules.
- `S3/MinIO catalog PDF materials`: хранилище PDF и preview-материалов цифровых каталогов; module `catalog` выдает только разрешенные temporary URLs.
- `CMS admin контур`: будущий административный контур для управления публичным контентом и справкой.
- `Auth/Profile контур`: будущий контур входа, профиля и определения пользовательской аудитории.
- `Cart/Order контур`: будущий полноценный контур корзины, заказа, оплаты и до заказа; feature #4 добавляет только минимальный quick add summary.
- `Partner контур`: будущий партнерский офис, отчеты, бонусы и логистика.

## Связи
- Пользователи открывают публичные маршруты через `frontend/public-web`.
- Frontend вызывает `backend/monolith/public-content` по REST для страниц, навигации, новостей, контента, офферов, FAQ, info и documents.
- Frontend вызывает `backend/monolith/catalog` по REST для `/api/catalog/search`, `/api/catalog/products/{productCode}`, `/api/catalog/cart/items`, `/api/catalog/digital-catalogues/current`, `/api/catalog/digital-catalogues/next`, `/api/catalog/digital-catalogues/{issueCode}` и PDF material actions.
- Backend возвращает DTO с i18n-ключами и mnemonic-кодами `STR_MNEMO_*`; пользовательские тексты локализуются на frontend.
- `public-content` ссылается на каталог через `productRef` и route references на `/search`; синхронная загрузка товаров выполняется frontend через `catalog`.
- `catalog` подготовлен к будущей интеграции с `Cart/Order контуром`, но на feature #6 хранит минимальные cart summary данные внутри owning module и передает checkout handoff через frontend route `/checkout`.
- `catalog` взаимодействует с S3/MinIO-compatible storage по внутреннему adapter/client protocol для генерации временных download/share URL PDF-материалов.
- CMS admin в будущих фичах будет управлять теми же сущностями через модуль `public-content`.

## Ownership
Backend module `public-content` соблюдает package policy:
- `api`: REST DTO и внешние контракты.
- `domain`: repository interfaces и будущие JPA entities.
- `db`: Liquibase XML changelog files.
- `impl/controller`: REST controllers.
- `impl/service`: service interfaces, implementations, in-memory repository и исключения.
- `impl/config`: module config и seed/fallback configuration.

Backend module `catalog` соблюдает package policy:
- `api`: REST DTO, enum audience/availability/sort и request/response contracts.
- `domain`: repository interfaces и будущие JPA entities.
- `db`: Liquibase XML changelog files.
- `impl/controller`: REST controllers.
- `impl/service`: service interfaces, default service, in-memory repository и exceptions.
- `impl/config`: module metadata и OpenAPI group metadata.
- `impl/client`: storage adapter/client для PDF material URL, если общий S3/MinIO client отсутствует.

## Локализация и сообщения
Все новые frontend user-facing строки размещаются в `resources_ru.ts` и `resources_en.ts`. Backend не отправляет hardcoded пользовательские тексты в API responses; для предопределенных состояний используются `STR_MNEMO_PUBLIC_FAQ_EMPTY`, `STR_MNEMO_PUBLIC_INFO_NOT_FOUND`, `STR_MNEMO_PUBLIC_DOCUMENTS_NOT_FOUND`, `STR_MNEMO_CATALOG_SEARCH_EMPTY`, `STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND`, `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`, `STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED`, `STR_MNEMO_CATALOG_CART_ITEM_ADDED`, `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND`, `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN`, `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`, `STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED`, `STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED`, `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY`, `STR_MNEMO_AUTH_REQUIRED` и существующие коды публичного контента.

## Feature #6
Feature #6 расширяет модуль `catalog` и frontend routes `/products/digital-catalogue-current`, `/products/digital-catalogue-next`:
- `GET /api/catalog/digital-catalogues/current` возвращает текущий опубликованный цифровой выпуск.
- `GET /api/catalog/digital-catalogues/next` возвращает следующий выпуск с учетом роли и preview window.
- `GET /api/catalog/digital-catalogues/{issueCode}` поддерживает прямую ссылку или preview выпуска.
- `POST /api/catalog/digital-catalogues/materials/{materialId}/download` и `/share` создают временные URL только для разрешенных PDF-материалов.
- Frontend показывает PDF viewer, навигацию по страницам, zoom/download/share actions, локализованные состояния и hotspots перехода в `/product/:productCode`.
- S3/MinIO хранит PDF и preview assets, а публичный frontend не получает приватные ссылки без проверки module `catalog`.

## Feature #5
Feature #5 расширяет модуль `catalog` и frontend route `/product/:productCode`:
- `GET /api/catalog/products/{productCode}` для детальной карточки товара, медиа, описания, состава, вложений, доступности, ограничений заказа и рекомендаций.
- `POST /api/catalog/cart/items` принимает `source=PRODUCT_CARD`, `partnerContextId` и проверяет лимиты карточки перед добавлением в корзину.
- Frontend показывает desktop/mobile карточку товара, локализует статические подписи и `STR_MNEMO_*`, а после успешного добавления открывает checkout handoff.
- Партнер получает partner-specific контекст покупки из карточки для будущей бонусной и комиссионной логики.

## Feature #4
Feature #4 добавляет модуль `catalog` и route `/search`:
- `GET /api/catalog/search` для поиска, фильтров, сортировки, пагинации и рекомендаций.
- `POST /api/catalog/cart/items` для добавления доступного товара из выдачи в корзину.
- Frontend сохраняет параметры поиска в URL и локализует товары, теги, промо-метки и messageCode через i18n.
- Партнер получает partner-specific контекст добавления в корзину.

## Feature #3
Feature #3 расширяет публичный контур справочным самообслуживанием:
- `GET /api/public-content/faq` для FAQ с category/query/audience.
- `GET /api/public-content/info/{section}` для информационных страниц.
- `GET /api/public-content/documents/{documentType}` для документов, PDF viewer, скачивания и архива версий.

Реализация остается в модуле `public-content`, потому что FAQ, info и documents являются частью публичного CMS-контента и используют те же правила i18n, публикации, аудитории и безопасного fallback.
