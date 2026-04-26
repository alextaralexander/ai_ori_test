# Module public-content. Полное описание OpenAPI

## Назначение
Модуль `public-content` публикует read-only API для публичной витрины Best Ori Gin. Контракт используется frontend-модулем `public-web` для загрузки главной страницы, community-страницы, навигации и быстрых entry points.

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

## DTO
- `PublicPageResponse`: `pageKey`, `routePath`, `titleKey`, `blocks`, `navigation`, `entryPoints`.
- `ContentBlock`: `blockKey`, `blockType`, `sortOrder`, `payload`.
- `NavigationResponse`: список `NavigationItem`.
- `NavigationItem`: `itemKey`, `labelKey`, `targetType`, `targetValue`, `area`, `children`.
- `EntryPointResponse`: список `EntryPoint`.
- `EntryPoint`: `entryKey`, `labelKey`, `descriptionKey`, `targetRoute`, `audience`.
- `ErrorResponse`: `code` в формате `STR_MNEMO_*`.

## Валидация и безопасность
- API не требует авторизации для публичных сценариев, но принимает `audience` для фильтрации состава блоков и переходов.
- Audience не является источником прав доступа к приватным данным.
- Backend не возвращает predefined user-facing строки; только i18n keys и mnemonic codes.
- `targetValue` для external URL должен проходить allowlist.
- Внутренние маршруты должны быть относительными путями платформы.

## Error contract
- `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`: временно недоступна CMS-конфигурация.
- `STR_MNEMO_PUBLIC_CONTENT_INVALID_AUDIENCE`: некорректная аудитория.
- `STR_MNEMO_PUBLIC_CONTENT_INVALID_NAVIGATION_AREA`: некорректная зона навигации.

Frontend обязан локализовать эти mnemonic codes во всех поддерживаемых словарях.
