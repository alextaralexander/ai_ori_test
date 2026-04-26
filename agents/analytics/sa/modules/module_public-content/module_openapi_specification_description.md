# Module public-content. Полное описание OpenAPI

## Назначение
Модуль `public-content` публикует read-only API для публичной витрины Best Ori Gin. Контракт используется frontend-модулем `public-web` для загрузки главной страницы, community-страницы, навигации, быстрых entry points, ленты новостей, динамических контентных страниц и промо-офферов.

## OpenAPI group
Модуль должен быть зарегистрирован в monolith Swagger group `public-content`.
Канонические URL:
- `/v3/api-docs/public-content`
- `/swagger-ui/public-content`

## Endpointы
- `GET /api/public-content/pages/home`: агрегированная конфигурация главной.
- `GET /api/public-content/pages/community`: агрегированная конфигурация community.
- `GET /api/public-content/navigation`: навигация для header, footer или mobile menu.
- `GET /api/public-content/entry-points`: быстрые переходы для аудитории.
- `GET /api/public-content/news`: опубликованная лента новостей с карточками, featured-материалом и mnemonic-кодом пустого состояния.
- `GET /api/public-content/content/{contentId}`: опубликованная динамическая контентная страница по стабильному идентификатору.
- `GET /api/public-content/offers/{offerId}`: опубликованное промо-предложение по стабильному идентификатору.

## DTO
- `PublicPageResponse`: `pageKey`, `routePath`, `titleKey`, `blocks`, `navigation`, `entryPoints`.
- `ContentBlock`: `blockKey`, `blockType`, `sortOrder`, `payload`.
- `NavigationResponse`: список `NavigationItem`.
- `NavigationItem`: `itemKey`, `labelKey`, `targetType`, `targetValue`, `area`, `children`.
- `EntryPointResponse`: список `EntryPoint`.
- `EntryPoint`: `entryKey`, `labelKey`, `descriptionKey`, `targetRoute`, `audience`.
- `NewsFeedResponse`: `items`, `featured`, `emptyStateCode`.
- `NewsItem`: `newsKey`, `contentId`, `titleKey`, `summaryKey`, `categoryKey`, `imageUrl`, `publishedAt`, `targetRoute`.
- `ContentPageResponse`: `contentId`, `templateCode`, `titleKey`, `descriptionKey`, `breadcrumbs`, `seo`, `sections`, `attachments`, `productLinks`, `ctas`.
- `OfferResponse`: `offerId`, `titleKey`, `summaryKey`, `breadcrumbs`, `seo`, `hero`, `sections`, `attachments`, `productLinks`, `ctas`.
- `ContentSection`: `sectionKey`, `sectionType`, `sortOrder`, `payload`.
- `ContentAttachment`: `attachmentKey`, `fileType`, `titleKey`, `url`, `fileSizeBytes`.
- `ProductLink`: `productRef`, `labelKey`, `targetRoute`.
- `ContentCta`: `labelKey`, `targetType`, `targetValue`, `audience`.
- `ErrorResponse`: `code` в формате `STR_MNEMO_*`.

## Валидация и безопасность
- API не требует авторизации для публичных сценариев, но принимает `audience` для фильтрации состава блоков и переходов.
- Audience не является источником прав доступа к приватным данным.
- Backend не возвращает predefined user-facing строки; только i18n keys и mnemonic codes.
- `targetValue` для external URL должен проходить allowlist.
- Внутренние маршруты должны быть относительными путями платформы.
- `/news` возвращает только новости со статусом `PUBLISHED`, `active_from <= now()` и пустым или будущим `active_to`.
- `/content/{contentId}` и `/offers/{offerId}` не раскрывают body материала, если он отсутствует, не опубликован, еще не начался или снят с публикации.
- Секции рендерятся по `sortOrder` и передают user-facing значения только i18n-ключами.
- PDF-вложения возвращаются только для разрешенного `fileType = PDF`.
- Product links ведут на существующие маршруты каталога или карточек товаров.

## Error contract
- `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`: временно недоступна CMS-конфигурация.
- `STR_MNEMO_PUBLIC_CONTENT_INVALID_AUDIENCE`: некорректная аудитория.
- `STR_MNEMO_PUBLIC_CONTENT_INVALID_NAVIGATION_AREA`: некорректная зона навигации.
- `STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND`: материал отсутствует, не опубликован или снят с публикации.
- `STR_MNEMO_PUBLIC_NEWS_EMPTY`: в ленте нет опубликованных новостей.

Frontend обязан локализовать эти mnemonic codes во всех поддерживаемых словарях.
