# Feature 002. Описание sequence diagram

## Назначение
Sequence diagram описывает runtime-взаимодействие по feature #2 между web-shell frontend, backend-модулем `public-content`, базой данных public-content, frontend i18n dictionaries и каталогом. Диаграмма покрывает маршруты `/news`, `/content/:contentId` и `/offer/:offerId`.

## Version baseline
На дату старта задачи 26.04.2026 целевой baseline: Java 25, Spring Boot 4.x stable, Spring Web MVC stable, PostgreSQL 18.x stable, TypeScript latest stable, React latest stable, Ant Design latest stable.

## Лента новостей
1. Пользователь открывает `/news`.
2. Frontend вызывает `GET /api/public-content/news?audience=...`.
3. `PublicContentController` передает запрос в `PublicContentService`.
4. Сервис выбирает только опубликованные новости, у которых текущий момент попадает в период публикации.
5. Backend возвращает `NewsFeedResponse` с i18n-ключами и mnemonic-кодом пустого состояния.
6. Frontend локализует ключи через словари и отображает карточки новостей.

## Контентная страница
1. Пользователь выбирает новость или открывает `/content/:contentId` напрямую.
2. Frontend вызывает `GET /api/public-content/content/{contentId}`.
3. Backend проверяет существование, статус и период публикации страницы.
4. Для опубликованной страницы backend возвращает `ContentPageResponse`: breadcrumbs, SEO, секции, PDF-вложения, product links и CTA.
5. Frontend локализует все ключи, рендерит блоки в порядке `sortOrder` и строит переходы к каталогу или товарам.
6. Если материал отсутствует или не опубликован, backend возвращает `404 ErrorResponse` с `STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND`; frontend показывает локализованное состояние недоступности.

## Промо-оффер
1. Пользователь открывает `/offer/:offerId`.
2. Frontend вызывает `GET /api/public-content/offers/{offerId}`.
3. Backend проверяет статус и период публикации оффера.
4. Для активного оффера backend возвращает `OfferResponse`: hero, условия, вложения, связанные товары и CTA.
5. Frontend отображает оффер и дает пользователю перейти в каталог или к связанным товарам.
6. Недоступный оффер не раскрывает контент и возвращает mnemonic-код недоступности.

## Правила контракта
- Backend не возвращает predefined user-facing тексты.
- Все пользовательские подписи приходят как i18n-ключи или mnemonic-коды `STR_MNEMO_*`.
- Frontend отвечает за локализацию, SEO fallback и адаптивный рендер.
- Переходы к каталогу выполняются только через существующие маршруты платформы.
