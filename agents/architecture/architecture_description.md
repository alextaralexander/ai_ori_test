# Best Ori Gin. Описание архитектуры на feature 002

## Version baseline
На дату старта задачи 26.04.2026 целевой baseline: Java 25, Spring Boot 4.x stable, Maven 4.x stable, Hibernate 7.x stable, Liquibase 5.x stable, PostgreSQL 18.x stable, TypeScript 5.x stable, React 19.x stable, Ant Design 6.x stable, Docker 29.x stable, Docker Compose 5.x stable.

## Модули
- `frontend/public-web`: публичное React-приложение для маршрутов `/`, `/home`, `/community`, `/news`, `/content/:contentId`, `/offer/:offerId` и навигационных переходов.
- `backend/monolith/public-content`: Spring Boot модуль, который отдает read-only API публичной CMS-конфигурации, ленты новостей, динамических контентных страниц и промо-офферов.
- `PostgreSQL public-content schema`: хранение страниц, блоков, навигации, entry points, новостей, content pages, offers, sections, attachments и product links.
- `CMS admin контур`: будущий административный контур для управления публичным контентом.
- `Auth/Profile контур`: будущий контур входа, профиля и определения пользовательской аудитории.
- `Catalog контур`: будущий каталог косметики и кампаний.
- `Partner контур`: будущий партнерский офис, отчеты, бонусы и логистика.

## Связи
- Пользователи -> `frontend/public-web`: HTTPS, browser routes `/`, `/home`, `/community`, `/news`, `/content/:contentId`, `/offer/:offerId`.
- `frontend/public-web` -> `backend/monolith/public-content`: HTTPS REST `/api/public-content/pages/*`, `/api/public-content/navigation`, `/api/public-content/entry-points`.
- `frontend/public-web` -> `backend/monolith/public-content`: HTTPS REST `/api/public-content/news`, `/api/public-content/content/{contentId}`, `/api/public-content/offers/{offerId}`.
- `backend/monolith/public-content` -> PostgreSQL: JDBC.
- `CMS admin контур` -> `backend/monolith/public-content`: HTTPS REST для будущего управления CMS-контентом.
- `frontend/public-web` -> `Auth/Profile контур`: HTTPS REST для входа, профиля и определения audience.
- `frontend/public-web` -> `Catalog контур`: HTTPS REST или route navigation к каталогу.
- `backend/monolith/public-content` -> `Catalog контур`: ссылочная интеграция через `productRef` и route references для связанных товаров и категорий; feature #2 не требует синхронного backend-вызова каталога.
- `frontend/public-web` -> `Partner контур`: HTTPS REST или route navigation к партнерскому офису.

## Ownership
Backend module `public-content` должен соблюдать package policy:
- `api`: REST DTO и внешние контракты.
- `domain`: JPA entities и repository interfaces.
- `db`: Liquibase XML changelog files.
- `impl/controller`: REST controllers.
- `impl/service`: service interfaces и implementations.
- `impl/mapper`: MapStruct mappers.
- `impl/validator`: validators для audience, route targets и payload.
- `impl/config`: module config, OpenAPI group и seed data.

## Локализация и сообщения
Frontend user-facing строки хранятся в i18n dictionaries всех поддерживаемых языков. Backend возвращает только i18n keys или mnemonic codes `STR_MNEMO_*`, если сообщение заранее определено и может попасть во frontend.

## Feature #2
Контентные страницы и новости расширяют публичную витрину без выделения нового backend-модуля. `public-content` остается владельцем публичного CMS-контента и добавляет:
- `GET /api/public-content/news` для ленты опубликованных новостей.
- `GET /api/public-content/content/{contentId}` для динамических материалов.
- `GET /api/public-content/offers/{offerId}` для промо-предложений.

Правила публикации одинаковы для новостей, контентных страниц и офферов: материал публичен только при `PUBLISHED`, наступившем `active_from` и пустом или будущем `active_to`. Недоступные материалы возвращают mnemonic-код `STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND` без body контента. Пустая лента новостей возвращает `STR_MNEMO_PUBLIC_NEWS_EMPTY` для локализации во frontend.
