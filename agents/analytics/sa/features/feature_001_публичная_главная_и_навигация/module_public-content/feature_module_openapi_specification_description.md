# OpenAPI описание feature 001 для модуля public-content

## Назначение API
API модуля `public-content` отдает frontend-приложению публичную конфигурацию главной страницы, community, глобальной навигации и быстрых entry points. API предназначен для чтения публичного CMS-контента и не возвращает приватные пользовательские данные.

## Endpoint GET /api/public-content/pages/home
Возвращает конфигурацию главной страницы для маршрутов `/` и `/home`.

Параметры:
- `Accept-Language`: предпочтительный язык локализации.
- `audience`: аудитория отображения, `GUEST`, `AUTHENTICATED`, `CUSTOMER`, `PARTNER`.

Ответ `200`:
- `pageKey`: `HOME`.
- `routePath`: канонический маршрут.
- `titleKey`: frontend i18n key.
- `blocks`: hero, промо-блоки, быстрые ссылки, fallback-блоки.
- `navigation`: пункты header/footer/mobile menu, доступные аудитории.
- `entryPoints`: быстрые переходы в каталог, community, benefits, регистрацию, профиль, заказы, бонусы или партнерский офис.

## Endpoint GET /api/public-content/pages/community
Возвращает конфигурацию public community page. Содержит обзор сообщества, материалы, CTA участия и связанные публичные переходы.

## Endpoint GET /api/public-content/navigation
Возвращает навигационные пункты для зоны:
- `HEADER`;
- `FOOTER`;
- `MOBILE_MENU`.

Навигация фильтруется по `audience`, `enabled=true` и сортируется по `sortOrder`.

## Endpoint GET /api/public-content/entry-points
Возвращает быстрые entry points для публичной главной. Для гостя доступны каталог, community, benefits, регистрация и вход. Для авторизованных аудиторий дополнительно доступны профиль, корзина, история заказов, бонусы или партнерский офис.

## DTO и контракты
- `PublicPageResponse`: агрегированный DTO страницы.
- `ContentBlock`: блок CMS с типом и структурированным `payload`.
- `NavigationItem`: навигационный пункт с вложенными children.
- `EntryPoint`: быстрый переход с i18n-ключами и target route.
- `ErrorResponse`: содержит только mnemonic `code`.

## Валидации
- `audience` принимает только допустимые enum-значения.
- `area` принимает только `HEADER`, `FOOTER`, `MOBILE_MENU`.
- Внутренние маршруты должны начинаться с `/`.
- Внешние ссылки допускаются только из allowlist.
- Backend не возвращает hardcoded predefined user-facing text; используются i18n keys и `STR_MNEMO_*`.

## STR_MNEMO codes
- `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`: публичный CMS-контент временно недоступен, frontend должен показать локализованный fallback.
- `STR_MNEMO_PUBLIC_CONTENT_INVALID_AUDIENCE`: передана неподдерживаемая аудитория.
- `STR_MNEMO_PUBLIC_CONTENT_INVALID_NAVIGATION_AREA`: передана неподдерживаемая зона навигации.

## Swagger/OpenAPI
Для monolith runtime модуль должен входить в OpenAPI group `public-content`.
Канонические URL:
- `/v3/api-docs/public-content`
- `/swagger-ui/public-content`
